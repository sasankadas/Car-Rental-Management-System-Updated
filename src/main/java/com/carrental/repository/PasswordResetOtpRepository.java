package com.carrental.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.carrental.entity.PasswordResetOtp;

public interface PasswordResetOtpRepository  extends JpaRepository<PasswordResetOtp, Long>{
	PasswordResetOtp findByOtpAndUsedFalse(String otp);
}
