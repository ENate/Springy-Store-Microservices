package com.siriusxi.cloud.infra.eds.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.userdetails.User;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final String username;
  private final String password;

  public SecurityConfig(
      @Value("${app.eureka.user}") String username,
      @Value("${app.eureka.pass}") String password) {
    this.username = username;
    this.password = "{noop}".concat(password);
  }

  @Bean
  protected SecurityFilterChain webSecurityWebFilterChain(HttpSecurity http) throws Exception {
      http
          .csrf(csrf -> csrf.disable())
          .authorizeHttpRequests(authz -> authz
          // Allow actuator paths
          .requestMatchers("/actuator/**").permitAll()
          .anyRequest().authenticated())
          .formLogin(Customizer.withDefaults())
          .httpBasic(Customizer.withDefaults());
      return http.build();
  }

  @Bean
  public static PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
    public InMemoryUserDetailsManager userDetailsService() {
        UserDetails user = User.builder()
                .username(username)
                .password(passwordEncoder().encode(password))
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(user);
    }

}
