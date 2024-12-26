package com.mss.codi.service;

import com.mss.codi.controller.dto.ProductDto;
import com.mss.codi.dao.CodiDao;
import com.mss.codi.dao.ProductDao;
import com.mss.codi.entity.Brand;
import com.mss.codi.entity.Category;
import com.mss.codi.entity.Product;
import com.mss.codi.repository.ProductRepository;
import com.mss.codi.type.PriceType;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProductService {
    private final ProductDao productDao;
    private final CategoryService categoryService;
    private final BrandService brandService;
    private final ProductRepository productRepository;
    private final Environment environment;
    private final CodiDao codiDao;

    // 한브랜드당 최대 노출할 상품 수
    @Value("${codi.brand.product.count:5}")
    private long maxProductCountOfBrand;


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void removeAll() {
        if (StringUtils.equalsAny("test", environment.getActiveProfiles())) {
            codiDao.truncateSummaries();
            productDao.clear();
        }
    }

    @Transactional(readOnly = true)
    public List<ProductDto.Product> getAllProducts() {
        return productDao.getAllProducts().stream().map(this::convert2ProductDto).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductDto.Product> getProductsByCategoryBrand(String category, String brand) {
        return productDao.getProductsByCategoryAndBrand(category, brand).stream()
                .map(this::convert2ProductDto).toList();
    }

    private ProductDto.Product convert2ProductDto(Product product) {
        return ProductDto.Product.builder()
                .id(product.getId())
                .brand(product.getBrand().getName())
                .category(product.getCategory().getName())
                .price(product.getPrice())
                .build();
    }

    @Transactional
    public ProductDto.Product addProduct(ProductDto.NewProduct newProduct) {
        Product product = convert2Product(newProduct);
        productRepository.save(product);
        return convert2ProductDto(product);
    }

    @Transactional
    public ProductDto.Product saveProduct(ProductDto.Product product) {
        Product savedProduct = productDao.getProductById(product.getId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."));
        Product modifiedProduct = convert2Product(product, savedProduct);
        modifiedProduct.setId(product.getId());
        productRepository.save(modifiedProduct);
        return convert2ProductDto(modifiedProduct);
    }

    @Transactional
    public ProductDto.Product removeProduct(long productId) {
        Product savedProduct = productDao.getProductById(productId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."));
        productRepository.delete(savedProduct);
        return convert2ProductDto(savedProduct);
    }

    private Product convert2Product(ProductDto.NewProduct newProduct) {
        Category category = categoryService.getCategoryByName(newProduct.getCategory());
        Brand brand = brandService.getBrandByName(newProduct.getBrand());;
        return new Product(category, brand, newProduct.getPrice());
    }

    private Product convert2Product(ProductDto.Product requestProduct, @NotNull Product savedProduct) {
        Category category;
        Brand brand;

        if (StringUtils.equals(savedProduct.getCategory().getName(), requestProduct.getCategory())) {
            // 같은 카테고리이면 저장된 카테고리 사용
            category = savedProduct.getCategory();
        } else {
            // 다른 카테고리이면 카테고리 조회
            category = categoryService.getCategoryByName(requestProduct.getCategory());
        }

        if (StringUtils.equals(savedProduct.getBrand().getName(), requestProduct.getBrand())) {
            // 같은 브랜드이면 저장된 브랜드 사용
            brand = savedProduct.getBrand();
        } else {
            // 다른 브랜드이면 브랜드 정보 조회
            brand = brandService.getBrandByName(requestProduct.getBrand());
        }

        savedProduct.setCategory(category);
        savedProduct.setBrand(brand);
        savedProduct.setPrice(requestProduct.getPrice());

        return savedProduct;
    }

    @Transactional(readOnly = true)
    public Optional<Product> getProduct(long id) {
        return productDao.getProductById(id);
    }

    @Transactional(readOnly = true)
    public List<Product> getTopPriceProductByCategoryAndBrand(Category category, Brand brand, PriceType priceType) {
        return getTopPriceProductByCategoryAndBrand(category, brand, priceType, 0L);
    }

    @Transactional(readOnly = true)
    public List<Product> getTopPriceProductByCategoryAndBrand(Category category, Brand brand, PriceType priceType, long price) {
        List<Product> products = productDao.getProductByCategoryAndBrand(category, brand, priceType, price, maxProductCountOfBrand);
        if (CollectionUtils.isEmpty(products)) {
            return Collections.emptyList();
        }

        long targetPrice = products.getFirst().getPrice();
        return products.stream().filter(p -> p.getPrice() == targetPrice).toList();
    }
}
