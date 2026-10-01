package com.ridelink.ride_service.exception;

public class InvalidStateTransitionException extends RuntimeException {
    public InvalidStateTransitionException(String msg) { super(msg); }
}