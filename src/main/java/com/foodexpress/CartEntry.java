package com.foodexpress;
import jakarta.persistence.Embeddable;
@Embeddable public class CartEntry {
    public Long menuItemId;
    public int quantity;
    public CartEntry() {
    } public CartEntry(Long id,int q) {
        menuItemId=id;
        quantity=q;
    }
}
