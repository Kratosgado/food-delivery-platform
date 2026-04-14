package com.fooddelivery.restaurant.aop;

import java.util.Arrays;
import java.util.Objects;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

  private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

  @Pointcut(
      "within(com.fooddelivery.restaurant.controller..*) ||"
          + " within(com.fooddelivery.restaurant.service..*)")
  public void applicationPackagePointcut() {}

  @Around("applicationPackagePointcut()")
  public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
    MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
    String className = methodSignature.getDeclaringType().getSimpleName();
    String methodName = methodSignature.getName();
    String currentExecution = className + "." + methodName;

    log.info(
        "Enter: {} with arguments[s] = {}", currentExecution, Arrays.toString(joinPoint.getArgs()));

    long startTime = System.currentTimeMillis();
    Object result = joinPoint.proceed();
    long executionTime = System.currentTimeMillis() - startTime;

    log.info(
        "Exit: {} with result = {} in {} ms",
        currentExecution,
        Objects.toString(result),
        executionTime);

    return result;
  }
}

