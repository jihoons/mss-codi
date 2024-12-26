package com.mss.codi.repository;

import com.mss.codi.entity.Product;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("""
from Product p
join fetch p.brand b
join fetch p.category c
""")
    List<Product> getAllProducts();

    @Query(nativeQuery = true, value = """
delete from product
""")
    @Modifying
    @Profile("test")
    void deleteProducts();
}
