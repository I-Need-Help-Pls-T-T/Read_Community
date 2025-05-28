package com.univer.bookcom.aspect;

import com.univer.bookcom.service.VisitCounterService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class VisitCountingAspect {

    private final VisitCounterService visitCounterService;

    public VisitCountingAspect(VisitCounterService visitCounterService) {
        this.visitCounterService = visitCounterService;
    }

    @Pointcut("within(com.univer.bookcom.controller..*)")
    public void controllerMethods() {}

    @Before("controllerMethods()")
    public void countVisit(JoinPoint joinPoint) {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String path = request.getRequestURI();
            visitCounterService.increment(path);
        }
    }
}
