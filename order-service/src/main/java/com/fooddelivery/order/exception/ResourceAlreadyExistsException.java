package com.fooddelivery.order.exception;

public class ResourceAlreadyExistsException extends RuntimeException {

  public ResourceAlreadyExistsException(String message) {
    super(message);
  }

  public ResourceAlreadyExistsException(String resource, String field, Object value) {
    super(String.format("%s with %s '%s' already exists", resource, field, value));
  }
}
