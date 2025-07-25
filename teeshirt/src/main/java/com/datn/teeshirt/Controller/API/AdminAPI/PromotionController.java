package com.datn.teeshirt.Controller.API.AdminAPI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import com.datn.teeshirt.Controller.ResponseObject;
import com.datn.teeshirt.DTO.PromotionDTO;
import com.datn.teeshirt.Service.PromotionService;

@RestController
@RequestMapping("/api/admin/promotions")
public class PromotionController {
    @Autowired
    PromotionService promotionService;

    @GetMapping("")
    public Page<PromotionDTO> getAllPromotions(@RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "10") int size,
                                               @RequestParam(name="keyword", required = false) String keyword) {
        return promotionService.getAll(page, size, keyword);
    }

    @GetMapping("/{id}")
    public PromotionDTO getPromotionById(@PathVariable Long id) {
        return promotionService.getById(id);
    }

    @PostMapping("")
    public PromotionDTO createPromotion(@RequestBody PromotionDTO dto) {
        return promotionService.create(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseObject> updatePromotion(@PathVariable Long id, @RequestBody PromotionDTO dto) {
        return ResponseEntity.ok(new ResponseObject("ok", "Update promotion successfully", promotionService.update(id, dto)));
    }
    
    @DeleteMapping("/{id}")
    public void deletePromotion(@PathVariable Long id) {
        promotionService.delete(id);
    }
} 