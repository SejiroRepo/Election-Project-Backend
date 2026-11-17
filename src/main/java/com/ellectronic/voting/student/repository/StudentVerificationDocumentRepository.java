package com.ellectronic.voting.student.repository;

import com.ellectronic.voting.student.entity.StudentVerificationDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.Optional;

public interface StudentVerificationDocumentRepository extends JpaRepository<StudentVerificationDocument, UUID>{
  
  Optional<StudentVerificationDocument> findByUserId(UUID userId);
  boolean existsByUserId(UUID userId);

}
