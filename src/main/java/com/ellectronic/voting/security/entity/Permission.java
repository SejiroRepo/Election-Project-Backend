package com.ellectronic.voting.security.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table (
  name = "permissions",
  uniqueConstraints = {
    @UniqueConstraint (
      name = "uk_permission_name",
      columnNames = {"name"}
    )
  }
)

public class Permission {
 
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, length = 100)
  private String name;

  @Column(length = 255)
  private String description;

  protected Permission() {
  
  } 

  public Permission(String name, String description) {
    this.name = name;
    this.description = description;
  }

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
