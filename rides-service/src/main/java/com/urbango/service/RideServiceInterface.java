package com.urbango.service;


import com.urbango.dto.RideAssignmentResponse;
import com.urbango.dto.RideRequestDto;
import com.urbango.dto.RideResponseDto;

public interface RideServiceInterface {
    RideResponseDto createRide(RideRequestDto rideRequestDto);
    RideAssignmentResponse acceptRide(Long rideId, Long driverId);
    RideResponseDto assignDriver(Long rideId, Long driverId);
    RideResponseDto completeRide(Long rideId);
}