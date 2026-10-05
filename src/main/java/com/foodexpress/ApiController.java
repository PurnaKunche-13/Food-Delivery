package com.foodexpress;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
@RestController @RequestMapping("/api")
public class ApiController {
    private final RestaurantRepository restaurants;
    private final MenuRepository menu;
    private final OrderRepository orders;
    private final OrderService service;
    public ApiController(RestaurantRepository r,MenuRepository m,OrderRepository o,OrderService s) {
        restaurants=r;
        menu=m;
        orders=o;
        service=s;
    }
    @GetMapping("/restaurants") public List<Restaurant> restaurants() {
        return restaurants.findAllByOrderByIdAsc().stream().filter(r->r.active).toList();
    }
    @GetMapping("/restaurants/{id}/menu") public Map<String,Object> restaurant(@PathVariable Long id) {
        Restaurant r=restaurants.findById(id).filter(x->x.active).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Restaurant not found"));
        return Map.of("restaurant",r,"items",menu.findByRestaurantIdOrderByIdAsc(id).stream().filter(m->m.available).toList());
    }
    @GetMapping("/orders") public List<FoodOrder> orders(Authentication a) {
        return orders.findByCustomerIdOrderByCreatedAtDesc(service.user(a.getName()).id);
    }
    @PostMapping("/orders") @ResponseStatus(HttpStatus.CREATED) public FoodOrder checkout(Authentication a,@Valid @RequestBody Requests.Checkout data) {
        return service.checkout(a.getName(),data);
    }
    @PostMapping("/orders/{id}/cancel") public FoodOrder cancel(Authentication a,@PathVariable Long id) {
        return service.cancel(a.getName(),id);
    }
}
