package com.foodexpress;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.oauth2.core.oidc.*;
import org.springframework.security.oauth2.core.oidc.user.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import java.time.Instant;
import java.util.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.assertj.core.api.Assertions.*;
@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:foodtest;DB_CLOSE_DELAY=-1","spring.jpa.hibernate.ddl-auto=create-drop","app.demo.enabled=true"})
@AutoConfigureMockMvc @Transactional
class FoodDeliveryIntegrationTest {
 @Autowired MockMvc mvc;@Autowired ObjectMapper json;@Autowired MenuRepository menu;@Autowired OrderRepository orders;@Autowired UserRepository users;@Autowired RestaurantRepository restaurants;@Autowired CartRepository carts;@Autowired GoogleLoginService google;
 MenuItem first(){return menu.findAll().stream().filter(m->m.name.equals("Hyderabadi Chicken Biryani")).findFirst().orElseThrow();}
 String checkout(Long id,int quantity){return "{\"address\":\"12 Main Street, Kakinada 533001\",\"phone\":\"9876543210\",\"notes\":\"Doorbell please\",\"items\":[{\"menuItemId\":"+id+",\"quantity\":"+quantity+"}],\"total\":0}";}
 @Test void anonymousCanBrowseButCannotSeeOrdersOrAdmin()throws Exception{
  mvc.perform(get("/api/restaurants")).andExpect(status().isOk()).andExpect(jsonPath("$[0].name").exists());
  mvc.perform(get("/api/orders")).andExpect(status().isUnauthorized());
  mvc.perform(get("/api/admin/users")).andExpect(status().isUnauthorized());
  mvc.perform(get("/api/auth/me")).andExpect(jsonPath("$.authenticated").value(false));
 }
 @Test void registrationUsesCustomerRoleAndHidesPassword()throws Exception{
  mvc.perform(post("/api/auth/register").with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"New Customer\",\"email\":\"new@example.com\",\"password\":\"Secure123!\",\"role\":\"ADMIN\"}")).andExpect(status().isCreated());
  var u=users.findByEmail("new@example.com").orElseThrow();assertThat(u.role).isEqualTo("CUSTOMER");assertThat(u.password).startsWith("$2a$");
  mvc.perform(post("/api/auth/login").with(csrf()).param("email","new@example.com").param("password","wrongpass")).andExpect(status().isUnauthorized());
  mvc.perform(post("/api/auth/login").with(csrf()).param("email","new@example.com").param("password","Secure123!")).andExpect(status().isOk());
 }
 @Test @WithUserDetails("customer@foodexpress.com") void checkoutRecalculatesTotalAndClearsSavedCart()throws Exception{
  Long id=first().id;
  mvc.perform(put("/api/cart").with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"items\":[{\"menuItemId\":"+id+",\"quantity\":1}]}")).andExpect(status().isOk());
  mvc.perform(post("/api/orders").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(checkout(id,1))).andExpect(status().isCreated()).andExpect(jsonPath("$.subtotal").value(249)).andExpect(jsonPath("$.deliveryFee").value(35)).andExpect(jsonPath("$.total").value(284)).andExpect(jsonPath("$.paymentStatus").value("UNPAID"));
  mvc.perform(get("/api/cart")).andExpect(jsonPath("$.length()").value(0));
  mvc.perform(get("/api/auth/me")).andExpect(jsonPath("$.user.password").doesNotExist());
 }
 @Test @WithUserDetails("customer@foodexpress.com") void freeDeliveryAndPriceSnapshot()throws Exception{
  var response=mvc.perform(post("/api/orders").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(checkout(first().id,3))).andExpect(status().isCreated()).andExpect(jsonPath("$.total").value(747)).andExpect(jsonPath("$.deliveryFee").value(0)).andReturn();
  Long id=json.readTree(response.getResponse().getContentAsString()).get("id").asLong();MenuItem m=first();m.price=new java.math.BigDecimal("999");menu.save(m);
  assertThat(orders.findById(id).orElseThrow().items.get(0).unitPrice).isEqualByComparingTo("249");
 }
 @Test @WithUserDetails("customer@foodexpress.com") void csrfAndAdminAccessAreEnforced()throws Exception{
  mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(checkout(first().id,1))).andExpect(status().isForbidden());
  mvc.perform(get("/api/admin/orders")).andExpect(status().isForbidden());
  mvc.perform(post("/api/orders").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(checkout(first().id,0))).andExpect(status().isBadRequest());
 }
 @Test @WithUserDetails("customer@foodexpress.com") void rejectsMixedRestaurantsAndUnavailableItems()throws Exception{
  MenuItem a=first(),b=menu.findAll().stream().filter(m->!m.restaurantId.equals(a.restaurantId)).findFirst().orElseThrow();
  String body=checkout(a.id,1).replace("}]", "},{\"menuItemId\":"+b.id+",\"quantity\":1}]");
  mvc.perform(post("/api/orders").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isBadRequest());
  a.available=false;menu.save(a);
  mvc.perform(post("/api/orders").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(checkout(a.id,1))).andExpect(status().isBadRequest());
 }
 @Test @WithUserDetails("customer@foodexpress.com") void cannotCancelAnotherCustomersOrder()throws Exception{
  var u=users.findByEmail("admin@foodexpress.com").orElseThrow();FoodOrder o=new FoodOrder();o.customerId=u.id;o.restaurantId=first().restaurantId;o.restaurantName="Test";o=orders.save(o);
  mvc.perform(post("/api/orders/"+o.id+"/cancel").with(csrf())).andExpect(status().isNotFound());
 }
 @Test @WithUserDetails("admin@foodexpress.com") void adminStatusFlowAndRevenue()throws Exception{
  FoodOrder o=new FoodOrder();o.customerId=users.findByEmail("customer@foodexpress.com").orElseThrow().id;o.restaurantId=first().restaurantId;o.total=new java.math.BigDecimal("284");o=orders.save(o);
  mvc.perform(patch("/api/admin/orders/"+o.id+"/status").with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"DELIVERED\"}")).andExpect(status().isBadRequest());
  for(String s:List.of("CONFIRMED","PREPARING","OUT_FOR_DELIVERY","DELIVERED"))mvc.perform(patch("/api/admin/orders/"+o.id+"/status").with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"status\":\""+s+"\"}")).andExpect(status().isOk());
  mvc.perform(get("/api/admin/stats")).andExpect(jsonPath("$.revenue").value(284));
  assertThat(orders.findById(o.id).orElseThrow().paymentStatus).isEqualTo("COLLECTED");
 }
 @Test @WithUserDetails("admin@foodexpress.com") void categoryAndUserManagement()throws Exception{
  Long id=first().categoryId;
  mvc.perform(delete("/api/admin/categories/"+id).with(csrf())).andExpect(status().isConflict());
  mvc.perform(put("/api/admin/categories/"+id).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Rice Specials\"}")).andExpect(status().isOk());
  assertThat(first().category).isEqualTo("Rice Specials");
  Long customer=users.findByEmail("customer@foodexpress.com").orElseThrow().id;
  mvc.perform(patch("/api/admin/users/"+customer+"/enabled").with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"enabled\":false}")).andExpect(status().isOk());
  mvc.perform(post("/api/auth/login").with(csrf()).param("email","customer@foodexpress.com").param("password","Customer@123")).andExpect(status().isUnauthorized());
 }
 @Test void googleProvisioningRequiresVerifiedEmailAndNeverGrantsAdmin(){
  Map<String,Object> claims=new HashMap<>();claims.put("sub","google-test-subject");claims.put("email","google-user@example.com");claims.put("email_verified",true);claims.put("name","Google User");
  var token=new OidcIdToken("test-token",Instant.now(),Instant.now().plusSeconds(60),claims);
  OidcUser result=google.provision(new DefaultOidcUser(List.of(new SimpleGrantedAuthority("ROLE_USER")),token));
  assertThat(result.getName()).isEqualTo("google-user@example.com");assertThat(users.findByEmail(result.getName()).orElseThrow().role).isEqualTo("CUSTOMER");
  claims.put("email_verified",false);var unverified=new OidcIdToken("test-token",Instant.now(),Instant.now().plusSeconds(60),claims);
  assertThatThrownBy(()->google.provision(new DefaultOidcUser(List.of(new SimpleGrantedAuthority("ROLE_USER")),unverified))).isInstanceOf(OAuth2AuthenticationException.class);
 }
}
