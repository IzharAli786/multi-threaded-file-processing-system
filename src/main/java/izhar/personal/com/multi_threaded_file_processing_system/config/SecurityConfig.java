package izhar.personal.com.multi_threaded_file_processing_system.config;


import org.springframework.boot.autoconfigure.graphql.GraphQlProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration

public class SecurityConfig {
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
          .authorizeHttpRequests(authorize -> authorize
                // 1. Add this rule to permit all access to the SockJS info endpoint
                .requestMatchers("/customers/**").permitAll()
                // 2. Keep this rule to secure all other endpoints
                .anyRequest().authenticated()
          )
          .httpBasic(withDefaults());

    http
          .csrf(AbstractHttpConfigurer::disable)
          .cors(withDefaults());

    return http.build();
  }

}
