package com.foodexpress;
import org.springframework.stereotype.Service;
import org.springframework.security.oauth2.client.oidc.userinfo.*;
import org.springframework.security.oauth2.core.oidc.user.*;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.*;
@Service
public class GoogleLoginService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    public GoogleLoginService(UserRepository u,PasswordEncoder p) {
        users=u;
        encoder=p;
    }
    public OidcUser load(OidcUserRequest request) {
        return provision(new OidcUserService().loadUser(request));
    }
    public OidcUser provision(OidcUser principal) {
        if(!Boolean.TRUE.equals(principal.getEmailVerified())||principal.getEmail()==null)throw new OAuth2AuthenticationException(new OAuth2Error("invalid_user"),"A verified Google email is required");
        String email=principal.getEmail().strip().toLowerCase(Locale.ROOT);
        AppUser u=users.findByEmail(email).orElseGet(()-> {
            AppUser n=new AppUser();n.email=email;n.name=Optional.ofNullable(principal.getFullName()).orElse("Google Customer");n.password=encoder.encode(UUID.randomUUID().toString());return n;
        });
        if(!u.enabled)throw new OAuth2AuthenticationException(new OAuth2Error("access_denied"),"Account disabled");
        if(u.googleSubject!=null&&!u.googleSubject.equals(principal.getSubject()))throw new OAuth2AuthenticationException(new OAuth2Error("invalid_user"),"Google account does not match");
        u.googleSubject=principal.getSubject();
        u=users.save(u);
        Map<String,Object> claims=new HashMap<>(principal.getClaims());
        claims.put("email",email);
        org.springframework.security.oauth2.core.oidc.OidcUserInfo info=new org.springframework.security.oauth2.core.oidc.OidcUserInfo(claims);
        return new DefaultOidcUser(List.of(new SimpleGrantedAuthority("ROLE_"+u.role)),principal.getIdToken(),info,"email");
    }
}
