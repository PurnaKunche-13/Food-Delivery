package com.foodexpress;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import java.io.IOException;
public class AccountStatusFilter extends OncePerRequestFilter {
    private final UserRepository users;
    public AccountStatusFilter(UserRepository users) {
        this.users=users;
    }
    @Override protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain)throws ServletException,IOException {
        var auth=SecurityContextHolder.getContext().getAuthentication();
        if(auth!=null&&auth.isAuthenticated()&&!(auth instanceof AnonymousAuthenticationToken)&&req.getRequestURI().startsWith("/api/")) {
            var user=users.findByEmail(auth.getName());
            if(user.isEmpty()||!user.get().enabled)
            {
                SecurityContextHolder.clearContext();
                var session=req.getSession(false);
                if(session!=null)session.invalidate();
                res.setStatus(403);
                res.setContentType("application/json");
                res.getWriter().write("{\"message\":\"Account disabled. Contact the administrator.\"}");
                return;
            }
        }
        chain.doFilter(req,res);
    }
}
