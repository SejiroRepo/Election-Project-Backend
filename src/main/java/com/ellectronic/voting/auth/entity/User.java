package com.ellectronic.voting.auth.entity;

import com.ellectronic.voting.common.enums.AccountStatus;
import com.ellectronic.voting.security.entity.Role;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(
  name = "users",
  uniqueConstraints = {
    @UniqueConstraint(
      name = "uk_user_matric_number",
      columnNames = "matric_number"
    ),
    @UniqueConstraint(
      name = "uk_user_email",
      columnNames = "email"
    )
  }
)

public class User{

  
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private  UUID id;

  @Column(name = "matricNumber", length = 50)
  private String matricNumber;

  @Column(length= 150)
  private String email;

  @Column(name = "password_hash", nullable = false)
  private String passwordHash;

  @Column(name = "first_name", nullable = false, length = 100)
  private String firstName;

  @Column(name = "last_name", nullable = false, length = 100)
  private String lastName;

  @Enumerated(EnumType.STRING)
  @Column(name = "account_status", nullable = false, length = 30)
  private AccountStatus accountStatus = AccountStatus.PENDING;

  @Column(name = "pasword_change_reuired", nullable= false)
  private boolean passwordChangeRequired = false;

  @ManyToMany
  @JoinTable(
    name = "user_roles",
    joinColumns = @JoinColumn(name = "user_id"),
    inverseJoinColumns = @JoinColumn(name = "role_id"),
    uniqueConstraints = {
      @UniqueConstraint(
        name = "uk_user_role",
        columnNames = {"user_id", "role_id"}
      )
    }
  )

  private Set<Role> roles = new HashSet<>();

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  protected User() {
  }

  public User(String firstName, String lastName, String matricNumber, String email, String passwordHash) {
      this.firstName = firstName;
      this.lastName = lastName;
      this.matricNumber = matricNumber;
      this.email = email;
      this.passwordHash = passwordHash;
  }

  public void addRole(Role role) {
      this.roles.add(role);
  }

  public void removeRole(Role role) {
      this.roles.remove(role);
  }

  @PrePersist
  protected void onCreate() {
      LocalDateTime now = LocalDateTime.now();
      this.createdAt = now;
      this.updatedAt = now;
  }

  @PreUpdate
  protected void onUpdate() {
      this.updatedAt = LocalDateTime.now();
  }

  public UUID getId() {
      return id;
  }

  public String getMatricNumber() {
      return matricNumber;
  }

  public void setMatricNumber(String matricNumber) {
      this.matricNumber = matricNumber;
  }

  public String getEmail() {
      return email;
  }

  public void setEmail(String email) {
      this.email = email;
  }

  public String getPasswordHash() {
      return passwordHash;
  }

  public void setPasswordHash(String passwordHash) {
      this.passwordHash = passwordHash;
  }

  public String getFirstName() {
      return firstName;
  }

  public void setFirstName(String firstName) {
      this.firstName = firstName;
  }

  public String getLastName() {
      return lastName;
  }

  public void setLastName(String lastName) {
      this.lastName = lastName;
  }

  public AccountStatus getAccountStatus() {
      return accountStatus;
  }

  public void setAccountStatus(AccountStatus accountStatus) {
      this.accountStatus = accountStatus;
  }

  public boolean isPasswordChangeRequired() {
      return passwordChangeRequired;
  }

  public void setPasswordChangeRequired(boolean passwordChangeRequired) {
      this.passwordChangeRequired = passwordChangeRequired;
  }

  public Set<Role> getRoles() {
      return roles;
  }

  public void setRoles(Set<Role> roles) {
      this.roles = roles;
  }

  public LocalDateTime getCreatedAt() {
      return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
      return updatedAt;
  }
}