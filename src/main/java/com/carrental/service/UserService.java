package com.carrental.service;

import com.carrental.entity.User;

public interface UserService {
	public String registerUser(User user);
	boolean verifyEmail(String token);
	User findByEmail(String email);
	void sendOtp(String email);
	public boolean verifyOtp(String otp);
	void changePassword(String username,String password);

	/**
	 * Used by the logged-in "change password" screens (user & admin): verifies the old
	 * password before setting the new one. Returns false if the old password does not match.
	 */
	boolean changePasswordWithVerification(String email, String oldPassword, String newPassword);

}
