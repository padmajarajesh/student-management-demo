package com.demo.student.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.List;

/**
 * Fails fast with a clear message when the "prod" profile is active but a
 * required environment variable is missing (e.g. forgotten on Render).
 * Registered in META-INF/spring.factories so it runs before any bean is created.
 */
public class RequiredEnvironmentCheck implements EnvironmentPostProcessor {

    private static final List<String> REQUIRED_IN_PROD =
            List.of("DB_URL", "DB_USERNAME", "DB_PASSWORD", "JWT_SECRET", "FRONTEND_URL");

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (!environment.matchesProfiles("prod")) {
            return;
        }
        List<String> missing = REQUIRED_IN_PROD.stream()
                .filter(name -> {
                    String value = environment.getProperty(name);
                    return value == null || value.isBlank();
                })
                .toList();
        if (!missing.isEmpty()) {
            throw new IllegalStateException(
                    "Missing required environment variable(s) for the prod profile: " + String.join(", ", missing)
                            + ". Set them in your hosting provider's dashboard (see README).");
        }
    }
}
