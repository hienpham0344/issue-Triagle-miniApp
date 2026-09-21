package com.example.issuetriage.dto;

import jakarta.validation.constraints.NotBlank;

public record TriageRequest(@NotBlank String issue) {
}
