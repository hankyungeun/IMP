package com.bootest.dto.pricing;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PriceDimensionDetailsDto {
    private String unit;
    private String endRange;
    private String description;
    private String rateCode;
    private String beginRange;
    private PricePerUnitDto pricePerUnit;
}
