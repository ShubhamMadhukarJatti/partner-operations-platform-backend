package com.sharkdom.security;


import com.sharkdom.constants.ErrorMessages;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

import static com.sharkdom.security.ApplicationSecurityConfig.AUTH_WHITELIST;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private static final AntPathMatcher pathMatcher = new AntPathMatcher();


    public JwtAuthenticationFilter(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Value("${app.security.enabled}")
    private boolean securityEnabled;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {


        if (!securityEnabled) {
            filterChain.doFilter(request, response);
            return;
        }
        String path = request.getRequestURI();

        if (isUnsecuredEndpoint(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if(path.contains("swagger-ui/index.html")) {
            filterChain.doFilter(request, response);
            return;
        }
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            handleErrorResponse(response, ErrorMessages.SH02);
            return;
        }

        Optional<String> accessToken = Optional.of(authorizationHeader.substring(7));

        var token = accessToken.get();
        try {
            String username = jwtUtil.extractUsername(token);
            if (username != null && jwtUtil.validateToken(token, username)) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // Create authentication token with user details
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
System.out.println("AcessToken"+accessToken);
                // Set the authentication in the security context
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                log.error("Token is invalid");
                handleErrorResponse(response, ErrorMessages.SH01);
                return;
            }
        } catch (Exception e) {
            log.error("Exception occurred while validating token {}", e.getMessage(), e);
            handleErrorResponse(response, ErrorMessages.SH01);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void handleErrorResponse(HttpServletResponse response, ErrorMessages errorMessage) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        String resp = createResponseBody(errorMessage.name(), errorMessage.getMessage());
        response.getWriter().write(resp);
    }


    private String createResponseBody(String errorCode, String errorMessage) {
        return String.format("""
                {
                    "errorCode": "%s",
                    "errorMessage": "%s"
                }""", errorCode, errorMessage);
    }

    private boolean isUnsecuredEndpoint(String path) {
        for (String unsecuredPath : AUTH_WHITELIST) {
            if (pathMatcher.match(unsecuredPath, path)) {
                return true;
            }
        }
        return path.contains("webjars") || path.contains("/v3/api-docs");
    }
}

