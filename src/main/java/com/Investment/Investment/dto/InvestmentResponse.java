package com.Investment.Investment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvestmentResponse {
    private String id;
    private String firstName;
    private String middleName;
    private String lastName;
    private String age;
    private String mobileNumber;
    private String emailAddress;
    private String profession;
    private String professionOther;
    private List<String> currentInvestments;
    private String currentInvestmentsOther;
    private String mostInterestedIn;
    private Long createdAt;
    private Long updatedAt;
    
    // Influencer referral tracking
    private String referredBy;        // The influencer name who referred this user
}

