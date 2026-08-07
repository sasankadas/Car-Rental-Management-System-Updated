package com.carrental.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.carrental.entity.User;

public class CustomUserDetails implements UserDetails {

	private static final long serialVersionUID = 1L;

	private final User user;

	public CustomUserDetails(User user) {
		this.user = user;
	}

	public User getUser() {
		return user;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		String role = user.getRole() == null ? "USER" : user.getRole().trim().toUpperCase();
		// Be defensive: some rows created before the security rewrite (or edited directly
		// in the DB) may already store the "ROLE_" prefix (e.g. "ROLE_USER" instead of
		// "USER"). Normalise so we never end up with "ROLE_ROLE_USER", which would silently
		// fail every hasRole("USER")/hasRole("ADMIN") check and show "Access Denied".
		if (role.startsWith("ROLE_")) {
			role = role.substring(5);
		}
		return List.of(new SimpleGrantedAuthority("ROLE_" + role));
	}

	@Override
	public String getPassword() {
		return user.getPassword();
	}

	@Override
	public String getUsername() {
		return user.getEmail();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return user.isEnabled();
	}
}
