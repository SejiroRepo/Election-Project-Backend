package com.ellectronic.voting.security.entity;

import com.ellectronic.voting.common.enums.RoleName;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(
  name = "roles",
  uniqueConstraints ={
    @UniqueConstraint(
      name = "uk_role_name",
      columnNames = {"name"}
    )
  } 
)


public class Role{

  @Id 
  @GeneratedValue(strategy = GenerationType.UUID) 
  private UUID id;

  @Enumerated (EnumType.STRING)
  @Column (nullable = false, length = 50)
  private RoleName name;

  @Column(length = 255)
  private String description;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
    name = "roles_permissions",
    joinColumns = @JoinColumn(name = "role_id"),
    inverseJoinColumns = @JoinColumn(name = "permission_id"),
    uniqueConstraints = {
      @UniqueConstraint(
        name = "uk_role_permission",
        columnNames = {"role_id", "permission_id"}
      )
    }
  )

  private Set<Permission> permissions = new HashSet<>();

  protected Role() {
  }

  public Role(RoleName name, String description){
    this.name = name;
    this.description = description;
  }

  public void addPermission(Permission permission){
    this.permissions.add(permission);
  }

  public void removePermission(Permission permission){
    this.permissions.remove(permission);
  }

  public UUID getId(){
    return id;
  }

  public RoleName getName(){
    return name;
  }

  public void setName(RoleName name){
    this.name = name;
  }

  public String getDescription(){
    return description;
  }

  public void setDescripiton(String description){
    this.description = description;
  }

  public Set<Permission> getPermission(){
    return permissions;
  }

  public void setPermission(Set<Permission> permissions){
    this.permissions = permissions;
  }
}
