package com.carrental.serviceImpl;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.carrental.controller.HomeController;
import com.carrental.entity.PasswordResetOtp;
import com.carrental.entity.User;
import com.carrental.repository.PasswordResetOtpRepository;
import com.carrental.repository.UserRepository;
import com.carrental.service.UserService;

@Service
public class UserServiceImpl implements UserService {

    private final HomeController homeController;



	@Autowired
	private EmailServiceImpl emailService;

	@Autowired
	private PasswordResetOtpRepository otpRepo;

	@Autowired
	private UserRepository ur;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Value("${app.base-url:http://localhost:8080}")
	private String baseUrl;


    UserServiceImpl(HomeController homeController) {
        this.homeController = homeController;
    }



	@Override
	public String registerUser(User user) {

		User existingUser = ur.findByEmail(user.getEmail());
		if (existingUser != null) {
			if (!existingUser.isEnabled()) {
				String token = UUID.randomUUID().toString();
				existingUser.setVerificationToken(token);
				existingUser.setTokenExpiryTime(LocalDateTime.now().plusMinutes(15));
				// allow the user to reset the password they typed while re-registering
				existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
				existingUser.setName(user.getName());
				existingUser.setPhone(user.getPhone());
				existingUser.setAddress(user.getAddress());
				ur.save(existingUser);
				String link = baseUrl + "/verify-email?token=" + token;
				emailService.sendVerificationMail(user.getEmail(), link, user.getName());
				return "UNVERIFIED";
			}
			return "EXISTS";
		}
		String token = UUID.randomUUID().toString();
		user.setEnabled(false);
		user.setRole("USER");
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		user.setVerificationToken(token);
		user.setTokenExpiryTime(LocalDateTime.now().plusMinutes(15));
		ur.save(user);
		String link = baseUrl + "/verify-email?token=" + token;
		emailService.sendVerificationMail(user.getEmail(), link, user.getName());
		return "CREATED";

	}

	@Override
	public boolean verifyEmail(String token) {
		User user = ur.findByVerificationToken(token);
		if (user == null) {
			return false;
		}
		if (LocalDateTime.now().isAfter(user.getTokenExpiryTime())) {
			return false;
		}
		user.setEnabled(true);
		user.setRole("USER");
		user.setVerificationToken(null);
		user.setTokenExpiryTime(null);
		ur.save(user);
		return true;
	}

	@Override
	public User findByEmail(String email) {
		User u = ur.findByEmail(email);
		return u;
	}

	@Override
	public void sendOtp(String email) {
		String otp = String.valueOf((int) (Math.random() * 900000) + 100000);
		PasswordResetOtp resetOtp = new PasswordResetOtp();
		resetOtp.setEmail(email);
		resetOtp.setOtp(otp);
		resetOtp.setExpiryTime(LocalDateTime.now().plusMinutes(5));
		resetOtp.setUsed(false);
		otpRepo.save(resetOtp);
		emailService.sendOtpMail(email, otp);
		System.out.println(otp);

	}

	@Override
	public boolean verifyOtp(String otp) {
		PasswordResetOtp resetOtp = otpRepo.findByOtpAndUsedFalse(otp);
		if (resetOtp == null || LocalDateTime.now().isAfter(resetOtp.getExpiryTime())) {
			return false;
		}
		resetOtp.setUsed(true);
		otpRepo.save(resetOtp);

		return true;
	}

	@Override
	public void changePassword(String username,String password) {
		User user=ur.findByEmail(username);
		if (user == null) {
			return;
		}
		user.setPassword(passwordEncoder.encode(password));
		ur.save(user);

	}

	@Override
	public boolean changePasswordWithVerification(String email, String oldPassword, String newPassword) {
		User user = ur.findByEmail(email);
		if (user == null || !passwordEncoder.matches(oldPassword, user.getPassword())) {
			return false;
		}
		user.setPassword(passwordEncoder.encode(newPassword));
		ur.save(user);
		return true;
	}

}
