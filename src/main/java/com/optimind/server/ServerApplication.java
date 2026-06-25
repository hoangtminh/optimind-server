package com.optimind.server;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ServerApplication {

	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		SpringApplication.run(ServerApplication.class, args);
	}

	@org.springframework.context.annotation.Bean
	public org.springframework.boot.CommandLineRunner initAdminUser(
			com.optimind.server.module.user.repo.UserRepository userRepository,
			org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
		return args -> {
			String email = "admin@gmail.com";
			userRepository.findByEmail(email).ifPresentOrElse(
				user -> {
					if (!"ADMIN".equals(user.getRole())) {
						user.setRole("ADMIN");
						userRepository.save(user);
						System.out.println("Updated existing user " + email + " to ADMIN role.");
					}
				},
				() -> {
					com.optimind.server.module.user.entity.UserEntity admin = com.optimind.server.module.user.entity.UserEntity.builder()
						.email(email)
						.username("admin")
						.password(passwordEncoder.encode("admin123"))
						.role("ADMIN")
						.build();
					userRepository.save(admin);
					System.out.println("Created admin user admin@gmail.com with password: admin123");
				}
			);
		};
	}

}
