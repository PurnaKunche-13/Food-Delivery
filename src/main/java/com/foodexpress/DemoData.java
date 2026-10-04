package com.foodexpress;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.math.BigDecimal;
@Component
public class DemoData implements CommandLineRunner {
 private final UserRepository users;private final RestaurantRepository restaurants;private final MenuRepository menu;private final PasswordEncoder encoder;private final CategoryRepository categories;
 @Value("${app.demo.enabled}") boolean demo;
 @Value("${app.admin.email}") String adminEmail;
 @Value("${app.admin.password}") String adminPassword;
 public DemoData(UserRepository u,RestaurantRepository r,MenuRepository m,PasswordEncoder p,CategoryRepository c){categories=c;users=u;restaurants=r;menu=m;encoder=p;}
 public void run(String... args){
  if(adminPassword.length()<8)throw new IllegalStateException("ADMIN_PASSWORD must contain at least 8 characters");
  createUser("Administrator",adminEmail,adminPassword,"ADMIN");
  if(!demo)return;createUser("Demo Customer","customer@foodexpress.com","Customer@123","CUSTOMER");
  if(restaurants.count()>0)return;
  Restaurant r=restaurant("Andhra Kitchen","Andhra • Biryani","Main Road, Kakinada","🍛");
  item(r,"Hyderabadi Chicken Biryani","Slow-cooked basmati rice, tender chicken and fragrant spices.",249,"Biryani","🍛",false);
  item(r,"Veg Biryani","Seasonal vegetables, aromatic rice and cooling raita.",179,"Biryani","🍚",true);
  item(r,"Andhra Meals","Rice, dal, sambar, seasonal curry, pickle and curd.",149,"Meals","🥘",true);
  item(r,"Chicken 65","Crispy spicy chicken with curry leaves.",199,"Starters","🍗",false);
  item(r,"Paneer Pepper Fry","Paneer cubes tossed with peppers and onions.",189,"Starters","🧀",true);
  item(r,"Sweet Lassi","Chilled yogurt drink with a touch of sweetness.",69,"Drinks","🥛",true);
  r=restaurant("The Pizza Studio","Italian • Pizza","City Centre, Hyderabad","🍕");
  item(r,"Margherita Pizza","Classic tomato, mozzarella and fresh basil.",229,"Pizza","🍕",true);
  item(r,"Farmhouse Pizza","Mushrooms, peppers, onions and golden cheese.",299,"Pizza","🍕",true);
  item(r,"Chicken Supreme","Chicken, olives and a rich tomato sauce.",349,"Pizza","🍕",false);
  item(r,"Garlic Bread","Warm toasted bread with garlic butter.",119,"Sides","🥖",true);
  r=restaurant("Burger Junction","American • Burgers","Market Street, Eluru","🍔");
  item(r,"Classic Veg Burger","Crispy veggie patty, lettuce and house sauce.",129,"Burgers","🍔",true);
  item(r,"Chicken Crunch Burger","Golden chicken fillet with slaw and mayo.",169,"Burgers","🍔",false);
  item(r,"Peri Peri Fries","Crispy fries with our signature spice blend.",99,"Sides","🍟",true);
  item(r,"Chocolate Shake","Rich chocolate blended with chilled milk.",129,"Drinks","🥤",true);
  r=restaurant("Tiffin Corner","South Indian • Breakfast","Temple Road, Tirupati","🥞");
  item(r,"Masala Dosa","Crisp dosa with spiced potato, chutney and sambar.",89,"Breakfast","🥞",true);
  item(r,"Idli Sambar","Four soft idlis with fresh chutney and sambar.",69,"Breakfast","🍽️",true);
  item(r,"Pesarattu","Andhra green gram dosa with ginger chutney.",99,"Breakfast","🥞",true);
  item(r,"Filter Coffee","Traditional South Indian filter coffee.",39,"Drinks","☕",true);
 }
 private void createUser(String name,String email,String password,String role){email=email.strip().toLowerCase(java.util.Locale.ROOT);if(users.findByEmail(email).isEmpty()){AppUser u=new AppUser();u.name=name;u.email=email;u.password=encoder.encode(password);u.role=role;users.save(u);}}
 private Restaurant restaurant(String name,String cuisine,String address,String emoji){Restaurant r=new Restaurant();r.name=name;r.cuisine=cuisine;r.address=address;r.emoji=emoji;return restaurants.save(r);}
 private void item(Restaurant r,String name,String desc,int price,String category,String emoji,boolean veg){MenuItem m=new MenuItem();m.restaurantId=r.id;m.name=name;m.description=desc;m.price=BigDecimal.valueOf(price);m.category=category;Category c=categories.findByNameIgnoreCase(category).orElseGet(()->{Category n=new Category();n.name=category;return categories.save(n);});m.categoryId=c.id;m.emoji=emoji;m.vegetarian=veg;menu.save(m);}
}
