
package com.example.snapseat;

import com.example.snapseat.auth.AuthService;
import com.example.snapseat.auth.dto.RegisterRequest;
import com.example.snapseat.security.JwtService;
import com.example.snapseat.user.User;
import com.example.snapseat.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

	private UserRepository ur;
	private PasswordEncoder pe;
	private JwtService js;
	private AuthService as;

	@BeforeEach
	void setUp() {
		ur = mock(UserRepository.class);
		pe = mock(PasswordEncoder.class);
		js = mock(JwtService.class);
		as = new AuthService(ur, pe, js);
	}

	@Test
	void registerSuccess() {
		RegisterRequest req = new RegisterRequest();
		req.setUsername("Alex");
		req.setEmail("alex@gmail.com");
		req.setPassword("password123");

		when(ur.existsByEmail("alex@gmail.com")).thenReturn(false);
		when(pe.encode("password123")).thenReturn("hashedPassword");

		String res = as.register(req);

		assertEquals("Registration successful", res);
		verify(ur).save(any(User.class));
	}

	@Test
	void registerDuplicateEmail() {
		RegisterRequest req = new RegisterRequest();
		req.setEmail("alex@gmail.com");

		when(ur.existsByEmail("alex@gmail.com")).thenReturn(true);

		assertThrows(RuntimeException.class, () -> as.register(req));

		verify(ur, never()).save(any(User.class));
	}

	@Test
	void loginSuccess() {
		User u = new User();
		u.setEmail("alex@gmail.com");
		u.setPasswordHash("hashedPassword");

		when(ur.findByEmail("alex@gmail.com")).thenReturn(Optional.of(u));
		when(pe.matches("password123", "hashedPassword")).thenReturn(true);
		when(js.generateToken("alex@gmail.com")).thenReturn("test.jwt.token");

		var req = new com.example.snapseat.auth.dto.LoginRequest();
		req.setEmail("alex@gmail.com");
		req.setPassword("password123");

		String res = as.login(req);

		assertEquals("test.jwt.token", res);
	}

	@Test
	void loginWrongPassword() {
		User u = new User();
		u.setEmail("alex@gmail.com");
		u.setPasswordHash("hashedPassword");

		when(ur.findByEmail("alex@gmail.com")).thenReturn(Optional.of(u));
		when(pe.matches("wrongPassword", "hashedPassword")).thenReturn(false);

		var req = new com.example.snapseat.auth.dto.LoginRequest();
		req.setEmail("alex@gmail.com");
		req.setPassword("wrongPassword");

		assertThrows(RuntimeException.class, () -> as.login(req));
	}
}
