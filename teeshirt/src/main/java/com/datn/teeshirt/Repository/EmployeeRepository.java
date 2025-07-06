package com.datn.teeshirt.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.datn.teeshirt.Entity.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByEmail(String email);

    // Lấy tất cả nhân viên
    @Query("SELECT e FROM Employee e")
    List<Employee> findAllEmployees();

    // Find employee by citizen ID
    Employee findByCitizenId(String citizenId);

    // Search employees by name, phone, or citizen ID
    @Query("SELECT e FROM Employee e WHERE " +
            "LOWER(e.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "e.phoneNumber LIKE CONCAT('%', :searchTerm, '%') OR " +
            "e.citizenId LIKE CONCAT('%', :searchTerm, '%')")
    List<Employee> searchEmployees(@Param("searchTerm") String searchTerm);

    // Filter employees by status
    List<Employee> findByStatus(String status);

    // Filter employees by birth date range
    @Query("SELECT e FROM Employee e WHERE e.birthday BETWEEN :startDate AND :endDate")
    List<Employee> findByBirthdayBetween(@Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}