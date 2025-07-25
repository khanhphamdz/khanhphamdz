package com.datn.teeshirt.Controller.API;

import com.datn.teeshirt.DTO.ReturnRequestDTO;
import com.datn.teeshirt.Entity.ReturnRequest;
import com.datn.teeshirt.Service.ReturnRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/return-request")
public class ReturnRequestAPI {
    @Autowired
    private ReturnRequestService returnRequestService;

    // 1. Khách gửi yêu cầu trả hàng
    @PostMapping("/create")
    public ResponseEntity<?> createReturnRequest(@RequestBody ReturnRequestDTO dto, @RequestParam Long customerId) {
        boolean result = returnRequestService.createReturnRequest(dto, customerId);
        if (result) return ResponseEntity.ok("Yêu cầu trả hàng đã được gửi thành công.");
        return ResponseEntity.badRequest().body("Không thể gửi yêu cầu trả hàng. Kiểm tra điều kiện hoặc liên hệ hỗ trợ.");
    }

    // 2. Admin duyệt yêu cầu trả hàng
    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approveReturnRequest(@PathVariable Long id, @RequestParam(required = false) String adminNote) {
        boolean result = returnRequestService.approveReturnRequest(id, adminNote);
        if (result) return ResponseEntity.ok("Đã duyệt yêu cầu trả hàng.");
        return ResponseEntity.badRequest().body("Không thể duyệt yêu cầu trả hàng.");
    }

    // 3. Admin từ chối yêu cầu trả hàng
    @PostMapping("/{id}/reject")
    public ResponseEntity<?> rejectReturnRequest(@PathVariable Long id, @RequestParam String reason) {
        boolean result = returnRequestService.rejectReturnRequest(id, reason);
        if (result) return ResponseEntity.ok("Đã từ chối yêu cầu trả hàng.");
        return ResponseEntity.badRequest().body("Không thể từ chối yêu cầu trả hàng.");
    }

    // 4. Admin hoàn tất yêu cầu trả hàng (sau khi đã hoàn tiền/đổi hàng)
    @PostMapping("/{id}/complete")
    public ResponseEntity<?> completeReturnRequest(@PathVariable Long id, @RequestParam(required = false) String adminNote) {
        boolean result = returnRequestService.completeReturnRequest(id, adminNote);
        if (result) return ResponseEntity.ok("Đã hoàn tất yêu cầu trả hàng.");
        return ResponseEntity.badRequest().body("Không thể hoàn tất yêu cầu trả hàng.");
    }

    // 5. Admin hoặc khách hủy yêu cầu trả hàng
    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelReturnRequest(@PathVariable Long id, @RequestParam(required = false) String note) {
        boolean result = returnRequestService.cancelReturnRequest(id, note);
        if (result) return ResponseEntity.ok("Đã hủy yêu cầu trả hàng.");
        return ResponseEntity.badRequest().body("Không thể hủy yêu cầu trả hàng.");
    }

    // 4. Lấy danh sách yêu cầu trả hàng (lọc theo trạng thái, phân trang nếu cần)
    @GetMapping("/list")
    public ResponseEntity<List<ReturnRequestDTO>> getReturnRequests(@RequestParam(required = false) String status) {
        List<ReturnRequestDTO> list = returnRequestService.getReturnRequestDTOs(status);
        return ResponseEntity.ok(list);
    }

    // 5. Lấy chi tiết yêu cầu trả hàng
    @GetMapping("/{id}")
    public ResponseEntity<ReturnRequestDTO> getReturnRequestDetail(@PathVariable Long id) {
        ReturnRequestDTO detail = returnRequestService.getReturnRequestDTODetail(id);
        if (detail != null) return ResponseEntity.ok(detail);
        return ResponseEntity.notFound().build();
    }

    // 6. Lấy chi tiết yêu cầu trả hàng đầy đủ cho giao diện quản trị
    @GetMapping("/{id}/full")
    public ResponseEntity<?> getReturnRequestDetailFull(@PathVariable Long id) {
        java.util.Map<String, Object> detail = returnRequestService.getReturnRequestDetailFull(id);
        if (detail != null) return ResponseEntity.ok(detail);
        return ResponseEntity.notFound().build();
    }

    // 7. Thống kê số lượng trả hàng theo trạng thái cho dashboard
    @GetMapping("/stats")
    public Map<String, Long> getReturnRequestStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("pending", returnRequestService.countByStatus("PENDING"));
        stats.put("approved", returnRequestService.countByStatus("APPROVED"));
        stats.put("rejected", returnRequestService.countByStatus("REJECTED"));
        stats.put("completed", returnRequestService.countByStatus("COMPLETED"));
        return stats;
    }

    // 8. API filter, tìm kiếm, phân trang danh sách trả hàng
    @GetMapping("/search")
    public Page<ReturnRequestDTO> searchReturnRequests(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return returnRequestService.searchAndPage(status, keyword, pageable);
    }
} 