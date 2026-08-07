package com.carrental.service;

public interface EmailService {
	public void sendVerificationMail(String to, String link, String name);
	 public String sendVerificationMailTemplate(String link, String name);
	 public void sendOtpMail(String to, String otp);
	 public String sendOtpMailTemplate(String otp);

}
