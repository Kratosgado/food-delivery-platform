package com.fooddelivery.customer.exception;

/** Exception thrown when authentication is required but missing or invalid. */
public class UnauthorizedException extends RuntimeException {

  public UnauthorizedException(String message) {
    super(message);
  }

  public UnauthorizedException() {
    super("Authentication required");
  }
}
