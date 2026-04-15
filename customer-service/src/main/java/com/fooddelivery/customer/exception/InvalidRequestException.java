package com.fooddelivery.customer.exception;

/** Exception thrown when request parameters or body are invalid. */
public class InvalidRequestException extends RuntimeException {

  public InvalidRequestException(String message) {
    super(message);
  }

  public InvalidRequestException(String message, Throwable cause) {
    super(message, cause);
  }
}
