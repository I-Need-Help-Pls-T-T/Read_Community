package com.univer.bookcom.aspect;

import com.univer.bookcom.service.VisitCounterService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

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
        String methodName = joinPoint.getSignature().getDeclaringType().getSimpleName()
                + "." + joinPoint.getSignature().getName();

        visitCounterService.increment(methodName);
    }
}