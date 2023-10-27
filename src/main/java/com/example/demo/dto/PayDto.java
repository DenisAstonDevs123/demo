package com.example.demo.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PayDto {
    private BigDecimal amount;
    private String pin;
}
