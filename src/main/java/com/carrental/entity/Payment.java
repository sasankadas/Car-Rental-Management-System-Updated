package com.carrental.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;

@Entity
@Table(name = "payment")
@Data
public class Payment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "booking_id")
	private Booking booking;

	private double amount;

	/** CARD, UPI, NET_BANKING, CASH */
	private String method;

	private String transactionId;

	/** PENDING, PAID, FAILED, CANCELLED */
	private String status;

	private LocalDateTime paymentDate;

	private LocalDateTime createdAt;

	@Transient
	public String getPaymentId() {
		return id != null ? String.format("PAY%05d", id) : null;
	}
}
