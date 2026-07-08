package com.yas.cart.controller;

import com.yas.cart.service.CartItemService;
import com.yas.cart.viewmodel.CartItemDeleteVm;
import com.yas.cart.viewmodel.CartItemGetVm;
import com.yas.cart.viewmodel.CartItemPostVm;
import com.yas.cart.viewmodel.CartItemPutVm;
import io.micrometer.core.annotation.Timed;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CartItemController {
    private final CartItemService cartItemService;

    @Timed(value = "http.server.cart.add", description = "HTTP request to add cart item", extraTags = {"endpoint", "add"})
    @PostMapping("/storefront/cart/items")
    public ResponseEntity<CartItemGetVm> addCartItem(@Valid @RequestBody CartItemPostVm cartItemPostVm) {
        CartItemGetVm cartItemGetVm = cartItemService.addCartItem(cartItemPostVm);
        return ResponseEntity.ok(cartItemGetVm);
    }

    @Timed(value = "http.server.cart.update", description = "HTTP request to update cart item", extraTags = {"endpoint", "update"})
    @PutMapping("/storefront/cart/items/{productId}")
    public ResponseEntity<CartItemGetVm> updateCartItem(@PathVariable Long productId,
                                                        @Valid @RequestBody CartItemPutVm cartItemPutVm) {
        CartItemGetVm cartItemGetVm = cartItemService.updateCartItem(productId, cartItemPutVm);
        return ResponseEntity.ok(cartItemGetVm);
    }

    @Timed(value = "http.server.cart.get", description = "HTTP request to get cart items", extraTags = {"endpoint", "get"})
    @GetMapping("/storefront/cart/items")
    public ResponseEntity<List<CartItemGetVm>> getCartItems() {
        List<CartItemGetVm> cartItemGetVms = cartItemService.getCartItems();
        return ResponseEntity.ok(cartItemGetVms);
    }

    @Timed(value = "http.server.cart.remove", description = "HTTP request to remove cart items", extraTags = {"endpoint", "remove"})
    @PostMapping("/storefront/cart/items/remove")
    public ResponseEntity<List<CartItemGetVm>> removeCartItems(
        @RequestBody List<@Valid CartItemDeleteVm> cartItemDeleteVms) {
        List<CartItemGetVm> cartItemGetVms = cartItemService.deleteOrAdjustCartItem(cartItemDeleteVms);
        return ResponseEntity.ok(cartItemGetVms);
    }

    @Timed(value = "http.server.cart.delete", description = "HTTP request to delete cart item", extraTags = {"endpoint", "delete"})
    @DeleteMapping("/storefront/cart/items/{productId}")
    public ResponseEntity<Void> deleteCartItem(@PathVariable Long productId) {
        cartItemService.deleteCartItem(productId);
        return ResponseEntity.noContent().build();
    }
}