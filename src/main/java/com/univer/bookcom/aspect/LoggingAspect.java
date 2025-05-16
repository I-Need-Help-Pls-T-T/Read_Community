package com.univer.bookcom.aspect;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class LoggingAspect {
    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    @Pointcut("within(com.univer.bookcom.controller..*)")
    public void controllerPointcut() {}

    @Around("controllerPointcut()")
    public Object logController(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes)
            RequestContextHolder.currentRequestAttributes()).getRequest();

        String httpMethod = request.getMethod();
        String requestURI = request.getRequestURI();

        String actionDescription = joinPoint.getSignature().getName();

        log.info("Request: {} {} -> {}", httpMethod, requestURI, actionDescription);

        try {
            Object result = joinPoint.proceed();
            log.info("Response: {} {} -> успешно выполнен", httpMethod, requestURI);
            return result;
        } catch (Exception e) {
            log.error("Ошибка при выполнении {} {}: {}", httpMethod, requestURI, e.toString());
            throw e;
        }
    }

    @AfterReturning(pointcut = "within(com.univer.bookcom.service..*) && execution(* add*(..))", returning = "result")
    public void logCacheAdd(Object result) {
        log.info("Добавление в кэш: результат = {}", result);
    }

    @AfterThrowing(pointcut = "within(com.univer.bookcom.service..*)", throwing = "ex")
    public void logServiceException(Exception ex) {
        log.error("Ошибка в сервисе: {}", ex.toString());
    }
}