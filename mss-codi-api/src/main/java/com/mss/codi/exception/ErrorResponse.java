package com.mss.codi.exception;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;


@Data
@Builder
public class ErrorResponse {
    private String message;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
