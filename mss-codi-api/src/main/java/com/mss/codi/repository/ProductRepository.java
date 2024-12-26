package com.mss.codi.repository;

import com.mss.codi.entity.Product;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    /**
     * 테스트를 위해서 모든 데이터를 삭제할 수 있도록 수정
     */
    @Query(nativeQuery = true, value = """
        delete from product
    """)
    @Modifying
    @Profile("test")
    void deleteProducts();
}
