package com.carrental.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.carrental.entity.User;
import com.carrental.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
public class AuthController {

	@Autowired
	private UserService userService;

	// Actual authentication (POST /login) is handled entirely by Spring Security's
	// formLogin filter configured in SecurityConfig - see loginProcessingUrl("/login").
	@GetMapping("/login")
	public String loginPage(@RequestParam(value = "error", required = false) String error,
			@RequestParam(value = "logout", required = false) String logout, Model model) {

		if ("disabled".equals(error)) {
			model.addAttribute("error", "Please verify your email before logging in.");
		} else if (error != null) {
			model.addAttribute("error", "Invalid email or password.");
		}
		if (logout != null) {
			model.addAttribute("success", "You have been logged out successfully.");
		}
		return "auth/login";
	}

	@GetMapping("/access-denied")
	public String accessDenied(Model model) {
		model.addAttribute("error", "You do not have permission to access that page.");
		return "auth/login";
	}

	@GetMapping("/register")
	public String registerPage(Model m) {
		m.addAttribute("user", new User());

		return "auth/signup";
	}

	@PostMapping("/register")
	public String register(@ModelAttribute User user, RedirectAttributes ra) {

		String result = userService.registerUser(user);

		if (result.equals("UNVERIFIED")) {
			ra.addFlashAttribute("success", "Account already exists but is not verified. Verification link resent.");
			return "redirect:/check-email";
		}
		if (result.equals("EXISTS")) {
			ra.addFlashAttribute("error", "Account already registered. Please login.");
			return "redirect:/login";
		}

		ra.addFlashAttribute("success", "Registration successful! Please check your email to verify your account.");

		return "redirect:/check-email";
	}

	@GetMapping("/check-email")
	public String checkEmail() {
		return "auth/check_mail";
	}

	@GetMapping("/verify-email")
	public String verifyEmail(@RequestParam("token") String token, RedirectAttributes ra) {
		boolean status = userService.verifyEmail(token);

		if (status) {
			ra.addFlashAttribute("success", "Email verified successfully! Please login.");
			return "redirect:/login";
		} else {
			ra.addFlashAttribute("error", "Invalid or expired link.");
			return "redirect:/register";
		}

	}

	@GetMapping("/forgot-password")
	public String optPage() {
		return "auth/forgot";
	}

	@PostMapping("/forgot-password")
	public String forgotPage(@RequestParam("username") String username, RedirectAttributes ra, HttpSession session) {
		userService.sendOtp(username);
		ra.addFlashAttribute("success", "OTP sent to your email.");
		session.setAttribute("username", username);
		return "redirect:/verify-otp?email=" + username;
	}

	@GetMapping("/verify-otp")
	public String verifyOtpPage(@RequestParam("email") String email, Model model) {
		model.addAttribute("email", email);
		return "auth/otp_verify";
	}

	@PostMapping("/verify-reset-otp")
	public String verifyotp(@RequestParam("otp") String otp, RedirectAttributes ra) {
		boolean status = userService.verifyOtp(otp);
		if (status) {
			ra.addFlashAttribute("succuss", "OTP Verified..Enter New password");
			return "redirect:/change-password";
		} else {
			ra.addFlashAttribute("error", "Invalid OTP or expired OTP.");
			return "redirect:/verify-otp";
		}
	}

	@GetMapping("/change-password")
	public String changePasswordPage() {
		return "auth/change_password";
	}

	@PostMapping("/change-password")
	public String changePassword(@RequestParam("newPassword") String password, RedirectAttributes ra,
			HttpSession session) {
		String username = (String) session.getAttribute("username");

		userService.changePassword(username, password);

		ra.addFlashAttribute("success", "Password changed successfully. Please login.");
		return "redirect:/login";
	}

}
