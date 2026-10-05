package com.foodexpress;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
@Configuration
public class SecurityConfig {
    @Bean PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean UserDetailsService users(UserRepository repository) {
        return email -> {
            AppUser u=repository.findByEmail(email.trim().toLowerCase(java.util.Locale.ROOT)).orElseThrow(()->new UsernameNotFoundException("Invalid credentials"));
            return User.withUsername(u.email).password(u.password).roles(u.role).disabled(!u.enabled).build();
        };
    }
    @Bean SecurityFilterChain security(HttpSecurity http,org.springframework.beans.factory.ObjectProvider<org.springframework.security.oauth2.client.registration.ClientRegistrationRepository> registrations,GoogleLoginService google,UserRepository users) throws Exception {
        http.authorizeHttpRequests(a->a
        .requestMatchers("/","/index.html","/app.js","/style.css","/favicon.svg","/error","/oauth2/**","/login/oauth2/**","/api/auth/providers","/api/auth/csrf","/api/auth/me","/api/auth/register").permitAll()
        .requestMatchers(org.springframework.http.HttpMethod.GET,"/api/restaurants/**").permitAll()
        .requestMatchers("/api/admin/**").hasRole("ADMIN").anyRequest().authenticated())
        .formLogin(f->f.loginProcessingUrl("/api/auth/login").usernameParameter("email")
        .successHandler((req,res,auth)-> {
            res.setContentType("application/json");res.getWriter().write("{\"message\":\"Logged in\"}");
        })
        .failureHandler((req,res,ex)-> {
            res.setStatus(401);res.setContentType("application/json");res.getWriter().write("{\"message\":\"Incorrect email or password\"}");
        }).permitAll())
        .logout(l->l.logoutUrl("/api/auth/logout").logoutSuccessHandler((req,res,auth)-> {
            res.setContentType("application/json");res.getWriter().write("{\"message\":\"Logged out\"}");
        }))
        .exceptionHandling(e->e.authenticationEntryPoint((req,res,ex)-> {
            res.setStatus(401);res.setContentType("application/json");res.getWriter().write("{\"message\":\"Please log in\"}");
        })
        .accessDeniedHandler((req,res,ex)-> {
            res.setStatus(403);res.setContentType("application/json");res.getWriter().write("{\"message\":\"Access denied or session expired. Refresh the page.\"}");
        }));
        if(registrations.getIfAvailable()!=null)http.oauth2Login(o->o.userInfoEndpoint(info->info.oidcUserService(google::load))
        .successHandler((req,res,auth)->res.sendRedirect("/?login=google"))
        .failureHandler((req,res,e)->res.sendRedirect("/?login=failed")));
        http.addFilterAfter(new AccountStatusFilter(users),org.springframework.security.web.authentication.AnonymousAuthenticationFilter.class);
        return http.build();
    }
}
