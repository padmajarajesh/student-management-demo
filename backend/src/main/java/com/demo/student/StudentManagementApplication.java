package com.demo.student;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

// UserDetailsServiceAutoConfiguration is excluded because authentication is
// handled by our own JWT cookie filter (no default in-memory user is needed).
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@ConfigurationPropertiesScan
public class StudentManagementApplication {

    public static void main(String[] args)
    {
        SpringApplication.run(StudentManagementApplication.class, args);
        System.out.println("------------------------------------------------------------");
    }
}
