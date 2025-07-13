package com.anistebbal.starter.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDto {
    private String token;
    private String role;
    private String username;
    private String phone;
}
