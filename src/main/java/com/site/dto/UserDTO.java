package com.site.dto;

import java.util.List;

public record UserDTO(
        Long userId,
        String email,
        List<String> roles
) {}