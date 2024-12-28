package org.example.gamestoreapp.init;

import org.example.gamestoreapp.model.entity.User;
import org.example.gamestoreapp.model.enums.UserRole;
import org.example.gamestoreapp.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminUserInit implements CommandLineRunner {
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final static Logger LOGGER = LoggerFactory.getLogger(AdminUserInit.class);

    @Value("${app.admin.password}")
    private String adminPassword;

    public AdminUserInit(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        LOGGER.info("Initializing application built-in admin user...");
        long count = userRepository.count();

        if (adminPassword == null || adminPassword.isEmpty()) {
            throw new IllegalArgumentException("Admin password is not set in application properties!");
        }

        if (count == 0) {
            User admin = new User();
            String password = passwordEncoder.encode(adminPassword);

            admin.setUsername("hristo");
            admin.setPassword(password);
            admin.setEmail("hsivov@gmail.com");
            admin.setFirstName("Hristo");
            admin.setLastName("Sivov");
            admin.setAge(46);
            admin.setRole(UserRole.ADMIN);
            admin.setEnabled(true);

            try {
                userRepository.save(admin);
                LOGGER.info("Built-in admin user created successfully.");
            } catch (Exception e) {
                LOGGER.error("Error creating admin user: ", e);
            }
        } else {
            LOGGER.info("Application built-in admin user already exists. Skipping creation.");
        }
    }
}
