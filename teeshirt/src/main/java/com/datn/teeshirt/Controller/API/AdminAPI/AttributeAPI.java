package com.datn.teeshirt.Controller.API.AdminAPI;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.datn.teeshirt.Controller.ResponseObject;
import com.datn.teeshirt.DTO.ColorDTO;
import com.datn.teeshirt.DTO.MaterialDTO;
import com.datn.teeshirt.DTO.SizeDTO;
import com.datn.teeshirt.Service.ColorService;
import com.datn.teeshirt.Service.MaterialService;
import com.datn.teeshirt.Service.SizeService;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/attribute")
public class AttributeAPI {

    @Autowired
    ColorService colorService;

    @Autowired
    SizeService sizeService;

    @Autowired
    MaterialService materialService;

    @GetMapping("/get-all-attribute")
    public ResponseEntity<ResponseObject> getMethodName() {
        List<ColorDTO> listColor = colorService.getAllColors();
        List<SizeDTO> listSize = sizeService.getAllSizes();
        List<MaterialDTO> listMaterial = materialService.getAllMaterials();

        Map<String, Object> response = Map.of(
                "listColor", listColor,
                "listSize", listSize,
                "listMaterial", listMaterial);

        if (listColor != null && listSize != null && listMaterial != null) {
            return ResponseEntity.ok(new ResponseObject("ok", "All attribute found", response));
        }

        return ResponseEntity.ok(new ResponseObject("ok", "All attribute not found", null));
    }

}
