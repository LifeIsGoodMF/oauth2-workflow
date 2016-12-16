package com.test.domain;

import org.hibernate.annotations.NaturalId;
import org.springframework.security.core.GrantedAuthority;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "user_role")
public class UserRoleEntity implements GrantedAuthority {

	@Id
	@GeneratedValue
	private long id;

	@NaturalId
    @Column(nullable = false, unique = true)
	@Enumerated(EnumType.STRING)
	private UserRoleType role;

	@ManyToMany(fetch = FetchType.LAZY, mappedBy = "roles")
	private Set<UserEntity> users = new HashSet<>();

	@Override
	public String getAuthority() {
		return role.name();
	}

	public long getId() {
		return id;
	}

	public UserRoleType getRole() {
		return role;
	}

	public void setRole(UserRoleType role) {
		this.role = role;
	}

	public Set<UserEntity> getUsers() {
		return users;
	}

	@Override
	public String toString() {
		return role.name();
	}
}
