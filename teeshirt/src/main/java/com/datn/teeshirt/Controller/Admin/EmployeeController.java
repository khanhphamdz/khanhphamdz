package com.datn.teeshirt.Controller.Admin;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.datn.teeshirt.Controller.ResponseObject;
import com.datn.teeshirt.DTO.EmployeeDTO;
import com.datn.teeshirt.Service.EmployeeService;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;


@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    // Lấy danh sách nhân viên (phân trang, tìm kiếm)
    @GetMapping
    public ResponseEntity<Page<EmployeeDTO>> getEmployees(
        @RequestParam(required = false) String searchTerm,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Page<EmployeeDTO> result = employeeService.searchEmployees(searchTerm, status, startDate, endDate, PageRequest.of(page, size));
        return ResponseEntity.ok(result);
    }

    // Lấy chi tiết nhân viên
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDTO> getEmployee(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    // Lấy thông tin nhân viên hiện tại
    @GetMapping("/me")
    public ResponseEntity<EmployeeDTO> getCurrentEmployee() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentEmail = null;
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            currentEmail = ((UserDetails) authentication.getPrincipal()).getUsername();
        }
        if (currentEmail == null) {
            return ResponseEntity.badRequest().build();
        }
        EmployeeDTO dto = employeeService.findByEmail(currentEmail)
                .map(EmployeeDTO::fromEntity)
                .orElse(null);
        if (dto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dto);
    }

    // Thêm nhân viên
    @PostMapping
    public ResponseEntity<ResponseObject> addEmployee(@RequestBody @Valid EmployeeDTO employeeDTO, BindingResult result) {
        
        
        try {
            employeeService.createEmployee(employeeDTO);
            return ResponseEntity.ok(new ResponseObject("ok", "Thêm nhân viên thành công", employeeDTO));
        } catch (Exception e) {
            return ResponseEntity.ok(new ResponseObject("error", "Lỗi khi thêm nhân viên", e.getMessage()));
        }
    }

    // Sửa nhân viên
    @PutMapping("/{id}")
    public ResponseEntity<ResponseObject> updateEmployee(
        @PathVariable Long id,
        @RequestBody @Valid EmployeeDTO employeeDTO,
        BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            StringBuilder errorMsg = new StringBuilder();
            bindingResult.getFieldErrors().forEach(err -> {
                errorMsg.append(err.getDefaultMessage()).append("; ");
            });
            return ResponseEntity.ok(new ResponseObject("error", errorMsg.toString(), null));
        }
        try {
            employeeService.updateEmployee(id, employeeDTO);
            return ResponseEntity.ok(new ResponseObject("ok", "Cập nhật thành công", null));
        } catch (Exception e) {
            return ResponseEntity.ok(new ResponseObject("error", e.getMessage(), null));
        }
    }

    // Xóa mềm nhân viên
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.setEmployeeInactive(id);
        return ResponseEntity.noContent().build();
    }
}
