package com.univer.bookcom.aspect;

import java.util.Arrays;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Pointcut("within(com.univer.bookcom.controller..*)")
    public void controllerPointcut() {}

    @Pointcut("within(com.univer.bookcom.service..*)")
    public void servicePointcut() {}

    @Around("controllerPointcut()")
    public Object logController(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("==> {}.{}() с аргументами: {}",
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName(),
                Arrays.toString(joinPoint.getArgs()));

        try {
            Object result = joinPoint.proceed();
            log.info("<== {}.{}() с результатом: {}",
                    joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName(),
                    result != null ? result.toString() : "null");

            return result;
        } catch (Exception e) {
            log.error("<== {}.{}() с исключением: {}: {}",
                    joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName(),
                    e.getClass().getName(),
                    e.getMessage());
            throw e;
        }
    }

    @Around("servicePointcut()")
    public Object logService(ProceedingJoinPoint joinPoint) throws Throwable {
        log.debug("==> {}.{}() с аргументами: {}",
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName(),
                Arrays.toString(joinPoint.getArgs()));

        try {
            Object result = joinPoint.proceed();
            log.debug("<== {}.{}() с результатом: {}",
                    joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName(),
                    result != null ? result.toString() : "null");

            return result;
        } catch (Exception e) {
            log.error("<== {}.{}() с исключением: {}: {}",
                    joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName(),
                    e.getClass().getName(),
                    e.getMessage());
            throw e;
        }
    }

    @AfterThrowing(pointcut = "controllerPointcut() || servicePointcut()", throwing = "e")
    public void logAfterThrowing(Exception e) {
        log.error("Исключение в приложении: {}", e.getMessage(), e);
    }
}