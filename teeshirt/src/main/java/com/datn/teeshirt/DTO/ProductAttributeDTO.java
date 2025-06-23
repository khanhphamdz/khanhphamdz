package com.datn.teeshirt.DTO;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductAttributeDTO {
    private Long productAttributeId;
    private String attributeName;
    private List<AttributeTermsDTO> attributeValue;
    private Boolean isVariation;
}
