package com.example.Bai6.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.core.userdetails.UserDetailsService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Autowired
	@Qualifier("accountService")
	private UserDetailsService userDetailsService;

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {

		AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);

		builder
				.userDetailsService(userDetailsService)
				.passwordEncoder(passwordEncoder());

		return builder.build();
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

		http
				.authorizeHttpRequests(auth -> auth

						.requestMatchers("/", "/home", "/login", "/access-denied", "/css/**", "/js/**", "/favicon.ico")
						.permitAll()

						.requestMatchers("/products/add",
								"/products/edit/**",
								"/products/delete/**",
								"/products/update/**")
						.hasRole("ADMIN")

						.requestMatchers("/products/**")
						.hasAnyRole("ADMIN", "USER")

						.anyRequest()
						.authenticated())

				.formLogin(form -> form
						.loginPage("/login")
						.loginProcessingUrl("/login")
						.defaultSuccessUrl("/products", true)
						.failureUrl("/login?error=true")
						.permitAll())

				.logout(logout -> logout
						.logoutUrl("/logout")
						.logoutSuccessUrl("/login")
						.permitAll())
				.exceptionHandling(ex -> ex
						.accessDeniedPage("/access-denied"))

				.csrf(csrf -> csrf.disable());

		return http.build();
	}
}