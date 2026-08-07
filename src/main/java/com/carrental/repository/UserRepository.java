package com.carrental.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.carrental.entity.User;

@Repository
public interface UserRepository  extends JpaRepository<User, Long>{
	User findByVerificationToken(String verificationToken);
	User findByEmail(String email);

	boolean existsByEmail(String email);

	long countByRole(String role);

	@Query("select u from User u where " +
			"(:keyword is null or lower(u.name) like lower(concat('%',:keyword,'%')) " +
			"  or lower(u.email) like lower(concat('%',:keyword,'%')) " +
			"  or lower(u.phone) like lower(concat('%',:keyword,'%'))) " +
			"and (:role is null or u.role = :role) " +
			"and (:enabled is null or u.enabled = :enabled) " +
			"order by u.createdAt desc")
	List<User> search(@Param("keyword") String keyword,
			@Param("role") String role,
			@Param("enabled") Boolean enabled);
}
