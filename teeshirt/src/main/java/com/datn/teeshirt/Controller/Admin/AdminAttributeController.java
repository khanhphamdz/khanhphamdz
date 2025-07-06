package com.datn.teeshirt.Controller.Admin;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.datn.teeshirt.DTO.ColorDTO;
import com.datn.teeshirt.DTO.MaterialDTO;
import com.datn.teeshirt.DTO.SizeDTO;
import com.datn.teeshirt.Service.ColorService;
import com.datn.teeshirt.Service.MaterialService;
import com.datn.teeshirt.Service.SizeService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/product/attribute")
public class AdminAttributeController {

    @Autowired
    private SizeService sizeService;

    @Autowired
    private ColorService colorService;

    @Autowired
    private MaterialService materialService;

    // ========== SIZE MANAGEMENT ==========

    @GetMapping("/size")
    public String listSizes(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {

        Page<SizeDTO> sizes = sizeService.searchSizes(search, page, 10);
        Page<ColorDTO> colors = colorService.searchColors(search, page, 10);
        Page<MaterialDTO> materials = materialService.searchMaterials(search, page, 10);
        model.addAttribute("materials", materials);
        model.addAttribute("sizes", sizes);
        model.addAttribute("colors", colors);
        model.addAttribute("search", search);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", sizes.getTotalPages());
        model.addAttribute("totalItems", sizes.getTotalElements());
        model.addAttribute("active", "size");

        return "admin/product/attribute-management";
    }

    @PostMapping("/size/add")
    @ResponseBody
    public Map<String, Object> addSize(@Valid SizeDTO sizeDTO, BindingResult result) {
        Map<String, Object> response = new HashMap<>();

        if (result.hasErrors()) {
            response.put("success", false);
            response.put("message", "Dữ liệu không hợp lệ");
            response.put("errors", result.getFieldErrors());
            return response;
        }

        try {
            SizeDTO savedSize = sizeService.createSize(sizeDTO);
            response.put("success", true);
            response.put("message", "Thêm kích cỡ thành công");
            response.put("data", savedSize);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return response;
    }

    @PostMapping("/size/update/{id}")
    @ResponseBody
    public Map<String, Object> updateSize(@PathVariable Long id, @Valid SizeDTO sizeDTO, BindingResult result) {
        Map<String, Object> response = new HashMap<>();

        if (result.hasErrors()) {
            response.put("success", false);
            response.put("message", "Dữ liệu không hợp lệ");
            response.put("errors", result.getFieldErrors());
            return response;
        }

        try {
            SizeDTO updatedSize = sizeService.updateSize(id, sizeDTO);
            response.put("success", true);
            response.put("message", "Cập nhật kích cỡ thành công");
            response.put("data", updatedSize);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return response;
    }

    @PostMapping("/size/delete/{id}")
    @ResponseBody
    public Map<String, Object> deleteSize(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();

        try {
            boolean deleted = sizeService.deleteSize(id);
            if (deleted) {
                response.put("success", true);
                response.put("message", "Xóa kích cỡ thành công");
            } else {
                response.put("success", false);
                response.put("message", "Không tìm thấy kích cỡ để xóa");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return response;
    }

    // ========== COLOR MANAGEMENT ==========

    @GetMapping("/color")
    public String listColors(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {

        Page<SizeDTO> sizes = sizeService.searchSizes(search, page, 10);
        Page<ColorDTO> colors = colorService.searchColors(search, page, 10);
        Page<MaterialDTO> materials = materialService.searchMaterials(search, page, 10);
        model.addAttribute("materials", materials);
        model.addAttribute("sizes", sizes);
        model.addAttribute("colors", colors);
        model.addAttribute("search", search);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", colors.getTotalPages());
        model.addAttribute("totalItems", colors.getTotalElements());
        model.addAttribute("active", "color");

        return "admin/product/attribute-management";
    }

    @PostMapping("/color/add")
    @ResponseBody
    public Map<String, Object> addColor(@Valid ColorDTO colorDTO, BindingResult result) {
        Map<String, Object> response = new HashMap<>();

        if (result.hasErrors()) {
            response.put("success", false);
            response.put("message", "Dữ liệu không hợp lệ");
            response.put("errors", result.getFieldErrors());
            return response;
        }

        try {
            ColorDTO savedColor = colorService.createColor(colorDTO);
            response.put("success", true);
            response.put("message", "Thêm màu sắc thành công");
            response.put("data", savedColor);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return response;
    }

    @PostMapping("/color/update/{id}")
    @ResponseBody
    public Map<String, Object> updateColor(@PathVariable Long id, @Valid ColorDTO colorDTO, BindingResult result) {
        Map<String, Object> response = new HashMap<>();

        if (result.hasErrors()) {
            response.put("success", false);
            response.put("message", "Dữ liệu không hợp lệ");
            response.put("errors", result.getFieldErrors());
            return response;
        }

        try {
            ColorDTO updatedColor = colorService.updateColor(id, colorDTO);
            response.put("success", true);
            response.put("message", "Cập nhật màu sắc thành công");
            response.put("data", updatedColor);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return response;
    }

    @PostMapping("/color/delete/{id}")
    @ResponseBody
    public Map<String, Object> deleteColor(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();

        try {
            boolean deleted = colorService.deleteColor(id);
            if (deleted) {
                response.put("success", true);
                response.put("message", "Xóa màu sắc thành công");
            } else {
                response.put("success", false);
                response.put("message", "Không tìm thấy màu sắc để xóa");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return response;
    }

    // ========== MATERIAL MANAGEMENT ==========

    @GetMapping("/material")
    public String listMaterials(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {

        Page<SizeDTO> sizes = sizeService.searchSizes(search, page, 10);
        Page<ColorDTO> colors = colorService.searchColors(search, page, 10);
        Page<MaterialDTO> materials = materialService.searchMaterials(search, page, 10);
        model.addAttribute("materials", materials);
        model.addAttribute("sizes", sizes);
        model.addAttribute("colors", colors);
        model.addAttribute("search", search);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", materials.getTotalPages());
        model.addAttribute("totalItems", materials.getTotalElements());
        model.addAttribute("active", "material");

        return "admin/product/attribute-management";
    }

    @PostMapping("/material/add")
    @ResponseBody
    public Map<String, Object> addMaterial(@Valid MaterialDTO materialDTO, BindingResult result) {
        Map<String, Object> response = new HashMap<>();

        if (result.hasErrors()) {
            response.put("success", false);
            response.put("message", "Dữ liệu không hợp lệ");
            response.put("errors", result.getFieldErrors());
            return response;
        }

        try {
            MaterialDTO savedMaterial = materialService.createMaterial(materialDTO);
            response.put("success", true);
            response.put("message", "Thêm chất liệu thành công");
            response.put("data", savedMaterial);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return response;
    }

    @PostMapping("/material/update/{id}")
    @ResponseBody
    public Map<String, Object> updateMaterial(@PathVariable Long id, @Valid MaterialDTO materialDTO,
            BindingResult result) {
        Map<String, Object> response = new HashMap<>();

        if (result.hasErrors()) {
            response.put("success", false);
            response.put("message", "Dữ liệu không hợp lệ");
            response.put("errors", result.getFieldErrors());
            return response;
        }

        try {
            MaterialDTO updatedMaterial = materialService.updateMaterial(id, materialDTO);
            response.put("success", true);
            response.put("message", "Cập nhật chất liệu thành công");
            response.put("data", updatedMaterial);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return response;
    }

    @PostMapping("/material/delete/{id}")
    @ResponseBody
    public Map<String, Object> deleteMaterial(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();

        try {
            boolean deleted = materialService.deleteMaterial(id);
            if (deleted) {
                response.put("success", true);
                response.put("message", "Xóa chất liệu thành công");
            } else {
                response.put("success", false);
                response.put("message", "Không tìm thấy chất liệu để xóa");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return response;
    }
}