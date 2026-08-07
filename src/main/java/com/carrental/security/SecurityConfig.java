package com.carrental.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

/**
 * Central Spring Security configuration.
 *
 * NOTE ON CSRF: the existing hand-built Thymeleaf templates in this project post to
 * several plain (non csrf-token) forms and even use plain GET links for a few admin
 * actions (delete/enable/disable). To keep the pre-built UI working without having to
 * retrofit every single template with a hidden csrf token, CSRF protection is disabled
 * here. For a hardened production deployment you should re-enable it and add
 * <code>&lt;input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}"/&gt;</code>
 * to every state changing form.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Autowired
	private CustomUserDetailsService userDetailsService;

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
		provider.setPasswordEncoder(passwordEncoder());
		return provider;
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
		return configuration.getAuthenticationManager();
	}

	@Bean
	public AuthenticationSuccessHandler successHandler() {
		return (request, response, authentication) -> {
			boolean isAdmin = authentication.getAuthorities().stream()
					.anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
			response.sendRedirect(isAdmin ? "/admin/dashboard" : "/user/dashboard");
		};
	}

	@Bean
	public AuthenticationFailureHandler failureHandler() {
		return (request, response, exception) -> {
			String reason = exception instanceof DisabledException ? "disabled" : "invalid";
			response.sendRedirect("/login?error=" + reason);
		};
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(
						"/", "/home", "/login", "/register", "/check-email", "/verify-email",
						"/forgot-password", "/verify-otp", "/verify-reset-otp", "/change-password",
						"/access-denied",
						"/css/**", "/js/**", "/images/**", "/uploads/**", "/webjars/**", "/favicon.ico")
					.permitAll()
				.requestMatchers("/admin/**").hasRole("ADMIN")
				.requestMatchers("/user/**").hasRole("USER")
				.anyRequest().authenticated()
			)
			.formLogin(form -> form
				.loginPage("/login")
				.loginProcessingUrl("/login")
				.usernameParameter("username")
				.passwordParameter("password")
				.successHandler(successHandler())
				.failureHandler(failureHandler())
				.permitAll()
			)
			.logout(logout -> logout
				.logoutUrl("/logout")
				.logoutSuccessUrl("/login?logout=true")
				.invalidateHttpSession(true)
				.deleteCookies("JSESSIONID")
				.permitAll()
			)
			.rememberMe(remember -> remember
				.key("carRentalRememberMeKey")
				.rememberMeParameter("remember-me")
				.tokenValiditySeconds(1209600)
			)
			.exceptionHandling(ex -> ex.accessDeniedPage("/access-denied"))
			.authenticationProvider(authenticationProvider())
			.csrf(csrf -> csrf.disable());

		return http.build();
	}
}
