package com.datn.teeshirt.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.datn.teeshirt.DTO.EmployeeDTO;
import com.datn.teeshirt.Entity.Employee;
import com.datn.teeshirt.Repository.EmployeeRepository;

import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

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
        Employee existingEmployee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));

        // Lấy user hiện tại
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentEmail = null;
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            currentEmail = ((UserDetails) authentication.getPrincipal()).getUsername();
        }
        if (currentEmail == null) {
            throw new RuntimeException("Không xác định được người dùng hiện tại");
        }
        Employee currentUser = employeeRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin người dùng hiện tại"));

        // Nếu target là quản lý và không phải chính mình => cấm cập nhật
        if (existingEmployee.getRole() == true && !existingEmployee.getEmployeeId().equals(currentUser.getEmployeeId())) {
            throw new RuntimeException("Không thể cập nhật thông tin của quản lý khác!");
        }
        // Nếu đang hạ quyền quản lý xuống nhân viên (role true -> false)
        if (existingEmployee.getRole() == true && employeeDTO.getRole() == false) {
            throw new RuntimeException("Không thể hạ quyền quản lý xuống nhân viên!");
        }
        // Nếu tự hạ quyền của chính mình
        if (existingEmployee.getEmployeeId().equals(currentUser.getEmployeeId()) && existingEmployee.getRole() == true && employeeDTO.getRole() == false) {
            throw new RuntimeException("Không thể tự hạ quyền của chính mình!");
        }

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

    // Tìm kiếm và phân trang nhân viên
    public Page<EmployeeDTO> searchEmployees(String searchTerm, String status, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        Specification<Employee> spec = (root, query, cb) -> {
            Predicate predicate = cb.conjunction();
            if (searchTerm != null && !searchTerm.isEmpty()) {
                Predicate namePredicate = cb.like(cb.lower(root.get("fullName")), "%" + searchTerm.toLowerCase() + "%");
                Predicate phonePredicate = cb.like(root.get("phoneNumber"), "%" + searchTerm + "%");
                Predicate cccdPredicate = cb.like(root.get("citizenId"), "%" + searchTerm + "%");
                predicate = cb.and(predicate, cb.or(namePredicate, phonePredicate, cccdPredicate));
            }
            if (status != null && !status.isEmpty()) {
                predicate = cb.and(predicate, cb.equal(root.get("status"), status));
            }
            if (startDate != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("birthday"), startDate));
            }
            if (endDate != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("birthday"), endDate));
            }
            return predicate;
        };
        return employeeRepository.findAll(spec, pageable).map(EmployeeDTO::fromEntity);
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
            Optional<Employee> existingByEmail = employeeRepository.findByEmail(employeeDTO.getEmail());
            if (existingByEmail.isPresent() && (id.length == 0 || !existingByEmail.get().getEmployeeId().equals(id[0]))) {
                throw new IllegalArgumentException("Email đã tồn tại");
            }
        }
}

}