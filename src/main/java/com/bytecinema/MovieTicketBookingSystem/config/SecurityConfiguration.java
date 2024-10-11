package com.bytecinema.MovieTicketBookingSystem.config;

import java.util.Map;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;

import com.bytecinema.MovieTicketBookingSystem.util.SecurityUtil;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.util.Base64;
// import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
@EnableWebSecurity
public class SecurityConfiguration {
    @Value("${bytecinema.jwt.base64-secret}")
    private String jwtKey;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    // @Bean
    // public SecurityFilterChain filterChain(HttpSecurity http, CustomAuthenticationEntryPoint customAuthenticationEntryPoint) throws Exception {
    //     http
    //             .csrf(c->c.disable())
    //             .cors(Customizer.withDefaults())
    //             .authorizeHttpRequests(
    //                     authz -> authz
    //                             // .requestMatchers("/", "/api/v1/auth/login", "/api/v1/auth/refresh", "/api/v1/auth/register", "/api/v1/roles", "/api/v1/auth/verify", "/api/v1/auth/resend").permitAll()
    //                             .requestMatchers("/", "/api/v1/auth/**").permitAll()
    //                             .anyRequest().authenticated())

    //             .oauth2ResourceServer((oauth2) -> oauth2.jwt(Customizer.withDefaults())
                
    //                 .authenticationEntryPoint(customAuthenticationEntryPoint))
    //             .formLogin(f -> f.disable())
    //             .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

    //     return http.build();
    // }
   @Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(c -> c.disable())
        .cors(Customizer.withDefaults())
        .authorizeHttpRequests(authz -> authz
                .requestMatchers("/", "/api/v1/auth/**").permitAll()
                .anyRequest().authenticated())
        .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwtConfigurer -> jwtConfigurer
                        .decoder(jwtDecoder())
                        .jwtAuthenticationConverter(jwtAuthenticationConverter())
                ))
        .formLogin(f -> f.disable())
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .addFilterBefore((request, response, chain) -> {
            // In ra thông tin Authentication trước khi vào Controller
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            System.out.println(">>> Authentication details: " + authentication);
            if (authentication != null) {
                authentication.getAuthorities().forEach(grantedAuthority -> {
                    System.out.println(">>> Granted Authority: " + grantedAuthority.getAuthority());
                });
            }
            chain.doFilter(request, response);
        }, CorsFilter.class);

    return http.build();
}


    //   Create signature's token
    @Bean
    public JwtEncoder jwtEncoder() {
        return new NimbusJwtEncoder(new ImmutableSecret<>(getSecretKey()));
    }

    private SecretKey getSecretKey() {
        byte[] keyBytes = Base64.from(jwtKey).decode();
        return new SecretKeySpec(keyBytes, 0, keyBytes.length, SecurityUtil.JWT_ALGORITHM.getName());
    }

    //Decode jwt from request's client which has jwt in its header
    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(
                getSecretKey()).macAlgorithm(SecurityUtil.JWT_ALGORITHM).build();
        return token -> {
            try {
                Jwt decodedJwt = jwtDecoder.decode(token);

                // Lấy role từ claims
                Map<String, Object> claims = decodedJwt.getClaims();
                String role = (String) claims.get("role");

                // Bạn có thể thêm logic ở đây để xử lý role nếu cần thiết
                System.out.println(">>> Role from JWT: " + role);
                 // In ra toàn bộ claims để kiểm tra
                System.out.println(">>> Decoded JWT claims: " + claims);

                return decodedJwt; // Trả về token đã decode
            } catch (Exception e) {
                System.out.println(">>> JWT error: " + e.getMessage());
                throw e;
            }
        };
    }

     @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthorityPrefix(""); //QUyền hạn được lấy ra ko cần có tiền tố gì trước nó
        grantedAuthoritiesConverter.setAuthoritiesClaimName("role"); //Lấy quyền hạn bên trong claim có tên là "permission"

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }
}
