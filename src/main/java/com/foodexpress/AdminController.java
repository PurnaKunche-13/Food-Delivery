package com.foodexpress;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.math.BigDecimal;
import java.util.*;
@RestController @RequestMapping("/api/admin")
public class AdminController {
    private final RestaurantRepository restaurants;
    private final MenuRepository menu;
    private final OrderRepository orders;
    private final UserRepository users;
    private final OrderService service;
    private final CategoryRepository categories;
    public AdminController(RestaurantRepository r,MenuRepository m,OrderRepository o,UserRepository u,OrderService s,CategoryRepository c) {
        categories=c;
        restaurants=r;
        menu=m;
        orders=o;
        users=u;
        service=s;
    }
    @GetMapping("/stats")
    public Map<String,Object> stats()
    {
        List<FoodOrder> all=orders.findAll();
        return Map.of("orders",all.size(),"customers",users.findAll().stream().filter(u->u.role.equals("CUSTOMER")).count(),"activeOrders",all.stream().filter(o->!List.of("CANCELLED","DELIVERED").contains(o.status)).count(),"revenue",all.stream().filter(o->o.status.equals("DELIVERED")).map(o->o.total).reduce(BigDecimal.ZERO,BigDecimal::add));
    }
    @GetMapping("/orders") public List<FoodOrder> orders() {
        return orders.findAllByOrderByCreatedAtDesc();
    }
    @PatchMapping("/orders/{id}/status") public FoodOrder status(@PathVariable Long id,@Valid @RequestBody Requests.StatusInput d) {
        return service.changeStatus(id,d.status());
    }
    @GetMapping("/restaurants") public List<Restaurant> restaurants() {
        return restaurants.findAllByOrderByIdAsc();
    }
    @PostMapping("/restaurants") @ResponseStatus(HttpStatus.CREATED) public Restaurant createRestaurant(@Valid @RequestBody Requests.RestaurantInput d) {
        return saveRestaurant(new Restaurant(),d);
    }
    @PutMapping("/restaurants/{id}") public Restaurant editRestaurant(@PathVariable Long id,@Valid @RequestBody Requests.RestaurantInput d) {
        return saveRestaurant(restaurants.findById(id).orElseThrow(()->missing()),d);
    }
    private Restaurant saveRestaurant(Restaurant r,Requests.RestaurantInput d) {
        r.name=d.name().strip();
        r.cuisine=d.cuisine();
        r.address=d.address();
        r.emoji=d.emoji();
        r.active=d.active();
        return restaurants.save(r);
    }
    @GetMapping("/menu") public List<MenuItem> menu() {
        return menu.findAll();
    }
    @PostMapping("/menu") @ResponseStatus(HttpStatus.CREATED) public MenuItem createMenu(@Valid @RequestBody Requests.MenuInput d) {
        return saveMenu(new MenuItem(),d);
    }
    @PutMapping("/menu/{id}") public MenuItem editMenu(@PathVariable Long id,@Valid @RequestBody Requests.MenuInput d) {
        return saveMenu(menu.findById(id).orElseThrow(()->missing()),d);
    }
    private MenuItem saveMenu(MenuItem m,Requests.MenuInput d) {
        if(!restaurants.existsById(d.restaurantId()))throw missing();
        m.restaurantId=d.restaurantId();
        m.name=d.name().strip();
        m.description=d.description();
        m.price=d.price();
        Category c=categories.findById(d.categoryId()).orElseThrow(()->missing());
        m.category=c.name;
        m.categoryId=c.id;
        m.emoji=d.emoji();
        m.vegetarian=d.vegetarian();
        m.available=d.available();
        return menu.save(m);
    }
    @GetMapping("/categories") public List<Category> categories() {
        return categories.findAll();
    }
    @PostMapping("/categories") @ResponseStatus(HttpStatus.CREATED) public Category createCategory(@Valid @RequestBody Requests.CategoryInput d) {
        return saveCategory(new Category(),d);
    }
    @PutMapping("/categories/{id}") @org.springframework.transaction.annotation.Transactional public Category editCategory(@PathVariable Long id,@Valid @RequestBody Requests.CategoryInput d) {
        Category c=saveCategory(categories.findById(id).orElseThrow(()->missing()),d);
        menu.findAll().stream().filter(m->Objects.equals(m.categoryId,id)).forEach(m-> {
            m.category=c.name;menu.save(m);
        });
        return c;
    }
    private Category saveCategory(Category c,Requests.CategoryInput d) {
        String name=d.name().strip();
        categories.findByNameIgnoreCase(name).filter(existing->!Objects.equals(existing.id,c.id)).ifPresent(existing-> {
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Category already exists");
        });
        c.name=name;
        return categories.save(c);
    }
    @DeleteMapping("/categories/{id}") public Map<String,String> deleteCategory(@PathVariable Long id) {
        Category c=categories.findById(id).orElseThrow(()->missing());
        if(menu.findAll().stream().anyMatch(m->Objects.equals(m.categoryId,id)))throw new ResponseStatusException(HttpStatus.CONFLICT,"Category is used by menu items");
        categories.delete(c);
        return Map.of("message","Category deleted");
    }
    @GetMapping("/users") public List<AppUser> users() {
        return users.findAll();
    }
    @PatchMapping("/users/{id}/enabled") public AppUser enableUser(@PathVariable Long id,@RequestBody Requests.UserEnabled d,org.springframework.security.core.Authentication auth) {
        AppUser u=users.findById(id).orElseThrow(()->missing());
        if(u.role.equals("ADMIN"))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Administrator accounts cannot be disabled here");
        u.enabled=d.enabled();
        return users.save(u);
    }
    private ResponseStatusException missing() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND,"Record not found");
    }
}
