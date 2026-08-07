package com.carrental.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.carrental.entity.Booking;
import com.carrental.entity.Car;
import com.carrental.entity.Payment;
import com.carrental.entity.User;
import com.carrental.repository.BookingRepository;
import com.carrental.repository.CarRepository;
import com.carrental.repository.PaymentRepository;
import com.carrental.repository.UserRepository;
import com.carrental.service.FileStorageService;
import com.carrental.service.UserService;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserRepository ur;

	@Autowired
	private CarRepository carRepository;

	@Autowired
	private BookingRepository bookingRepository;

	@Autowired
	private PaymentRepository paymentRepository;

	@Autowired
	private FileStorageService fileStorageService;

	@Autowired
	private UserService userService;

	private static final List<String> ACTIVE_STATUSES = List.of("PENDING", "APPROVED");

	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {

		if (principal != null) {
			String name = principal.getName();
			User user = ur.findByEmail(name);
			model.addAttribute("user", user);
		}
	}

	// ---------------------------------------------------------------- Dashboard

	@GetMapping("/dashboard")
	public String dashboardPage(Model model, Principal principal) {
		User user = ur.findByEmail(principal.getName());

		model.addAttribute("totalBookings", bookingRepository.findByUserOrderByBookingDateDesc(user).size());
		model.addAttribute("activeBookings", bookingRepository.countByUserAndStatusIn(user, ACTIVE_STATUSES));
		model.addAttribute("recentBookings", bookingRepository.findTop5ByUserOrderByBookingDateDesc(user));
		model.addAttribute("recommendedCars", carRepository.findTop6ByStatusOrderByCreatedAtDesc("AVAILABLE"));

		return "user/dashboard";
	}

	// ---------------------------------------------------------------- Cars

	@GetMapping("/cars")
	public String showCars(@RequestParam(required = false) String keyword,
			@RequestParam(required = false) String fuelType, @RequestParam(required = false) String transmission,
			@RequestParam(required = false) String carType, Model model) {

		String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
		String ft = (fuelType == null || fuelType.isBlank()) ? null : fuelType;
		String tr = (transmission == null || transmission.isBlank()) ? null : transmission;
		String ct = (carType == null || carType.isBlank()) ? null : carType;

		model.addAttribute("cars", carRepository.search(kw, ft, tr, ct, null));
		model.addAttribute("keyword", keyword);
		return "user/cars";
	}

	@GetMapping("/search-cars")
	public String searchCars(@RequestParam(required = false) String keyword) {
		return "redirect:/user/cars?keyword=" + (keyword == null ? "" : keyword);
	}

	@GetMapping("/book-car/{id}")
	public String bookCarPage(@PathVariable Long id, Model model, RedirectAttributes ra) {
		Car car = carRepository.findById(id).orElse(null);
		if (car == null) {
			ra.addFlashAttribute("error", "Car not found.");
			return "redirect:/user/cars";
		}
		if (!car.isAvailable()) {
			ra.addFlashAttribute("error", "This car is currently not available for booking.");
			return "redirect:/user/cars";
		}
		model.addAttribute("car", car);
		return "user/book_car";
	}

	@PostMapping("/book-car/{id}")
	public String bookCar(@PathVariable Long id, @RequestParam String pickupDate, @RequestParam String returnDate,
			Principal principal, RedirectAttributes ra) {

		Car car = carRepository.findById(id).orElse(null);
		if (car == null) {
			ra.addFlashAttribute("error", "Car not found.");
			return "redirect:/user/cars";
		}
		if (!car.isAvailable()) {
			ra.addFlashAttribute("error", "This car is currently not available for booking.");
			return "redirect:/user/cars";
		}

		LocalDate pickup = LocalDate.parse(pickupDate);
		LocalDate ret = LocalDate.parse(returnDate);

		if (!ret.isAfter(pickup)) {
			ra.addFlashAttribute("error", "Return date must be after the pickup date.");
			return "redirect:/user/book-car/" + id;
		}

		int days = (int) ChronoUnit.DAYS.between(pickup, ret);
		double amount = days * car.getPricePerDay();

		User user = ur.findByEmail(principal.getName());

		Booking booking = new Booking();
		booking.setCar(car);
		booking.setUser(user);
		booking.setPickupDate(pickup);
		booking.setReturnDate(ret);
		booking.setTotalDays(days);
		booking.setTotalAmount(amount);
		booking.setStatus("PENDING");
		booking.setBookingDate(LocalDateTime.now());
		bookingRepository.save(booking);

		Payment payment = new Payment();
		payment.setBooking(booking);
		payment.setAmount(amount);
		payment.setStatus("PENDING");
		payment.setCreatedAt(LocalDateTime.now());
		paymentRepository.save(payment);

		ra.addFlashAttribute("success",
				"Booking request submitted! We'll notify you once the admin approves it, then you can pay online.");
		return "redirect:/user/bookings";
	}

	// ---------------------------------------------------------------- Bookings

	@GetMapping("/bookings")
	public String showBookings(Model model, Principal principal) {
		User user = ur.findByEmail(principal.getName());
		model.addAttribute("bookings", bookingRepository.findByUserOrderByBookingDateDesc(user));
		return "user/bookings";
	}

	@GetMapping("/booking/{id}")
	public String viewBooking(@PathVariable Long id, Model model, Principal principal, RedirectAttributes ra) {
		Booking booking = bookingRepository.findById(id).orElse(null);
		User user = ur.findByEmail(principal.getName());
		if (booking == null || !booking.getUser().getId().equals(user.getId())) {
			ra.addFlashAttribute("error", "Booking not found.");
			return "redirect:/user/bookings";
		}
		model.addAttribute("booking", booking);
		model.addAttribute("payment", paymentRepository.findByBooking(booking));
		return "user/view_booking";
	}

	@GetMapping("/cancel-booking/{id}")
	public String cancelBooking(@PathVariable Long id, Principal principal, RedirectAttributes ra) {
		Booking booking = bookingRepository.findById(id).orElse(null);
		User user = ur.findByEmail(principal.getName());

		if (booking == null || !booking.getUser().getId().equals(user.getId())) {
			ra.addFlashAttribute("error", "Booking not found.");
			return "redirect:/user/bookings";
		}
		if ("COMPLETED".equals(booking.getStatus()) || "CANCELLED".equals(booking.getStatus())
				|| "REJECTED".equals(booking.getStatus())) {
			ra.addFlashAttribute("error", "This booking can no longer be cancelled.");
			return "redirect:/user/bookings";
		}

		booking.setStatus("CANCELLED");
		bookingRepository.save(booking);

		Payment payment = paymentRepository.findByBooking(booking);
		if (payment != null && "PENDING".equals(payment.getStatus())) {
			payment.setStatus("CANCELLED");
			paymentRepository.save(payment);
		}

		Car car = booking.getCar();
		if (car != null && "BOOKED".equals(car.getStatus())) {
			car.setStatus("AVAILABLE");
			carRepository.save(car);
		}

		ra.addFlashAttribute("success", "Booking cancelled.");
		return "redirect:/user/bookings";
	}

	// ---------------------------------------------------------------- Payments

	@GetMapping("/payments")
	public String showpayments(Model model, Principal principal) {
		User user = ur.findByEmail(principal.getName());

		Double paid = paymentRepository.totalPaidByUser(user);
		Double pending = paymentRepository.totalPendingByUser(user);

		model.addAttribute("totalPaid", paid == null ? 0 : paid);
		model.addAttribute("pendingAmount", pending == null ? 0 : pending);
		model.addAttribute("totalTransactions", paymentRepository.countByBookingUser(user));
		model.addAttribute("payments", paymentRepository.findByBookingUserOrderByCreatedAtDesc(user));

		return "user/payments";
	}

	@GetMapping("/payment/{id}")
	public String viewPayment(@PathVariable Long id, Model model, Principal principal, RedirectAttributes ra) {
		Payment payment = paymentRepository.findById(id).orElse(null);
		User user = ur.findByEmail(principal.getName());

		if (payment == null || !payment.getBooking().getUser().getId().equals(user.getId())) {
			ra.addFlashAttribute("error", "Payment not found.");
			return "redirect:/user/payments";
		}
		model.addAttribute("payment", payment);
		return "user/view_payment";
	}

	@GetMapping("/pay-now/{bookingId}")
	public String payNowPage(@PathVariable Long bookingId, Model model, Principal principal, RedirectAttributes ra) {
		Booking booking = bookingRepository.findById(bookingId).orElse(null);
		User user = ur.findByEmail(principal.getName());

		if (booking == null || !booking.getUser().getId().equals(user.getId())) {
			ra.addFlashAttribute("error", "Booking not found.");
			return "redirect:/user/bookings";
		}
		if (!"APPROVED".equals(booking.getStatus())) {
			ra.addFlashAttribute("error", "This booking is not yet approved for payment.");
			return "redirect:/user/bookings";
		}
		Payment payment = paymentRepository.findByBooking(booking);
		if (payment == null || !"PENDING".equals(payment.getStatus())) {
			ra.addFlashAttribute("error", "There is nothing pending to pay for this booking.");
			return "redirect:/user/payments";
		}

		model.addAttribute("booking", booking);
		model.addAttribute("payment", payment);
		return "user/pay_now";
	}

	@PostMapping("/pay-now/{bookingId}")
	public String processPayment(@PathVariable Long bookingId, @RequestParam String method, Principal principal,
			RedirectAttributes ra) {

		Booking booking = bookingRepository.findById(bookingId).orElse(null);
		User user = ur.findByEmail(principal.getName());

		if (booking == null || !booking.getUser().getId().equals(user.getId())) {
			ra.addFlashAttribute("error", "Booking not found.");
			return "redirect:/user/bookings";
		}
		Payment payment = paymentRepository.findByBooking(booking);
		if (payment == null || !"PENDING".equals(payment.getStatus())) {
			ra.addFlashAttribute("error", "There is nothing pending to pay for this booking.");
			return "redirect:/user/payments";
		}

		// Simulated payment gateway: no real card/UPI network is contacted. This models
		// the interface & data flow of a payment provider (method selection, generated
		// transaction id, PAID/FAILED status) without transmitting funds.
		payment.setMethod(method);
		payment.setTransactionId("TXN" + UUID.randomUUID().toString().replace("-", "").substring(0, 14).toUpperCase());
		payment.setStatus("PAID");
		payment.setPaymentDate(LocalDateTime.now());
		paymentRepository.save(payment);

		Car car = booking.getCar();
		if (car != null) {
			car.setStatus("BOOKED");
			carRepository.save(car);
		}

		ra.addFlashAttribute("success", "Payment successful! Your booking is confirmed.");
		return "redirect:/user/receipt/" + bookingId;
	}

	@GetMapping("/receipt/{bookingId}")
	public String viewReceipt(@PathVariable Long bookingId, Model model, Principal principal, RedirectAttributes ra) {
		Booking booking = bookingRepository.findById(bookingId).orElse(null);
		User user = ur.findByEmail(principal.getName());

		if (booking == null || !booking.getUser().getId().equals(user.getId())) {
			ra.addFlashAttribute("error", "Booking not found.");
			return "redirect:/user/bookings";
		}
		Payment payment = paymentRepository.findByBooking(booking);
		model.addAttribute("booking", booking);
		model.addAttribute("payment", payment);
		model.addAttribute("backUrl", "/user/payments");
		return "receipt";
	}

	// ---------------------------------------------------------------- Profile

	@GetMapping("/profile")
	public String userProfile(Model model, Principal principal) {
		model.addAttribute("user", ur.findByEmail(principal.getName()));
		return "user/profile";
	}

	@PostMapping("/update-profile")
	public String updateProfile(@ModelAttribute User user, @RequestParam(required = false) MultipartFile imageFile,
			Principal principal, RedirectAttributes ra) {

		User existing = ur.findByEmail(principal.getName());
		if (existing == null) {
			return "redirect:/user/profile";
		}
		existing.setName(user.getName());
		existing.setPhone(user.getPhone());
		existing.setAddress(user.getAddress());
		if (imageFile != null && !imageFile.isEmpty()) {
			existing.setProfileImage(fileStorageService.store(imageFile, ""));
		}
		ur.save(existing);
		ra.addFlashAttribute("success", "Profile updated successfully.");
		return "redirect:/user/profile";
	}

	@GetMapping("/change-password")
	public String changepassword() {
		return "user/change_password";
	}

	@PostMapping("/change-password")
	public String changePassword(@RequestParam String oldPassword, @RequestParam String newPassword,
			@RequestParam String confirmPassword, Principal principal, RedirectAttributes ra) {

		if (!newPassword.equals(confirmPassword)) {
			ra.addFlashAttribute("error", "New password and confirm password do not match.");
			return "redirect:/user/change-password";
		}
		boolean ok = userService.changePasswordWithVerification(principal.getName(), oldPassword, newPassword);
		if (!ok) {
			ra.addFlashAttribute("error", "Old password is incorrect.");
			return "redirect:/user/change-password";
		}
		ra.addFlashAttribute("success", "Password changed successfully.");
		return "redirect:/user/profile";
	}
}
