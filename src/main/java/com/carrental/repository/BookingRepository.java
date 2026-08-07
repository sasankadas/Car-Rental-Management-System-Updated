package com.carrental.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.carrental.entity.Booking;
import com.carrental.entity.Car;
import com.carrental.entity.User;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

	List<Booking> findByUserOrderByBookingDateDesc(User user);

	List<Booking> findTop5ByUserOrderByBookingDateDesc(User user);

	List<Booking> findTop5ByOrderByBookingDateDesc();

	List<Booking> findAllByOrderByBookingDateDesc();

	long countByUserAndStatusIn(User user, List<String> statuses);

	long countByStatus(String status);

	boolean existsByCar(Car car);

	@Query("select coalesce(sum(b.totalAmount),0) from Booking b where b.status <> 'REJECTED' and b.status <> 'CANCELLED'")
	Double totalRevenue();
}
