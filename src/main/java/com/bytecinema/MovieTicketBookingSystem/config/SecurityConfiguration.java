package com.bytecinema.MovieTicketBookingSystem.config;

import java.util.Map;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.bytecinema.MovieTicketBookingSystem.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import com.bytecinema.MovieTicketBookingSystem.util.SecurityUtil;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.util.Base64;


@Configuration
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {
    @Value("${bytecinema.jwt.base64-secret}")
    private String jwtKey;

    private final UserService userService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, CustomAuthenticationEntryPoint customAuthenticationEntryPoint) throws Exception {
    http
        .csrf(c -> c.disable())
        .cors(Customizer.withDefaults())
        .authorizeHttpRequests(authz -> authz
                .requestMatchers("/", "/api/v1/auth/**", "/api/v1/roles").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/genres/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/movies/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/screening/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/vn-pay-callback/**").permitAll()
                .anyRequest().authenticated())
        .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwtConfigurer -> jwtConfigurer
                        .decoder(jwtDecoder())
                        .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
                .authenticationEntryPoint(customAuthenticationEntryPoint))
        .formLogin(f -> f.disable())
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));


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
//                Instant iat = (Instant) claims.get("iat");
//                String email = (String) claims.get("sub");
//                User user = this.userService.handleGetUserByEmail(email);
//                if(user == null) {
//                    throw new RuntimeException("User not found");
//                }
//                if(user.getPasswordUpdatedAt().isAfter(iat)) {
//                    throw new RuntimeException("Access token invalid");
//                }

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
