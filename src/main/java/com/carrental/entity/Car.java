package com.carrental.entity;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "car")
@Data
public class Car {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	private String brand;

	private String model;

	private String carType; // Sedan, SUV, Hatchback, Luxury, Electric

	private String registrationNumber;

	private String fuelType; // Petrol, Diesel, Electric, CNG

	private String transmission; // Manual, Automatic

	private int seats;

	private double pricePerDay;

	private String color;

	private String image;

	private String status; // AVAILABLE, BOOKED, MAINTENANCE

	private String description;

	@CreationTimestamp
	private LocalDateTime createdAt;

	@UpdateTimestamp
	private LocalDateTime updatedAt;

	@ManyToOne
	@JoinColumn(name = "added_by_id")
	private User addedBy;

	@OneToMany(mappedBy = "car", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Booking> bookings;

	/**
	 * Convenience computed property used by the Thymeleaf views (${car.available}).
	 */
	public boolean isAvailable() {
		return "AVAILABLE".equalsIgnoreCase(status);
	}
}
