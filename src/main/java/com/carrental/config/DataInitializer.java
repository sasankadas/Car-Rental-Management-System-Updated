package com.carrental.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.carrental.entity.User;
import com.carrental.repository.UserRepository;

/**
 * Seeds a default administrator account on startup so the system is usable out of the
 * box. Change the credentials via the admin.default.* properties (or environment
 * variables) before deploying to production, and change the password immediately after
 * first login.
 */
@Component
public class DataInitializer implements CommandLineRunner {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Value("${admin.default.email:admin@driveeasy.com}")
	private String defaultAdminEmail;

	@Value("${admin.default.password:Admin@123}")
	private String defaultAdminPassword;

	@Override
	public void run(String... args) {
		if (userRepository.findByEmail(defaultAdminEmail) == null) {
			User admin = new User();
			admin.setName("System Admin");
			admin.setEmail(defaultAdminEmail);
			admin.setPassword(passwordEncoder.encode(defaultAdminPassword));
			admin.setPhone("9999999999");
			admin.setAddress("Head Office");
			admin.setRole("ADMIN");
			admin.setEnabled(true);
			userRepository.save(admin);
			System.out.println("==============================================================");
			System.out.println(" Default admin account created:");
			System.out.println("   email    : " + defaultAdminEmail);
			System.out.println("   password : " + defaultAdminPassword);
			System.out.println(" Please log in and change this password immediately.");
			System.out.println("==============================================================");
		}
	}
}
