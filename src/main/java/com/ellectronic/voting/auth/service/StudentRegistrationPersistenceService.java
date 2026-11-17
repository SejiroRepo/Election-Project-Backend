package com.ellectronic.voting.auth.service;

import com.ellectronic.voting.auth.dto.StudentRegistrationRequest;
import com.ellectronic.voting.auth.entity.User;
import com.ellectronic.voting.auth.repository.UserRepository;
import com.ellectronic.voting.common.enums.AccountStatus;
import com.ellectronic.voting.common.enums.RoleName;
import com.ellectronic.voting.security.entity.Role;
import com.ellectronic.voting.security.repository.RoleRepository;
import com.ellectronic.voting.security.service.PasswordService;
import com.ellectronic.voting.storage.dto.StoredFile;
import com.ellectronic.voting.student.entity.StudentVerificationDocument;
import com.ellectronic.voting.student.repository.StudentVerificationDocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentRegistrationPersistenceService {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final StudentVerificationDocumentRepository documentRepository;
  private final PasswordService passwordService;

  public StudentRegistrationPersistenceService(
    UserRepository userRepository, 
    RoleRepository roleRepository, 
    StudentVerificationDocumentRepository documentRepository,
    PasswordService passwordService
  ){
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.documentRepository = documentRepository;
    this.passwordService = passwordService;    
  }


  @Transactional
  public User persist(StudentRegistrationRequest request, String matricNumber, String email, StoredFile storedFile){
  
    Role studentRole = roleRepository.findByName(RoleName.STUDENT).orElseThrow(() -> new IllegalStateException("STUDENT role has not been initialized"));
    User student = new User(
      request.firstName().trim(),
      request.lastName().trim(),
      matricNumber,
      email,
      passwordService.encode(request.password())
    );
  
    student.setAccountStatus(AccountStatus.PENDING);
    student.addRole(studentRole);

    User savedStudent = userRepository.saveAndFlush(student);

    StudentVerificationDocument studentDocument = new StudentVerificationDocument(
      savedStudent,
      storedFile.provider(),
      storedFile.storageKey(),
      storedFile.contentType(),
      storedFile.size()
    );

    documentRepository.saveAndFlush(studentDocument);

    return savedStudent;
  }

}
