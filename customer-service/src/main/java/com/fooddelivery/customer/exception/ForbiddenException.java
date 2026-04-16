package com.fooddelivery.customer.exception;

/** Exception thrown when user lacks permission to access a resource. */
public class ForbiddenException extends RuntimeException {

  public ForbiddenException(String message) {
    super(message);
  }

  public ForbiddenException() {
    super("Access denied");
  }
}
