package com.datn.teeshirt.Service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.datn.teeshirt.Entity.Customer;
import com.datn.teeshirt.Entity.Product;
import com.datn.teeshirt.Entity.Wishlist;
import com.datn.teeshirt.Entity.WishlistItem;
import com.datn.teeshirt.Repository.ProductRepository;
import com.datn.teeshirt.Repository.WishlistItemRepository;
import com.datn.teeshirt.Repository.WishlistRepository;

@Service
public class WishlistService {
    @Autowired
    private WishlistRepository wishlistRepository;
    @Autowired
    private WishlistItemRepository wishlistItemRepository;
    @Autowired
    private ProductRepository productRepository;

    public Wishlist getOrCreateWishlist(Customer customer) {
        return wishlistRepository.findByCustomerCustomerId(customer.getCustomerId())
                .orElseGet(() -> wishlistRepository.save(Wishlist.builder().customer(customer).build()));
    }

    public List<WishlistItem> getWishlistItems(Customer customer) {
        Wishlist wishlist = getOrCreateWishlist(customer);
        return wishlistItemRepository.findByWishlistWishlistId(wishlist.getWishlistId())
            .stream().filter(item -> item.getProduct() != null).collect(Collectors.toList());
    }

    @Transactional
    public void addProductToWishlist(Customer customer, Long productId) {
        Wishlist wishlist = getOrCreateWishlist(customer);
        if (!wishlistItemRepository.existsByWishlistWishlistIdAndProductProductId(wishlist.getWishlistId(), productId)) {
            Product product = productRepository.findById(productId).orElseThrow();
            WishlistItem item = WishlistItem.builder().wishlist(wishlist).product(product).build();
            wishlistItemRepository.save(item);
        }
    }

    @Transactional
    public void removeProductFromWishlist(Customer customer, Long productId) {
        Wishlist wishlist = getOrCreateWishlist(customer);
        wishlistItemRepository.deleteByWishlistWishlistIdAndProductProductId(wishlist.getWishlistId(), productId);
    }
} 
