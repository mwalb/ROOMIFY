package com.ROOMIFY.Roomify.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DalaliStats {
    private int totalListings;
    private int activeListings;
    private int pendingListings;
    private int rentedListings;
    private double totalCommission;
    private double monthlyCommission;
    private int totalViews;
    private int totalInterested;
    private float averageRating;
    private String verificationStatus;
}