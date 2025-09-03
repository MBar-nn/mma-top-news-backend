package com.site.security;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.regex.Pattern;

@Aspect
@Component
@Slf4j
public class InputValidationAspect {

    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
            "(?i)(union|select|insert|update|delete|drop|create|alter|exec|execute|script|javascript|vbscript|onload|onerror)",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern XSS_PATTERN = Pattern.compile(
            "(?i)<script[^>]*>.*?</script>|javascript:|on\\w+\\s*=|<iframe|<object|<embed",
            Pattern.CASE_INSENSITIVE
    );

    @Around("@annotation(org.springframework.web.bind.annotation.RequestMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PatchMapping)")
    public Object validateInput(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();

        for (Object arg : args) {
            if (arg instanceof String) {
                validateStringInput((String) arg);
            } else if (arg != null) {
                validateObjectInput(arg);
            }
        }

        return joinPoint.proceed();
    }

    private void validateStringInput(String input) {
        if (input == null || input.trim().isEmpty()) {
            return;
        }

        if (SQL_INJECTION_PATTERN.matcher(input).find()) {
            log.warn("Potential SQL injection attempt detected: {}", input);
            throw new SecurityException("Invalid input detected");
        }

        if (XSS_PATTERN.matcher(input).find()) {
            log.warn("Potential XSS attempt detected: {}", input);
            throw new SecurityException("Invalid input detected");
        }

        // Sprawdź długość
        if (input.length() > 10000) {
            throw new SecurityException("Input too long");
        }
    }

    private void validateObjectInput(Object obj) {
        Field[] fields = obj.getClass().getDeclaredFields();

        for (Field field : fields) {
            if (field.getType() == String.class) {
                field.setAccessible(true);
                try {
                    String value = (String) field.get(obj);
                    if (value != null) {
                        validateStringInput(value);
                    }
                } catch (IllegalAccessException e) {
                    log.error("Error validating field: {}", field.getName(), e);
                }
            }
        }
    }
}