package com.carrental.controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

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
@RequestMapping("/admin")
public class AdminController {

	@Autowired
	private UserRepository userRepository;

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

	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		if (principal != null) {
			User admin = userRepository.findByEmail(principal.getName());
			model.addAttribute("admin", admin);
		}
	}

	// ---------------------------------------------------------------- Dashboard

	@GetMapping("/dashboard")
	public String dashboardPage(Model model) {
		model.addAttribute("pageTitle", "Admin Dashboard");
		model.addAttribute("pageSubtitle", "Manage cars, users, bookings, payments and reports.");

		model.addAttribute("totalUsers", userRepository.countByRole("ROLE_USER"));
		model.addAttribute("totalCars", carRepository.count());
		model.addAttribute("totalBookings", bookingRepository.count());

		Double revenue = bookingRepository.totalRevenue();
		model.addAttribute("totalRevenue", revenue == null ? 0 : revenue);

		model.addAttribute("recentBookings", bookingRepository.findTop5ByOrderByBookingDateDesc());
		model.addAttribute("latestCars", carRepository.findTop5ByOrderByCreatedAtDesc());
		
	

		return "admin/dashboard";
	}

	// ---------------------------------------------------------------- Cars

	@GetMapping("/cars")
	public String manageCars(@RequestParam(required = false) String keyword,
			@RequestParam(required = false) String fuelType, @RequestParam(required = false) String transmission,
			@RequestParam(required = false) String status, Model model) {

		String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
		String ft = (fuelType == null || fuelType.isBlank()) ? null : fuelType;
		String tr = (transmission == null || transmission.isBlank()) ? null : transmission;
		String st = (status == null || status.isBlank()) ? null : status;

		List<Car> cars = carRepository.search(kw, ft, tr, null, st);

		model.addAttribute("cars", cars);
		model.addAttribute("keyword", keyword);
		model.addAttribute("pageTitle", "Manage Cars");
		model.addAttribute("pageSubtitle", "View, edit, or remove vehicles from the fleet.");
		return "admin/manage_cars";
	}

	@GetMapping("/cars/add")
	public String addCarPage(Model model) {
		Car car = new Car();
		car.setStatus("AVAILABLE");
		model.addAttribute("car", car);
		model.addAttribute("pageTitle", "Add New Car");
		return "admin/add_car";
	}

	@GetMapping("/cars/edit/{id}")
	public String editCarPage(@PathVariable Long id, Model model, RedirectAttributes ra) {
		Car car = carRepository.findById(id).orElse(null);
		if (car == null) {
			ra.addFlashAttribute("error", "Car not found.");
			return "redirect:/admin/cars";
		}
		model.addAttribute("car", car);
		model.addAttribute("pageTitle", "Edit Car");
		return "admin/add_car";
	}

	@PostMapping("/cars/save")
	public String saveCar(@ModelAttribute Car car, @RequestParam(required = false) MultipartFile imageFile,
			Principal principal, RedirectAttributes ra) {

		if (car.getId() != null) {
			Car existing = carRepository.findById(car.getId()).orElse(null);
			if (existing != null) {
				existing.setName(car.getName());
				existing.setBrand(car.getBrand());
				existing.setModel(car.getModel());
				existing.setCarType(car.getCarType());
				existing.setRegistrationNumber(car.getRegistrationNumber());
				existing.setFuelType(car.getFuelType());
				existing.setTransmission(car.getTransmission());
				existing.setSeats(car.getSeats());
				existing.setPricePerDay(car.getPricePerDay());
				existing.setStatus(car.getStatus());
				existing.setDescription(car.getDescription());
				if (imageFile != null && !imageFile.isEmpty()) {
					existing.setImage(fileStorageService.store(imageFile, "cars"));
				}
				carRepository.save(existing);
				ra.addFlashAttribute("success", "Car updated successfully.");
				return "redirect:/admin/cars";
			}
		}

		if (imageFile != null && !imageFile.isEmpty()) {
			car.setImage(fileStorageService.store(imageFile, "cars"));
		}
		if (car.getStatus() == null || car.getStatus().isBlank()) {
			car.setStatus("AVAILABLE");
		}
		if (principal != null) {
			car.setAddedBy(userRepository.findByEmail(principal.getName()));
		}
		car.setId(null);
		carRepository.save(car);
		ra.addFlashAttribute("success", "Car added successfully.");
		return "redirect:/admin/cars";
	}

	@GetMapping("/cars/view/{id}")
	public String viewCar(@PathVariable Long id, Model model, RedirectAttributes ra) {
		Car car = carRepository.findById(id).orElse(null);
		if (car == null) {
			ra.addFlashAttribute("error", "Car not found.");
			return "redirect:/admin/cars";
		}
		model.addAttribute("car", car);
		model.addAttribute("pageTitle", "Car Details");
		return "admin/view_car";
	}

	@GetMapping("/cars/delete/{id}")
	public String deleteCar(@PathVariable Long id, RedirectAttributes ra) {
		Car car = carRepository.findById(id).orElse(null);
		if (car == null) {
			ra.addFlashAttribute("error", "Car not found.");
			return "redirect:/admin/cars";
		}
		if (bookingRepository.existsByCar(car)) {
			ra.addFlashAttribute("error", "Cannot delete a car that has bookings. Set it to MAINTENANCE instead.");
			return "redirect:/admin/cars";
		}
		carRepository.delete(car);
		ra.addFlashAttribute("success", "Car deleted successfully.");
		return "redirect:/admin/cars";
	}

	// ---------------------------------------------------------------- Bookings

	@GetMapping("/bookings")
	public String bookings(Model model) {
		model.addAttribute("bookings", bookingRepository.findAllByOrderByBookingDateDesc());
		model.addAttribute("pageTitle", "Manage Bookings");
		return "admin/bookings";
	}

	@GetMapping("/bookings/view/{id}")
	public String viewBooking(@PathVariable Long id, Model model, RedirectAttributes ra) {
		Booking booking = bookingRepository.findById(id).orElse(null);
		if (booking == null) {
			ra.addFlashAttribute("error", "Booking not found.");
			return "redirect:/admin/bookings";
		}
		model.addAttribute("booking", booking);
		model.addAttribute("payment", paymentRepository.findByBooking(booking));
		model.addAttribute("pageTitle", "Booking Details");
		return "admin/view_booking";
	}

	@PostMapping("/bookings/approve/{id}")
	public String approveBooking(@PathVariable Long id, RedirectAttributes ra) {
		Booking booking = bookingRepository.findById(id).orElse(null);
		if (booking != null) {
			booking.setStatus("APPROVED");
			bookingRepository.save(booking);
			ra.addFlashAttribute("success", "Booking approved. The customer can now pay for it.");
		}
		return "redirect:/admin/bookings";
	}

	@PostMapping("/bookings/reject/{id}")
	public String rejectBooking(@PathVariable Long id, RedirectAttributes ra) {
		Booking booking = bookingRepository.findById(id).orElse(null);
		if (booking != null) {
			booking.setStatus("REJECTED");
			bookingRepository.save(booking);

			Payment payment = paymentRepository.findByBooking(booking);
			if (payment != null && "PENDING".equals(payment.getStatus())) {
				payment.setStatus("FAILED");
				paymentRepository.save(payment);
			}
			ra.addFlashAttribute("success", "Booking rejected.");
		}
		return "redirect:/admin/bookings";
	}

	@PostMapping("/bookings/complete/{id}")
	public String completeBooking(@PathVariable Long id, RedirectAttributes ra) {
		Booking booking = bookingRepository.findById(id).orElse(null);
		if (booking != null) {
			booking.setStatus("COMPLETED");
			bookingRepository.save(booking);

			Car car = booking.getCar();
			if (car != null) {
				car.setStatus("AVAILABLE");
				carRepository.save(car);
			}
			ra.addFlashAttribute("success", "Booking marked as completed.");
		}
		return "redirect:/admin/bookings";
	}

	// ---------------------------------------------------------------- Users

	@GetMapping("/users")
	public String users(@RequestParam(required = false) String keyword, @RequestParam(required = false) String role,
			@RequestParam(required = false) Boolean enabled, Model model) {

		String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
		String rl = (role == null || role.isBlank()) ? null : role;

		model.addAttribute("users", userRepository.search(kw, rl, enabled));
		model.addAttribute("keyword", keyword);
		model.addAttribute("pageTitle", "Manage Users");
		return "admin/users";
	}

	@GetMapping("/users/view/{id}")
	public String viewUser(@PathVariable Long id, Model model, RedirectAttributes ra) {
		User user = userRepository.findById(id).orElse(null);
		if (user == null) {
			ra.addFlashAttribute("error", "User not found.");
			return "redirect:/admin/users";
		}
		model.addAttribute("viewedUser", user);
		model.addAttribute("userBookings", bookingRepository.findByUserOrderByBookingDateDesc(user));
		model.addAttribute("pageTitle", "User Details");
		return "admin/view_user";
	}

	@PostMapping("/users/enable/{id}")
	public String enableUser(@PathVariable Long id, RedirectAttributes ra) {
		User user = userRepository.findById(id).orElse(null);
		if (user != null) {
			user.setEnabled(true);
			userRepository.save(user);
			ra.addFlashAttribute("success", "User account enabled.");
		}
		return "redirect:/admin/users";
	}

	@PostMapping("/users/disable/{id}")
	public String disableUser(@PathVariable Long id, RedirectAttributes ra) {
		User user = userRepository.findById(id).orElse(null);
		if (user != null) {
			user.setEnabled(false);
			userRepository.save(user);
			ra.addFlashAttribute("success", "User account disabled.");
		}
		return "redirect:/admin/users";
	}

	@GetMapping("/users/delete/{id}")
	public String deleteUser(@PathVariable Long id, RedirectAttributes ra) {
		User user = userRepository.findById(id).orElse(null);
		if (user == null) {
			ra.addFlashAttribute("error", "User not found.");
			return "redirect:/admin/users";
		}
		List<Booking> userBookings = bookingRepository.findByUserOrderByBookingDateDesc(user);
		for (Booking b : userBookings) {
			Payment p = paymentRepository.findByBooking(b);
			if (p != null) {
				paymentRepository.delete(p);
			}
		}
		bookingRepository.deleteAll(userBookings);
		userRepository.delete(user);
		ra.addFlashAttribute("success", "User deleted successfully.");
		return "redirect:/admin/users";
	}

	// ---------------------------------------------------------------- Payments

	@GetMapping("/payments")
	public String payments(Model model) {
		model.addAttribute("payments", paymentRepository.findAllByOrderByCreatedAtDesc());
		model.addAttribute("pageTitle", "Manage Payments");
		return "admin/payments";
	}

	@GetMapping("/payments/view/{id}")
	public String viewPayment(@PathVariable Long id, Model model, RedirectAttributes ra) {
		Payment payment = paymentRepository.findById(id).orElse(null);
		if (payment == null) {
			ra.addFlashAttribute("error", "Payment not found.");
			return "redirect:/admin/payments";
		}
		model.addAttribute("payment", payment);
		model.addAttribute("pageTitle", "Payment Details");
		return "admin/view_payment";
	}

	@GetMapping("/receipts/view/{bookingId}")
	public String viewReceipt(@PathVariable Long bookingId, Model model, RedirectAttributes ra) {
		Booking booking = bookingRepository.findById(bookingId).orElse(null);
		if (booking == null) {
			ra.addFlashAttribute("error", "Booking not found.");
			return "redirect:/admin/payments";
		}
		Payment payment = paymentRepository.findByBooking(booking);
		model.addAttribute("booking", booking);
		model.addAttribute("payment", payment);
		model.addAttribute("backUrl", "/admin/payments");
		return "receipt";
	}

	// ---------------------------------------------------------------- Search (topbar)

	@GetMapping("/search")
	public String search(@RequestParam(required = false) String keyword) {
		return "redirect:/admin/cars?keyword=" + (keyword == null ? "" : keyword);
	}

	// ---------------------------------------------------------------- Profile


	
	
	@GetMapping("/profile")
	public String profile(Model model,Principal principal) {
		model.addAttribute("admin", userRepository.findByEmail(principal.getName()));
	    model.addAttribute("pageTitle", "Admin Profile");
	    model.addAttribute("pageSubtitle",
	            "Update your admin account information and profile picture.");

	    return "admin/profile";
	}

	@PostMapping("/profile/update")
	public String updateProfile(@ModelAttribute User admin, @RequestParam(required = false) MultipartFile imageFile,
			Principal principal, RedirectAttributes ra) {

		User existing = userRepository.findByEmail(principal.getName());
		if (existing == null) {
			return "redirect:/admin/profile";
		}
		existing.setName(admin.getName());
		existing.setPhone(admin.getPhone());
		existing.setAddress(admin.getAddress());
		if (imageFile != null && !imageFile.isEmpty()) {
			existing.setProfileImage(fileStorageService.store(imageFile, ""));
		}
		userRepository.save(existing);
		ra.addFlashAttribute("success", "Profile updated successfully.");
		return "redirect:/admin/profile";
	}


	
	
	@GetMapping("/change-password")
	public String changePassword(Model model) {

	    model.addAttribute("pageTitle", "Change Password");
	    model.addAttribute("pageSubtitle",
	            "Update your account password securely.");

	    return "admin/change_password";
	}

	@PostMapping("/change-password")
	public String changePassword(@RequestParam String oldPassword, @RequestParam String newPassword,
			@RequestParam String confirmPassword, Principal principal, RedirectAttributes ra) {

		if (!newPassword.equals(confirmPassword)) {
			ra.addFlashAttribute("error", "New password and confirm password do not match.");
			return "redirect:/admin/change-password";
		}
		boolean ok = userService.changePasswordWithVerification(principal.getName(), oldPassword, newPassword);
		if (!ok) {
			ra.addFlashAttribute("error", "Old password is incorrect.");
			return "redirect:/admin/change-password";
		}
		ra.addFlashAttribute("success", "Password changed successfully.");
		return "redirect:/admin/profile";
	}
}
