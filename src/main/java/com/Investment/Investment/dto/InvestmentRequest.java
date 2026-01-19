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
    
    @NotBlank(message = "Age is required")
    private String age;
    
    @NotBlank(message = "Mobile number is required")
    private String mobileNumber;
    
    @NotBlank(message = "Email address is required")
    @Email(message = "Email must be valid")
    private String emailAddress;
    
    @NotBlank(message = "Profession is required")
    private String profession;
    
    private String professionOther; // Only required if profession is "Other"
    
    @NotNull(message = "Current investments are required")
    @NotEmpty(message = "At least one current investment must be selected")
    private List<String> currentInvestments;
    
    @NotBlank(message = "Interest area is required")
    private String mostInterestedIn;
}

