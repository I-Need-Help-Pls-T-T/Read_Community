package com.univer.bookcom.aspect;

import com.univer.bookcom.exception.BookNotFoundException;
import com.univer.bookcom.exception.UserNotFoundException;
import com.univer.bookcom.exception.CommentNotFoundException;
import com.univer.bookcom.exception.ServiceExecutionException;
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
    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    @Pointcut("within(com.univer.bookcom.controller..*)")
    public void controllerPointcut() {}

    @Pointcut("within(com.univer.bookcom.service..*)")
    public void servicePointcut() {}

    @Around("controllerPointcut()")
    public Object logController(ProceedingJoinPoint joinPoint) throws Throwable {
        if (log.isInfoEnabled()) {
            log.info("==> {}.{}() с аргументами: {}",
                    joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName(),
                    Arrays.toString(joinPoint.getArgs()));
        }

        try {
            Object result = joinPoint.proceed();

            if (log.isInfoEnabled()) {
                log.info("<== {}.{}() с результатом: {}",
                        joinPoint.getSignature().getDeclaringTypeName(),
                        joinPoint.getSignature().getName(),
                        result != null ? result : "null");
            }

            return result;
        } catch (BookNotFoundException | UserNotFoundException | CommentNotFoundException e) {
            throw e;
        } catch (Exception e) {
            String message = String.format("Ошибка в %s.%s() с исключением: %s",
                    joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName(),
                    e.getClass().getSimpleName());
            throw new ServiceExecutionException(message, e);
        }
    }

    @Around("servicePointcut()")
    public Object logService(ProceedingJoinPoint joinPoint) throws Throwable {
        if (log.isDebugEnabled()) {
            log.debug("==> {}.{}() с аргументами: {}",
                    joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName(),
                    Arrays.toString(joinPoint.getArgs()));
        }

        try {
            Object result = joinPoint.proceed();

            if (log.isDebugEnabled()) {
                log.debug("<== {}.{}() с результатом: {}",
                        joinPoint.getSignature().getDeclaringTypeName(),
                        joinPoint.getSignature().getName(),
                        result != null ? result : "null");
            }

            return result;
        } catch (Exception e) {
            String message = String.format("Ошибка в %s.%s() с исключением: %s",
                    joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName(),
                    e.getClass().getSimpleName());
            throw new ServiceExecutionException(message, e);
        }
    }

    @AfterThrowing(pointcut = "controllerPointcut() || servicePointcut()", throwing = "e")
    public void logAfterThrowing(Exception e) {
        log.error("Исключение в приложении", e);
    }
}