package com.datn.teeshirt.Controller.Admin;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.datn.teeshirt.DTO.EmployeeDTO;
import com.datn.teeshirt.Service.EmployeeService;

import java.util.List;

@Controller
@RequestMapping("/admin/employees")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    // Hiển thị danh sách nhân viên
    @GetMapping
    public String danhSachNhanVien(Model model) {
        List<EmployeeDTO> nhanViens = employeeService.getAllEmployees();
        model.addAttribute("employees", nhanViens);
        model.addAttribute("x", nhanViens.size());
        model.addAttribute("employeeDTO", new EmployeeDTO());
        return "admin/account/employee-management.html";
    }

    // Hiển thị form thêm nhân viên
    @GetMapping("/them")
    public String hienThiFormThemNhanVien(Model model) {
        model.addAttribute("employeeDTO", new EmployeeDTO());
        model.addAttribute("employees", employeeService.getAllEmployees());
        model.addAttribute("x", employeeService.getAllEmployees().size());
        return "admin/account/employee-management.html";
    }

    // Thêm nhân viên mới
    @PostMapping("/them")
    public String themNhanVien(@Valid @ModelAttribute("employeeDTO") EmployeeDTO employeeDTO,
            BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("employees", employeeService.getAllEmployees());
            model.addAttribute("x", employeeService.getAllEmployees().size());
            model.addAttribute("error", "Vui lòng kiểm tra lại các trường dữ liệu");
            return "admin/account/employee-management.html";
        }
        try {
            employeeService.createEmployee(employeeDTO);
            return "redirect:/admin/employees?success=Thêm nhân viên thành công";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("employees", employeeService.getAllEmployees());
            model.addAttribute("x", employeeService.getAllEmployees().size());
            return "admin/account/employee-management.html";
        } catch (Exception e) {
            model.addAttribute("error", "Lỗi hệ thống: " + e.getMessage());
            model.addAttribute("employees", employeeService.getAllEmployees());
            model.addAttribute("x", employeeService.getAllEmployees().size());
            return "admin/account/employee-management.html";
        }
    }

    // Hiển thị form sửa nhân viên
    @GetMapping("/sua/{id}")
    public String hienThiFormSuaNhanVien(@PathVariable Long id, Model model) {
        EmployeeDTO employeeDTO = employeeService.getEmployeeById(id);
        model.addAttribute("employeeDTO", employeeDTO);
        model.addAttribute("employees", employeeService.getAllEmployees());
        model.addAttribute("x", employeeService.getAllEmployees().size());
        return "admin/account/employee-management.html";
    }

    // Cập nhật thông tin nhân viên
    @PostMapping("/sua/{id}")
    public String suaNhanVien(@PathVariable Long id,
            @Valid @ModelAttribute("employeeDTO") EmployeeDTO employeeDTO,
            BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("employees", employeeService.getAllEmployees());
            model.addAttribute("x", employeeService.getAllEmployees().size());
            model.addAttribute("error", "Vui lòng kiểm tra lại các trường dữ liệu");
            return "admin/account/employee-management.html";
        }
        try {
            employeeService.updateEmployee(id, employeeDTO);
            return "redirect:/admin/employees?success=Cập nhật nhân viên thành công";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("employees", employeeService.getAllEmployees());
            model.addAttribute("x", employeeService.getAllEmployees().size());
            return "admin/account/employee-management.html";
        }
    }

    // Xóa nhân viên (xóa mềm)
    @PostMapping("/xoa/{id}")
    public String xoaNhanVien(@PathVariable Long id) {
        try {
            employeeService.setEmployeeInactive(id);
            return "redirect:/admin/employees?success=Chuyển trạng thái nhân viên thành nghỉ việc";
        } catch (RuntimeException e) {
            return "redirect:/admin/employees?error=Không tìm thấy nhân viên";
        }
    }
}
