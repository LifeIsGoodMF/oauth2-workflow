package com.test.domain;

import org.hibernate.annotations.NaturalId;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity(name = "users")
public class UserEntity implements Serializable {
	@Id
	@GeneratedValue
	private long id;

    @Column(nullable = false)
    @Size(max = 50)
	private String name;

    @Size(max = 256)
    @Column(nullable = false)
    private String password;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "users_user_role", joinColumns = { @JoinColumn(name = "user_id") }, inverseJoinColumns = { @JoinColumn(name = "role_id") })
	private Set<UserRoleEntity> roles = new HashSet<>();

	@NaturalId
	@Size(max = 64)
	@Column(unique = true, nullable = false)
	private String email;

	@Column(name = "enabled", nullable = false)
	private boolean isEnabled;

    @OneToOne(mappedBy = "user", cascade = CascadeType.REMOVE)
    private VerificationTokenEntity verificationTokenEntity;

	public long getId() {
		return id;
	}

	public boolean isAdmin () {
		return roles.stream()
				.map(UserRoleEntity::getRole)
				.anyMatch(UserRoleType.ROLE_ADMIN::equals);
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	public void setEnabled(boolean enabled) {
		isEnabled = enabled;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Set<UserRoleEntity> getRoles() {
		return roles;
	}

	public void addRole(UserRoleEntity userRoleEntity) {
		roles.add(userRoleEntity);
	}

	@Override
	public String toString() {
		return email + "(" + id + ")";
	}
}
