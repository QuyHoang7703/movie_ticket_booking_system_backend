// package com.bytecinema.MovieTicketBookingSystem.config;

// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.web.servlet.config.annotation.CorsRegistry;
// import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// @Configuration
// public class WebConfig implements WebMvcConfigurer {

//     // @Override
//     // public void addCorsMappings(CorsRegistry registry) {
//     //     registry.addMapping("/**")  // Cho phép tất cả các endpoint
//     //             .allowedOrigins("http://localhost:5173")  // Thay thế bằng domain của frontend
//     //             .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")  // Các phương thức được phép
//     //             .allowedHeaders("*")  // Cho phép tất cả các header
//     //             .allowCredentials(true);  // Cho phép gửi cookie nếu cần
//     // }

//     @Override
//     public void addCorsMappings(CorsRegistry registry) {
//         registry.addMapping("/**") // Áp dụng cho tất cả các endpoint
//                 .allowedOrigins("*") // Cho phép tất cả các domain
//                 .allowedMethods("*") // Cho phép tất cả các phương thức
//                 .allowedHeaders("*") // Cho phép tất cả các header
//                 .allowCredentials(true); // Cho phép gửi cookie
//     }
// }
