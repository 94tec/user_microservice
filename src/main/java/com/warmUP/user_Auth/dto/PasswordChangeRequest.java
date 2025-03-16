package com.warmUP.user_Auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordChangeRequest {

    @NotBlank(message = "Old password is required")
    @Size(min = 8, max = 128, message = "Old password must be between 8 and 128 characters")
    private String oldPassword;

    @NotBlank(message = "New password is required")
    @Size(min = 8, max = 128, message = "New password must be between 8 and 128 characters")
    private String newPassword;

    @NotBlank(message = "Confirmation of new password is required")
    @Size(min = 8, max = 128, message = "Confirmation of new password must be between 8 and 128 characters")
    private String confirmNewPassword;

    // Method to validate if new password and confirmation match
    public boolean isValidPasswordConfirmation() {
        return newPassword.equals(confirmNewPassword);
    }
}

