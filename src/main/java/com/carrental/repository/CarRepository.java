package com.carrental.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.carrental.entity.Car;

@Repository
public interface CarRepository extends JpaRepository<Car, Long> {

	List<Car> findTop5ByOrderByCreatedAtDesc();

	List<Car> findTop6ByStatusOrderByCreatedAtDesc(String status);

	List<Car> findByStatus(String status);

	long countByStatus(String status);

	@Query("select c from Car c where " +
			"(:keyword is null or lower(c.name) like lower(concat('%',:keyword,'%')) " +
			"  or lower(c.brand) like lower(concat('%',:keyword,'%')) " +
			"  or lower(c.model) like lower(concat('%',:keyword,'%'))) " +
			"and (:fuelType is null or c.fuelType = :fuelType) " +
			"and (:transmission is null or c.transmission = :transmission) " +
			"and (:carType is null or c.carType = :carType) " +
			"and (:status is null or c.status = :status) " +
			"order by c.createdAt desc")
	List<Car> search(@Param("keyword") String keyword,
			@Param("fuelType") String fuelType,
			@Param("transmission") String transmission,
			@Param("carType") String carType,
			@Param("status") String status);
}
