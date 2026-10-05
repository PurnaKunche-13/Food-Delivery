package com.foodexpress;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("google")
@SpringBootTest(properties={"GOOGLE_CLIENT_ID=test-client.apps.googleusercontent.com",
 "GOOGLE_CLIENT_SECRET=test-secret", "spring.datasource.url=jdbc:h2:mem:googletest",
 "spring.jpa.hibernate.ddl-auto=create-drop", "app.demo.enabled=false"})
@AutoConfigureMockMvc
class GoogleLoginIntegrationTest {
 @Autowired MockMvc mvc;

 @Test void googleButtonHasConfiguredAuthorizationEndpoint() throws Exception {
  mvc.perform(get("/api/auth/providers")).andExpect(status().isOk())
   .andExpect(jsonPath("$.googleEnabled").value(true));
  var result=mvc.perform(get("/oauth2/authorization/google"))
   .andExpect(status().isFound()).andReturn();
  String redirect=result.getResponse().getRedirectedUrl();
  assertThat(redirect).startsWith("https://accounts.google.com/o/oauth2/v2/auth?")
   .contains("client_id=test-client.apps.googleusercontent.com", "state=",
    "redirect_uri=http://localhost/login/oauth2/code/google", "scope=openid")
   .doesNotContain("test-secret");
  assertThat(result.getRequest().getSession(false)).isNotNull();
 }

 @Test void cancelledGoogleLoginReturnsToApplication() throws Exception {
  var request=mvc.perform(get("/oauth2/authorization/google")).andReturn();
  var session=(org.springframework.mock.web.MockHttpSession)request.getRequest().getSession(false);
  mvc.perform(get("/login/oauth2/code/google").session(session)
   .param("error","access_denied").param("state","invalid-state"))
   .andExpect(status().isFound()).andExpect(redirectedUrl("/?login=failed"));
 }
}
