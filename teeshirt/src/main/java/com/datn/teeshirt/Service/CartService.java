package com.datn.teeshirt.Service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.datn.teeshirt.DTO.CartItemDTO;
import com.datn.teeshirt.DTO.CartItemRequest;
import com.datn.teeshirt.Entity.Cart;
import com.datn.teeshirt.Entity.CartItem;
import com.datn.teeshirt.Entity.Customer;
import com.datn.teeshirt.Entity.ProductVariant;
import com.datn.teeshirt.Repository.CartItemRepository;
import com.datn.teeshirt.Repository.CartRepository;
import com.datn.teeshirt.Repository.ProductVariantRepository;

@Service
public class CartService {
    @Autowired
    private CustomerService customerService;
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private ProductVariantRepository productVariantRepository;

    // Lấy Customer từ Authentication
    private Customer getCustomerFromAuth(Authentication authentication) {
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        return customerService.findByEmail(email).orElseThrow();
    }

    public List<CartItemDTO> getCartItems(Authentication authentication) {
        Customer customer = getCustomerFromAuth(authentication);
        Cart cart = cartRepository.findByCustomer(customer).orElse(null);
        List<CartItemDTO> result = new ArrayList<>();
        if (cart != null && cart.getCartItems() != null) {
            for (CartItem item : cart.getCartItems()) {
                ProductVariant variant = item.getVariant();
                String productName = variant.getProduct().getName();
                String color = variant.getColor().getName();
                String size = variant.getSize().getName();
                Double price = variant.getDiscountPrice() != null ? variant.getDiscountPrice().doubleValue()
                        : variant.getPrice().doubleValue();
                String imageUrl = null;
                if (variant.getImages() != null && !variant.getImages().isEmpty()) {
                    imageUrl = variant.getImages().get(0).getImageUrl();
                }
                result.add(CartItemDTO.builder()
                        .cartItemId(item.getCartItemId())
                        .variantId(variant.getVariantId())
                        .productName(productName)
                        .color(color)
                        .size(size)
                        .quantity(item.getQuantity())
                        .price(price)
                        .imageUrl(imageUrl)
                        .build());
            }
        }
        return result;
    }

    @Transactional
    public void addToCart(CartItemRequest request, Authentication authentication) {
        Customer customer = getCustomerFromAuth(authentication);
        Cart cart = cartRepository.findByCustomer(customer).orElse(null);
        if (cart == null) {
            cart = Cart.builder().customer(customer).build();
            cart = cartRepository.save(cart);
        }
        ProductVariant variant = productVariantRepository.findById(request.getVariantId()).orElseThrow();
        // Kiểm tra đã có sản phẩm này trong giỏ chưa
        CartItem existed = null;
        for (CartItem item : cart.getCartItems()) {
            if (item.getVariant().getVariantId().equals(request.getVariantId())) {
                existed = item;
                break;
            }
        }
        if (existed != null) {
            existed.setQuantity(existed.getQuantity() + request.getQuantity());
            cartItemRepository.save(existed);
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .variant(variant)
                    .quantity(request.getQuantity())
                    .build();
            cartItemRepository.save(newItem);
        }
    }

    @Transactional
    public void updateCartItem(CartItemRequest request, Authentication authentication) {
        Customer customer = getCustomerFromAuth(authentication);
        Cart cart = cartRepository.findByCustomer(customer).orElse(null);
        if (cart == null)
            return;
        for (CartItem item : cart.getCartItems()) {
            if (item.getVariant().getVariantId().equals(request.getVariantId())) {
                item.setQuantity(request.getQuantity());
                cartItemRepository.save(item);
                break;
            }
        }
    }

    @Transactional
    public void removeCartItem(Long variantId, Authentication authentication) {
        Customer customer = getCustomerFromAuth(authentication);
        Cart cart = cartRepository.findByCustomer(customer).orElse(null);
        if (cart == null) {
            throw new RuntimeException("Không tìm thấy giỏ hàng");
        }
        
        // Xóa CartItem trực tiếp bằng cartId và variantId
        cartItemRepository.deleteByCartIdAndVariantId(cart.getCartId(), variantId);
        
        // Refresh cart để cập nhật danh sách cartItems
        cart = cartRepository.findByCustomer(customer).orElse(null);
    }

    public void mergeCart(List<CartItemRequest> localCart, Authentication authentication) {
        Customer customer = getCustomerFromAuth(authentication);
        Cart cart = cartRepository.findByCustomer(customer).orElse(null);
        if (cart == null) {
            cart = Cart.builder().customer(customer).build();
            cart = cartRepository.save(cart);
        }
        for (CartItemRequest req : localCart) {
            ProductVariant variant = productVariantRepository.findById(req.getVariantId()).orElse(null);
            if (variant == null)
                continue;
            CartItem existed = null;
            for (CartItem item : cart.getCartItems()) {
                if (item.getVariant().getVariantId().equals(req.getVariantId())) {
                    existed = item;
                    break;
                }
            }
            if (existed != null) {
                existed.setQuantity(existed.getQuantity() + req.getQuantity());
                cartItemRepository.save(existed);
            } else {
                CartItem newItem = CartItem.builder()
                        .cart(cart)
                        .variant(variant)
                        .quantity(req.getQuantity())
                        .build();
                cartItemRepository.save(newItem);
            }
        }
    }

    // private CartDTO convertToCartDTO(Cart cart) {
    // return CartDTO cart = new CartDTO();
    // }
}
