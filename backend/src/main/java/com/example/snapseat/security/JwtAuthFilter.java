
package com.example.snapseat.security;

import com.example.snapseat.user.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService js;
    private final UserRepository userRepository;

    public JwtAuthFilter(JwtService js, UserRepository userRepository) {
        this.js = js;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest req,
            HttpServletResponse res,
            FilterChain chain
    ) throws ServletException, IOException {

        String h = req.getHeader("Authorization");

        if (h == null || !h.startsWith("Bearer ")) {
            chain.doFilter(req, res);
            return;
        }

        try {
            String email = js.extractEmail(h.substring(7));

            var dbUser = userRepository.findByEmail(email)
                    .orElse(null);

            if (dbUser == null) {
                chain.doFilter(req, res);
                return;
            }

            String role = "ROLE_" + dbUser.getRole().name();

            var u = User.withUsername(email)
                    .password("")
                    .authorities(List.of(
                            new SimpleGrantedAuthority(role)
                    ))
                    .build();

            var a = new UsernamePasswordAuthenticationToken(
                    u,
                    null,
                    u.getAuthorities()
            );

            a.setDetails(
                    new WebAuthenticationDetailsSource()
                            .buildDetails(req)
            );

            SecurityContextHolder.getContext().setAuthentication(a);

        } catch (Exception e) {
            SecurityContextHolder.clearContext();
        }

        chain.doFilter(req, res);
    }
}
