package com.ellectronic.voting.auth.service;

import com.ellectronic.voting.auth.dto.StudentRegistrationRequest;
import com.ellectronic.voting.auth.dto.StudentRegistrationResponse;
import com.ellectronic.voting.auth.entity.User;
import com.ellectronic.voting.auth.repository.UserRepository;
import com.ellectronic.voting.auth.support.IdentifierNormalizer;
import com.ellectronic.voting.auth.support.PasswordPolicy;
import com.ellectronic.voting.common.exception.BusinessRuleException;
import com.ellectronic.voting.common.exception.DuplicateResourceException;
import com.ellectronic.voting.storage.dto.StoredFile;
import com.ellectronic.voting.storage.service.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class StudentRegistrationService {

  private static final Logger log = LoggerFactory.getLogger(StudentRegistrationService.class);

  private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

  private static final String VERIFICATION_FOLDER = "student-verification-documents";

  private final UserRepository userRepository;
  private final FileStorageService fileStorageService;
  private final StudentRegistrationPersistenceService persistenceService;

  public StudentRegistrationService(
    UserRepository userRepository,
    FileStorageService fileStorageService,
    StudentRegistrationPersistenceService persistenceService
  ) {
    this.userRepository = userRepository;
    this.fileStorageService = fileStorageService;
    this.persistenceService = persistenceService;
  }

  public StudentRegistrationResponse register(StudentRegistrationRequest request, MultipartFile schoolIdImage) {
    String matricNumber = normalizeMatricNumber(request.matricNumber());
    String email = normalizeEmail(request.email());

    validatePasswordLength(request.password());
    validateSchoolIdImage(schoolIdImage);

    boolean isAccountExitByMatricNumber = userRepository.existsByMatricNumber(matricNumber);
    boolean isAccountExitByEmail = userRepository.existsByEmail(email);

    if (isAccountExitByMatricNumber) {
      throw new DuplicateResourceException("An account with this matric number already exists.");
    }

    if (isAccountExitByEmail) {
      throw new DuplicateResourceException("An account with this email already exists.");
    }

    StoredFile storedFile = fileStorageService.upload(schoolIdImage, VERIFICATION_FOLDER);

    try {
      User savedUser = persistenceService.persist(
        request, matricNumber,
        email, storedFile
      );

      return new StudentRegistrationResponse(
        savedUser.getId(),
        savedUser.getMatricNumber(),
        savedUser.getEmail(),
        savedUser.getAccountStatus(),
        savedUser.getCreatedAt(),
        "Registration submitted successfully. Await admin verification."
      );

    } catch (DataIntegrityViolationException exception) {
      deleteUploadedFile(storedFile);
      throw new DuplicateResourceException("An account with the supplied matric number or email already exists.");

    } catch (RuntimeException exception) {
      deleteUploadedFile(storedFile);
      throw exception;
    }
  }

  private String normalizeMatricNumber(String matricNumber) {
    return IdentifierNormalizer.normalizeMatricNumber(matricNumber);
  }

  private String normalizeEmail(String email) {
    return IdentifierNormalizer.normalizeEmail(email);
  }

  private void validatePasswordLength(String password) {
    PasswordPolicy.validate(password);
  }

  private void validateSchoolIdImage(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new BusinessRuleException("A school ID image is required.");
    }

    if (file.getSize() > MAX_FILE_SIZE) {
      throw new BusinessRuleException("School ID image must not exceed 5 MB.");
    }

    try {
      byte[] bytes = file.getBytes();

      String actualContentType = detectImageType(bytes);

      if (actualContentType == null) {
        throw new BusinessRuleException("Only valid JPEG, PNG, and WebP images are accepted.");
      }

      if (!actualContentType.equalsIgnoreCase(file.getContentType())) {
        throw new BusinessRuleException("The uploaded image content does not match its declared file type.");
      }
    } 
    catch (IOException exception) {
      throw new BusinessRuleException("Unable to read the uploaded school ID image.");
    }
  }

  private String detectImageType(byte[] bytes) {
    if (bytes.length >= 8
      && (bytes[0] & 0xFF) == 0x89
      && bytes[1] == 0x50
      && bytes[2] == 0x4E
      && bytes[3] == 0x47
      && bytes[4] == 0x0D
      && bytes[5] == 0x0A
      && bytes[6] == 0x1A
      && bytes[7] == 0x0A
    ){
      return "image/png";
    }

    if (bytes.length >= 3
      && (bytes[0] & 0xFF) == 0xFF
      && (bytes[1] & 0xFF) == 0xD8
      && (bytes[2] & 0xFF) == 0xFF
    ){
      return "image/jpeg";
    }

    if (bytes.length >= 12
      && bytes[0] == 'R'
      && bytes[1] == 'I'
      && bytes[2] == 'F'
      && bytes[3] == 'F'
      && bytes[8] == 'W'
      && bytes[9] == 'E'
      && bytes[10] == 'B'
      && bytes[11] == 'P'
    ){
      return "image/webp";
    }

    return null;
  }

  private void deleteUploadedFile(StoredFile storedFile) {
    try {
      fileStorageService.delete(storedFile.storageKey());
    } catch (RuntimeException cleanupException) {
      log.error(
        "Could not clean up uploaded verification document: {}",
        storedFile.storageKey(),
        cleanupException
      );
    }
  }
}