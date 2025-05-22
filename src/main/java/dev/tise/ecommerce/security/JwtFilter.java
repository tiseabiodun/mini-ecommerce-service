package dev.tise.ecommerce.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Configuration
public class JwtFilter extends OncePerRequestFilter {

    private final JwtTokenGenerator tokenGenerator;

    private final CustomUserDetails customUserDetails;

    public JwtFilter(JwtTokenGenerator tokenGenerator, CustomUserDetails customUserDetails) {
        this.tokenGenerator = tokenGenerator;
        this.customUserDetails = customUserDetails;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        System.err.println("request path: " + request.getServletPath());
//         ||
//
        if (request.getServletPath().equals("/api/ecommerce/user/login-user") || request.getServletPath().equals("/api/ecommerce/user/validate-user") || request.getServletPath().equals("/api/ecommerce/user/create-user")) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwtToken = request.getHeader("Authorization");
        System.err.println("Token with Bearer : "+jwtToken);
        if (Objects.isNull(jwtToken) || !jwtToken.startsWith("Bearer ")) {
            writeError("Missing or Invalid Authorization Header", response, UNAUTHORIZED);
            return;
        }

        try {
            jwtToken = jwtToken.substring(7);
            System.err.println("After substring : "+jwtToken);
            String username = tokenGenerator.extractUsername(jwtToken);
            System.err.println("Fetched user from token : "+username);

            if (username != null && !username.isEmpty()) {
                var userDetails = customUserDetails.loadUserByUsername(username);
                boolean isTokenValid = tokenGenerator.isTokenValid(jwtToken, userDetails.getUsername());

                if (isTokenValid) {
                    System.err.println("Token is valid...");
                    System.err.println(userDetails.getAuthorities());
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(username, null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    filterChain.doFilter(request,response);

                } else {
                    writeError("Invalid Token", response, UNAUTHORIZED);
                    return;
                }

            }
        } catch (Exception e) {
            if (e.getMessage().contains("The token has expired") || e.getMessage().contains("Bad Credentials")) {
                writeError(e.getMessage(), response, UNAUTHORIZED);
            } else {
                writeError(e.getMessage(), response, FORBIDDEN);
            }

        }


    }

    private void writeError(String message, HttpServletResponse response, HttpStatus httpStatus) throws IOException {
        System.err.println("Error in auth filter ==> " + message);
        response.setStatus(httpStatus.value());
        response.setContentType(APPLICATION_JSON_VALUE);
        Map<String, String> error = new HashMap<>();
        error.put("error message: ", message);
        new ObjectMapper().writeValue(response.getOutputStream(), error);
    }
}
