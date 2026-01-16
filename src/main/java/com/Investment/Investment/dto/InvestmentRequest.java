package com.Investment.Investment.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class InvestmentRequest {
    
    @NotBlank(message = "First name is required")
    private String firstName;
    
    private String middleName;
    
    @NotBlank(message = "Last name is required")
    private String lastName;
    
    @NotNull(message = "Age is required")
    @Min(value = 1, message = "Age must be greater than 0")
    @Max(value = 150, message = "Age must be less than 150")
    private Integer age;
    
    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Mobile number must be valid")
    private String mobileNumber;
    
    @NotBlank(message = "Email address is required")
    @Email(message = "Email must be valid")
    private String emailAddress;
    
    @NotBlank(message = "Profession is required")
    private String profession;
    
    private String professionOther; // Only required if profession is "Other"
    
    @NotBlank(message = "Investment background is required")
    private String investmentBackground;
    
    @NotNull(message = "Current investments are required")
    @NotEmpty(message = "At least one current investment must be selected")
    private List<String> currentInvestments;
    
    @NotBlank(message = "Interest area is required")
    private String mostInterestedIn;
}

