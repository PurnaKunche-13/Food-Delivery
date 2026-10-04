package com.foodexpress;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
@RestController @RequestMapping("/api/auth")
public class AuthController {
 private final UserRepository users; private final PasswordEncoder encoder;
 public AuthController(UserRepository users,PasswordEncoder encoder) {this.users=users;this.encoder=encoder;}
 @org.springframework.beans.factory.annotation.Autowired private org.springframework.beans.factory.ObjectProvider<org.springframework.security.oauth2.client.registration.ClientRegistrationRepository> registrations;
 @GetMapping("/providers") public Map<String,Boolean> providers(){return Map.of("googleEnabled",registrations.getIfAvailable()!=null&&registrations.getIfAvailable().findByRegistrationId("google")!=null);}
 @GetMapping("/csrf") public Map<String,String> csrf(CsrfToken token) {return Map.of("token",token.getToken(),"headerName",token.getHeaderName());}
 @GetMapping("/me") public Object me(Authentication auth) {return auth==null?Map.of("authenticated",false):Map.of("authenticated",true,"user",users.findByEmail(auth.getName()).orElseThrow());}
 @PostMapping("/register") @ResponseStatus(HttpStatus.CREATED) public Map<String,String> register(@Valid @RequestBody Requests.Registration data) {
  String email=data.email().strip().toLowerCase(Locale.ROOT);
  if(users.findByEmail(email).isPresent()) throw new ResponseStatusException(HttpStatus.CONFLICT,"Email already registered");
  AppUser u=new AppUser();u.name=data.name().strip();u.email=email;u.password=encoder.encode(data.password());users.save(u);
  return Map.of("message","Account created. You can now log in.");
 }
}
