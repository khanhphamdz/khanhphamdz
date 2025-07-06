package com.datn.teeshirt.Controller.API;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/province-proxy")
public class ProvinceProxyController {
    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/province/{id}")
    public ResponseEntity<?> getProvince(@PathVariable String id) {
        String url = "https://provinces.open-api.vn/api/p/" + id;
        return ResponseEntity.ok(restTemplate.getForObject(url, Object.class));
    }

    @GetMapping("/district/{id}")
    public ResponseEntity<?> getDistrict(@PathVariable String id) {
        String url = "https://provinces.open-api.vn/api/d/" + id;
        return ResponseEntity.ok(restTemplate.getForObject(url, Object.class));
    }

    @GetMapping("/ward/{id}")
    public ResponseEntity<?> getWard(@PathVariable String id) {
        String url = "https://provinces.open-api.vn/api/w/" + id;
        return ResponseEntity.ok(restTemplate.getForObject(url, Object.class));
    }
} 