package com.foodexpress;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;
@RestController @RequestMapping("/api/cart")
public class CartController {
    private final CartRepository carts;
    private final MenuRepository menu;
    private final RestaurantRepository restaurants;
    private final OrderService service;
    public CartController(CartRepository c,MenuRepository m,RestaurantRepository r,OrderService s) {
        carts=c;
        menu=m;
        restaurants=r;
        service=s;
    }
    public record CartInput(@NotNull @Size(max=30) List<Requests.@Valid CartLine> items) {
    }
    @GetMapping public List<Map<String,Object>> get(Authentication a) {
        return view(carts.findByCustomerId(service.user(a.getName()).id).map(c->c.items).orElseGet(ArrayList::new));
    }
    @PutMapping @Transactional public List<Map<String,Object>> put(Authentication a,@Valid @RequestBody CartInput input) {
        Long userId=service.user(a.getName()).id;
        AppCart c=carts.findByCustomerId(userId).orElseGet(()-> {
            AppCart n=new AppCart();n.customerId=userId;return n;
        });
        Set<Long> seen=new HashSet<>();
        Long restaurant=null;
        List<CartEntry> updated=new ArrayList<>();
        for(var line:input.items()) {
            if(!seen.add(line.menuItemId()))throw bad("Duplicate cart item");
            MenuItem m=menu.findById(line.menuItemId()).orElseThrow(()->bad("Item not found"));
            if(!m.available||!restaurants.findById(m.restaurantId).map(r->r.active).orElse(false))throw bad("Item unavailable");
            if(restaurant!=null&&!restaurant.equals(m.restaurantId))throw bad("One restaurant per cart");
            restaurant=m.restaurantId;
            updated.add(new CartEntry(m.id,line.quantity()));
        }
        c.items.clear();
        c.items.addAll(updated);
        carts.save(c);
        return view(c.items);
    }
    private List<Map<String,Object>> view(List<CartEntry> entries) {
        List<Map<String,Object>> result=new ArrayList<>();
        for(CartEntry e:entries) {
            MenuItem m=menu.findById(e.menuItemId).orElse(null);
            if(m==null)continue;
            Restaurant r=restaurants.findById(m.restaurantId).orElse(null);
            if(r==null)continue;
            result.add(Map.of("id",m.id,"name",m.name,"emoji",m.emoji,"price",m.price,"quantity",e.quantity,"restaurantId",r.id,"restaurantName",r.name));
        }
        return result;
    }
    private ResponseStatusException bad(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST,message);
    }
}
