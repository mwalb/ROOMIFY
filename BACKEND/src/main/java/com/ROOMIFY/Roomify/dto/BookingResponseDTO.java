package com.ROOMIFY.Roomify.dto;


import java.time.LocalDateTime;

public class BookingResponseDTO {
    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    private Long roomId;
    private String roomTitle;
    private String status;
    private Double totalPrice;
    private Double rentAmount;
    private Double facilitationFee;
    private String listingType;
    private Long dalaliId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime createdAt;
    private Integer numberOfGuests;
    private String specialRequests;
    private String roomImageUrl;
    private String bookingType; // VIEWING or RESERVATION
    private String viewingTimeSlot; // MORNING, AFTERNOON, EVENING
    private String meetingPoint;

    // Getters and setters for ALL fields
    public Double getRentAmount() { return rentAmount; }
    public void setRentAmount(Double rentAmount) { this.rentAmount = rentAmount; }

    public Double getFacilitationFee() { return facilitationFee; }
    public void setFacilitationFee(Double facilitationFee) { this.facilitationFee = facilitationFee; }

    public String getListingType() { return listingType; }
    public void setListingType(String listingType) { this.listingType = listingType; }

    public Long getDalaliId() { return dalaliId; }
    public void setDalaliId(Long dalaliId) { this.dalaliId = dalaliId; }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }

    public String getRoomTitle() { return roomTitle; }
    public void setRoomTitle(String roomTitle) { this.roomTitle = roomTitle; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(Double totalPrice) { this.totalPrice = totalPrice; }

    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Integer getNumberOfGuests() { return numberOfGuests; }
    public void setNumberOfGuests(Integer numberOfGuests) { this.numberOfGuests = numberOfGuests; }

    public String getSpecialRequests() { return specialRequests; }
    public void setSpecialRequests(String specialRequests) { this.specialRequests = specialRequests; }

    public String getRoomImageUrl() { return roomImageUrl; }
    public void setRoomImageUrl(String roomImageUrl) { this.roomImageUrl = roomImageUrl; }

    public String getBookingType() { return bookingType; }
    public void setBookingType(String bookingType) { this.bookingType = bookingType; }

    public String getViewingTimeSlot() { return viewingTimeSlot; }
    public void setViewingTimeSlot(String viewingTimeSlot) { this.viewingTimeSlot = viewingTimeSlot; }

    public String getMeetingPoint() { return meetingPoint; }
    public void setMeetingPoint(String meetingPoint) { this.meetingPoint = meetingPoint; }
}
