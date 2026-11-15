package com.ellectronic.voting.student.entity;

import com.ellectronic.voting.auth.entity.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
  name = "student_verification_documents",
  uniqueConstraints = {
    @UniqueConstraint(
      name = "uk_student_verification_user",
      columnNames = "user_id"
    )
  }
)

public class StudentVerificationDocument {
  
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @OneToOne
  @JoinColumn(
    name = "user_id",
    nullable = false,
    unique = true
  )
  private User user;

  @Column(nullable = false, length = 50)
  private String storageProvider; 

  @Column(nullable = false, length = 500)
  private String storageKey;

  @Column(nullable = false, length = 100)
  private String contentType;

  @Column(nullable = false)
  private long fileSize;

  @Column(nullable = false)
  private LocalDateTime uploadedAt;

  protected StudentVerificationDocument() {
  }

  public StudentVerificationDocument(
    User user,
    String storageProvider,
    String storageKey,
    String contentType,
    long fileSize
  ) {
    this.user = user;
    this.storageProvider = storageProvider;
    this.storageKey = storageKey;
    this.contentType = contentType;
    this.fileSize = fileSize;
    this.uploadedAt = LocalDateTime.now();
  }

  public UUID getId() {
    return id;
  }

  public User getUser() {
    return user;
  }

  public String getStorageProvider() {
    return storageProvider;
  }

  public String getStorageKey() {
    return storageKey;
  }

  public String getContentType() {
    return contentType;
  }

  public long getFileSize() {
    return fileSize;
  }

  public LocalDateTime getUploadedAt() {
    return uploadedAt;
  }
}
