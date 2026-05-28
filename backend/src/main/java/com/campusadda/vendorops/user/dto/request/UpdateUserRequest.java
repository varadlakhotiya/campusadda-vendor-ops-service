package com.campusadda.vendorops.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRequest {

    @Size(max = 120, message = "Full name must not exceed 120 characters")
    private String fullName;

    @Email(message = "Email must be valid")
    @Size(max = 150, message = "Email must not exceed 150 characters")
    private String email;

    @Size(max = 20, message = "Phone must not exceed 20 characters")
    @Pattern(
            regexp = "^[0-9+\\-() ]*$",
            message = "Phone contains invalid characters"
    )
    private String phone;
}