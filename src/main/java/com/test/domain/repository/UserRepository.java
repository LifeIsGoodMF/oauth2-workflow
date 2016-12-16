package com.test.domain.repository;

import com.test.domain.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
	UserEntity findByEmailIgnoreCase(String email);

//	@EntityGraph(attributePaths = { "userTasks" }, type = EntityGraph.EntityGraphType.LOAD)
//	UserEntity findOneById(long userId);
}
