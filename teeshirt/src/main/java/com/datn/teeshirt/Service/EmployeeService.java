package com.datn.teeshirt.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.datn.teeshirt.DTO.EmployeeDTO;
import com.datn.teeshirt.Entity.Employee;
import com.datn.teeshirt.Repository.EmployeeRepository;

import jakarta.transaction.Transactional;

@Service
public class EmployeeService {
    @Autowired
    private EmployeeRepository employeeRepository;

    public Optional<Employee> findByEmail(String email) {
        return employeeRepository.findByEmail(email);
    }

    // Get all employees
    public List<EmployeeDTO> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .map(EmployeeDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // Get employee by ID
    public EmployeeDTO getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));
        return EmployeeDTO.fromEntity(employee);
    }

    // Create new employee
    @Transactional
    public EmployeeDTO createEmployee(EmployeeDTO employeeDTO) {
        validateEmployee(employeeDTO);
        Employee employee = employeeDTO.toEntity();
        Employee savedEmployee = employeeRepository.save(employee);
        return EmployeeDTO.fromEntity(savedEmployee);
    }

    // Update existing employee
    @Transactional
    public EmployeeDTO updateEmployee(Long id, EmployeeDTO employeeDTO) {
        employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));

        validateEmployee(employeeDTO, id);

        Employee employee = employeeDTO.toEntity();
        employee.setEmployeeId(id);
        Employee updatedEmployee = employeeRepository.save(employee);
        return EmployeeDTO.fromEntity(updatedEmployee);
    }

    // Delete employee (soft delete by updating status to INACTIVE)
    @Transactional
    public void setEmployeeInactive(Long id) {
        Employee employee = employeeRepository.findById(id).orElseThrow();
        employee.setStatus("INACTIVE");
        employeeRepository.save(employee);
    }

    // Search employees
    public List<EmployeeDTO> searchEmployees(String searchTerm) {
        return employeeRepository.searchEmployees(searchTerm).stream()
                .map(EmployeeDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // Filter employees by status
    public List<EmployeeDTO> filterByStatus(String status) {
        return employeeRepository.findByStatus(status).stream()
                .map(EmployeeDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // Filter employees by birthday range
    public List<EmployeeDTO> filterByBirthdayRange(LocalDate startDate, LocalDate endDate) {
        return employeeRepository.findByBirthdayBetween(startDate, endDate).stream()
                .map(EmployeeDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // Validate employee data
    private void validateEmployee(EmployeeDTO employeeDTO, Long... id) {
        if (employeeDTO.getCitizenId() != null) {
            Employee existingByCitizenId = employeeRepository.findByCitizenId(employeeDTO.getCitizenId());
            if (existingByCitizenId != null && (id.length == 0 || !existingByCitizenId.getEmployeeId().equals(id[0]))) {
                throw new IllegalArgumentException("Citizen ID already exists");
            }
        }
        if (employeeDTO.getEmail() != null) {
            employeeRepository.findByEmail(employeeDTO.getEmail())
                    .ifPresent(existing -> {
                        if (id.length == 0 || !existing.getEmployeeId().equals(id[0])) {
                            throw new IllegalArgumentException("Email already exists");
                        }
                    });
        }
    }
}