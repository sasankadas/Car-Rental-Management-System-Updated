package com.carrental.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.carrental.entity.Booking;
import com.carrental.entity.Payment;
import com.carrental.entity.User;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

	List<Payment> findAllByOrderByCreatedAtDesc();

	List<Payment> findByBookingUserOrderByCreatedAtDesc(User user);

	Payment findByBooking(Booking booking);

	@Query("select coalesce(sum(p.amount),0) from Payment p where p.booking.user = :user and p.status = 'PAID'")
	Double totalPaidByUser(User user);

	@Query("select coalesce(sum(p.amount),0) from Payment p where p.booking.user = :user and p.status = 'PENDING'")
	Double totalPendingByUser(User user);

	long countByBookingUser(User user);
}
