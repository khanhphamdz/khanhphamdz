package com.datn.teeshirt.Service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.datn.teeshirt.DTO.ProductDTO;
import com.datn.teeshirt.Entity.Customer;
import com.datn.teeshirt.Entity.Product;
import com.datn.teeshirt.Entity.Wishlist;
import com.datn.teeshirt.Entity.WishlistItem;
import com.datn.teeshirt.Repository.CustomerRepository;
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
    private CustomerRepository customerRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    public Wishlist getOrCreateWishlist(Customer customer) {
        return wishlistRepository.findByCustomerCustomerId(customer.getCustomerId())
                .orElseGet(() -> wishlistRepository.save(Wishlist.builder().customer(customer).build()));
    }
    
    private Customer getCustomerFromAuth(Authentication authentication) {
        String email = authentication.getName();
        return customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
    }
    
    @Transactional
    public void addToWishlist(Long productId, Authentication authentication) {
        Customer customer = getCustomerFromAuth(authentication);
        
        // T�m ho?c t?o wishlist cho customer
        Wishlist wishlist = wishlistRepository.findByCustomerId(customer.getCustomerId())
                .orElseGet(() -> {
                    Wishlist newWishlist = Wishlist.builder()
                            .customer(customer)
                            .build();
                    return wishlistRepository.save(newWishlist);
                });
        
        // Ki?m tra s?n ph?m d� c� trong wishlist chua
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        if (wishlistItemRepository.findByWishlistAndProduct(wishlist, product).isPresent()) {
            throw new RuntimeException("S?n ph?m d� c� trong danh s�ch y�u th�ch");
        }
        
        // Th�m s?n ph?m v�o wishlist
        WishlistItem wishlistItem = WishlistItem.builder()
                .wishlist(wishlist)
                .product(product)
                .build();
        
        wishlistItemRepository.save(wishlistItem);
    }
    
    @Transactional
    public void removeFromWishlist(Long productId, Authentication authentication) {
        Customer customer = getCustomerFromAuth(authentication);
        
        Wishlist wishlist = wishlistRepository.findByCustomerId(customer.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Wishlist not found"));
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        WishlistItem wishlistItem = wishlistItemRepository.findByWishlistAndProduct(wishlist, product)
                .orElseThrow(() -> new RuntimeException("Product not found in wishlist"));
        
        wishlistItemRepository.delete(wishlistItem);
    }
    
    public List<ProductDTO> getWishlistProducts(Authentication authentication) {
        Customer customer = getCustomerFromAuth(authentication);
        
        List<WishlistItem> wishlistItems = wishlistRepository.findWishlistItemsByCustomerId(customer.getCustomerId());
        
        return wishlistItems.stream()
                .map(item -> {
                    Product product = productRepository.findById(item.getProduct().getProductId())
                            .orElseThrow(() -> new RuntimeException("Product not found"));
                    return toProductDTO(product);
                })
                .collect(Collectors.toList());
    }
    
    public boolean isProductInWishlist(Long productId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        try {
            Customer customer = getCustomerFromAuth(authentication);
            return wishlistRepository.findByCustomerIdAndProductId(customer.getCustomerId(), productId).isPresent();
        } catch (Exception e) {
            return false;
        }
    }
    
    public int getWishlistCount(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return 0;
        }
        
        try {
            Customer customer = getCustomerFromAuth(authentication);
            return wishlistRepository.countWishlistItemsByCustomerId(customer.getCustomerId());
        } catch (Exception e) {
            return 0;
        }
    }
    
    // Convert Product entity to ProductDTO
    private ProductDTO toProductDTO(Product product) {
        return ProductDTO.builder()
                .productId(product.getProductId())
                .name(product.getName())
                .description(product.getDescription())
                .shortDescription(product.getShortDescription())
                .basePrice(product.getBasePrice())
                .status(product.getStatus())
                .materialId(product.getMaterial() != null ? product.getMaterial().getMaterialId() : null)
                .images(product.getImages() != null ? product.getImages().stream()
                        .map(image -> com.datn.teeshirt.DTO.ProductImageDTO.builder()
                                .imageId(image.getImageId())
                                .imageUrl(image.getImageUrl())
                                .build())
                        .collect(java.util.stream.Collectors.toList()) : null)
                .build();
    }
} 
