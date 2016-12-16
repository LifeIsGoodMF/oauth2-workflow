package com.test.domain.repository;

import com.test.domain.UserRoleEntity;
import com.test.domain.UserRoleType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRoleRepository extends JpaRepository<UserRoleEntity, Long> {
	UserRoleEntity findByRole(UserRoleType userRoleType);
}
