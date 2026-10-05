package com.foodexpress;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.math.BigDecimal;
import java.util.*;
@Service
public class OrderService {
    private final UserRepository users;
    private final RestaurantRepository restaurants;
    private final MenuRepository menu;
    private final OrderRepository orders;
    private final CartRepository carts;
    public OrderService(UserRepository u,RestaurantRepository r,MenuRepository m,OrderRepository o,CartRepository c) {
        carts=c;
        users=u;
        restaurants=r;
        menu=m;
        orders=o;
    }
    public AppUser user(String email) {
        return users.findByEmail(email).filter(u->u.enabled).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Please log in"));
    }
    @Transactional public FoodOrder checkout(String email,Requests.Checkout data) {
        FoodOrder o=new FoodOrder();
        AppUser u=user(email);
        o.customerId=u.id;
        o.customerName=u.name;
        o.address=data.address().strip();
        o.phone=data.phone();
        o.notes=data.notes();
        o.subtotal=BigDecimal.ZERO;
        Set<Long> seen=new HashSet<>();
        for(Requests.CartLine line:data.items()) {
            if(!seen.add(line.menuItemId())) throw bad("Duplicate cart item");
            MenuItem m=menu.findById(line.menuItemId()).orElseThrow(()->bad("Menu item no longer exists"));
            if(!m.available) throw bad(m.name+" is unavailable");
            if(o.restaurantId==null) o.restaurantId=m.restaurantId;
            if(!o.restaurantId.equals(m.restaurantId)) throw bad("Choose food from one restaurant per order");
            OrderLine item=new OrderLine(m,line.quantity());
            o.items.add(item);
            o.subtotal=o.subtotal.add(item.lineTotal);
        }
        Restaurant r=restaurants.findById(o.restaurantId).orElseThrow(()->bad("Restaurant not found"));
        if(!r.active) throw bad("Restaurant is closed");
        o.restaurantName=r.name;
        o.deliveryFee=o.subtotal.compareTo(new BigDecimal("499"))>=0?BigDecimal.ZERO:new BigDecimal("35.00");
        o.total=o.subtotal.add(o.deliveryFee);
        FoodOrder saved=orders.save(o);
        carts.findByCustomerId(u.id).ifPresent(c-> {
            c.items.clear();carts.save(c);
        });
        return saved;
    }
    @Transactional public FoodOrder cancel(String email,Long id) {
        FoodOrder o=orders.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Order not found"));
        if(!o.customerId.equals(user(email).id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Order not found");
        if(!o.status.equals("PLACED")) throw bad("Only newly placed orders can be cancelled");
        o.status="CANCELLED";
        return orders.save(o);
    }
    @Transactional public FoodOrder changeStatus(Long id,String next) {
        FoodOrder o=orders.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Order not found"));
        Map<String,List<String>> flow=Map.of("PLACED",List.of("CONFIRMED","CANCELLED"),"CONFIRMED",List.of("PREPARING","CANCELLED"),"PREPARING",List.of("OUT_FOR_DELIVERY"),"OUT_FOR_DELIVERY",List.of("DELIVERED"));
        if(!flow.getOrDefault(o.status,List.of()).contains(next)) throw bad("Invalid order transition");
        o.status=next;
        if(next.equals("DELIVERED"))o.paymentStatus="COLLECTED";
        return orders.save(o);
    }
    private ResponseStatusException bad(String m) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST,m);
    }
}
