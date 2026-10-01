package com.ridelink.ride_service.exception;

public class ServiceUnavailableException extends RuntimeException {
    public ServiceUnavailableException(String msg) { super(msg); }
}