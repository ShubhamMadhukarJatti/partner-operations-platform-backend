package com.sharkdom.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import static org.springframework.security.config.Customizer.withDefaults;


@Configuration
@EnableWebSecurity
public class ApplicationSecurityConfig {

    @Value("${swagger.username}")
    private String swaggerUsername;
    @Value("${swagger.password}")
    private String swaggerPassword;

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    protected static final String[] AUTH_WHITELIST = {
            "/",
            "/service/**",
            "/instamojo/webhook",
            "/transaction/save",
            "/payment/stripe/callback",
            "/payment/callback",
            "/meetings/callback",
            "/email/sns",
            "/mou/callback",
            "/typeForm/callback",
            "/typeForm/webhook",
            "/sharkdom-stripe/v1/webhook/subscription",
            "/phonepe/callback",
            "/hyperverge/webhook",

            "/user/additionalDetails/get",

            // Swagger
            "/v2/api-docs",
            "/swagger-resources",
            "/swagger-resources/**",
            "/configuration/ui",
            "/configuration/security",
            "/swagger-ui.html",
            "/webjars/**",
            "/swagger-ui/swagger-ui.css",
            "/swagger-ui/swagger-ui-bundle.js",
            "/swagger-ui/index.css",
            "/swagger-ui/swagger-initializer.js",
            "/swagger-ui/swagger-ui-standalone-preset.js",
            "/swagger-ui/favicon-32x32.png",
            "/swagger-ui/favicon-16x16.png",
            "/v3/api-docs/**",
            "/v3/api-docs/swagger-config",

            // Chatbot & Public APIs
            "/chatbot",
            "/chatbot/schedule-meeting",
            "/chatbot/**",
            "/organization/search",
            "/organization/details",
            "/organization/searchPartial",
            "/organization/searchByPartialName",
            "/organization/marketplace",

            "/email/verify",
            "/email/resetPassword",
            "/email/sendOne",
            "/organization/emailUnsubscribe",
            "/email/subscribe",

            "/referral/lead",
            "/referral/impression",

            "organizationCollaboration/timeline",
            "organizationCollaboration/isEmailOpened",
            "organizationCollaboration/isEmailClicked",

            "/demo-book",
            "/persona",
            "/public-profile",
            "/ws",

            // User APIs
            "/v1/users/login",
            "/v1/users/partner-portal/login",
            "/v1/users/direct/login",
            "/v1/users/register",
            "/v1/users/verify",
            "/v1/users/refresh-token",
            "/v1/users/chatbot/ask",
            "/v1/users/dns/form-url",
            "/v1/users/dns/form-id",
            "/v1/users/external/partner/verify",

            // Webhooks
            "/zoho/sign-callback",
            "/slack/interaction",
            "/receive-webhook/**",
            "/oauth/**",
            "/mailgun/webhook",
            "/mailgun/data",

            // PPI
            "/ppi/no/auth/fetchQuestionByFormId",
            "/ppi/counter",
            "/ppi/response/internalForm/external/save",
            "/ppi/counter/external/increment",

            // NEW — External Branding API
            "/ppi/org/branding/external/user/**",
            "/api/onboarding/start",
            "/api/onboarding/website/check",
            "/api/onboarding/complete",
            "/payment/reseller/stripe/callback",
            "/api/no/auth/**",
            "/api/talent/network/**",
            "/v1/users/admin/refreshToken/data",
            "/api/company-partner-applications/**",
            "/api/consultant-partner-applications/**",
            "/api/v1/free-trial-subscription-plans/**",
            "/api/v1/partner/application",
            "/api/v1/partner/application/**",
            "/api/v1/partner/applications",
            "/api/v1/partner/auth/send-otp",
            "/api/v1/partner/auth/verify-otp",
            "/api/v1/partner/auth/login",
            "/api/v1/partner/approve",
            "/api/v1/partner/disable",
            "/api/v1/partner/enable",
            "/api/webhooks/zoho/**",
            "/api/partner-leads/**"

    };


    public ApplicationSecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public WebSecurityCustomizer configure() {
        return web -> web.ignoring().requestMatchers(AUTH_WHITELIST);
    }

    @Bean
    @Order(1)
    SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        return http.securityMatcher("/swagger-ui/index.html")
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(withDefaults()).build();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication().withUser(swaggerUsername).password("{noop}" + swaggerPassword).roles("USER");
    }

    @Bean
    @Order(3)
    @ConditionalOnProperty(prefix = "app.security", name = "enabled", havingValue = "true")
    SecurityFilterChain prodConfig(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(httpSecuritySessionManagementConfigurer -> httpSecuritySessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterAfter(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(authorizationManagerRequestMatcherRegistry -> authorizationManagerRequestMatcherRegistry
                        .requestMatchers(HttpMethod.GET, "/swagger-ui/**").permitAll()
                        .requestMatchers("/**").authenticated())
                .exceptionHandling(httpSecurityExceptionHandlingConfigurer -> httpSecurityExceptionHandlingConfigurer
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED)));
        return http.build();
    }




    @Bean
    @Order(2)
    @ConditionalOnProperty(prefix = "app.security", name = "enabled", havingValue = "false")
    SecurityFilterChain devConfig(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(httpSecuritySessionManagementConfigurer -> httpSecuritySessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorizationManagerRequestMatcherRegistry -> authorizationManagerRequestMatcherRegistry
                        .anyRequest().permitAll());

        return http.build();
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

}