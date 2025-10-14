package com.sliit.parking_reservation_and_management_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class ParkingReservationAndManagementSystemApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ParkingReservationAndManagementSystemApplication.class);

        // Add shutdown hook for graceful shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("🛑 Application is shutting down...");
        }));

        app.run(args);
    }

    @Bean
    public String debugInfo(Environment env) {
        System.out.println("🚀 Starting Parking Reservation and Management System");
        System.out.println("🔗 Database URL: " + env.getProperty("spring.datasource.url"));
        System.out.println("👤 Database User: " + env.getProperty("spring.datasource.username"));
        System.out.println("🌐 Server Port: " + env.getProperty("server.port"));

        return "debug-info";
    }

//    @Bean
//    public CommandLineRunner initAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
//        return args -> {
//            if (userRepository.findByEmail("admin@parking.com").isEmpty()) {
//                User admin = new User();
//                admin.setFirstName("Default");
//                admin.setLastName("Admin");
//                admin.setEmail("admin@parking.com");
//                admin.setPasswordHash(passwordEncoder.encode("admin123"));
//                admin.setRole("ADMIN");
//                admin.setPhoneNumber("0000000000");
//                userRepository.save(admin);
//                System.out.println("✅ Default admin created: admin@parking.com / admin123");
//            }
//        };
//    }



}
