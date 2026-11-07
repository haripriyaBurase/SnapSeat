package com.example.snapseat.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthFilter extends OncePerRequestFilter{
    private final JwtService js;

    public JwtAuthFilter(JwtService js) {
        this.js = js;
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

            var u = User.withUsername(email)
                    .password("")
                    .authorities(Collections.emptyList())
                    .build();

            var a = new UsernamePasswordAuthenticationToken(
                    u, null, u.getAuthorities()
            );

            a.setDetails(new WebAuthenticationDetailsSource()
                    .buildDetails(req));

            SecurityContextHolder.getContext().setAuthentication(a);

        } catch (Exception e) {
            SecurityContextHolder.clearContext();
        }

        chain.doFilter(req, res);
    }
}
