package com.mss.codi;

import com.mss.codi.controller.dto.MinMaxProductByCategoryResponse;
import com.mss.codi.controller.dto.ProductDto;
import com.mss.codi.entity.Category;
import com.mss.codi.service.CategoryService;
import com.mss.codi.service.CodiService;
import com.mss.codi.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@SpringBootTest(classes = Application.class)
@TestExecutionListeners({DataResetTestExecutionListener.class, DependencyInjectionTestExecutionListener.class})
@ActiveProfiles(value = "test")
class MinMaxProductByCategoryTest {
	@Autowired
	private CategoryService categoryService;
	@Autowired
	private CodiService codiService;
	@Autowired
	private ProductService productService;
	@Autowired
	private ProductModifyService productModifyService;

	@Test
	@DisplayName("카테고리 최저최고가 브랜드 조회")
	public void testMinMaxBrandByCategory() {
		var categories = categoryService.getAllCategories();
		testMinMaxBrandByCategory(categories);
	}

	private void testMinMaxBrandByCategory(List<Category> categories) {
		var minMaxProductByCategory = getMinMaxProductByCategory();
		for (var category : categories) {
			testMinMaxBrand(category.getName(), minMaxProductByCategory.get(category.getName()));
		}
	}

	@Test
	@DisplayName("상품 변경 후 최저최고가 브랜드 조회")
	public void testAfterProductModifiedMinMaxBrandByCategory() {
		var categories = categoryService.getAllCategories();

		var newProduct = new ProductDto.NewProduct("D", "상의", 100000L);
		var newMaxProduct = new ProductDto.NewProduct("F", "상의", 10L);
		var productModifyContext = productModifyService.addProduct(List.of(newProduct, newMaxProduct));

		testMinMaxBrandByCategory(categories);

		// 삭제
		productModifyService.removeProduct(productModifyContext);
		testMinMaxBrandByCategory(categories);

		Optional<ProductDto.Product> optProduct = productModifyService.findProduct("상의", "C");
		Assertions.assertTrue(optProduct.isPresent());
		var product = optProduct.get();

		productModifyService.modifyProductPrice(product, 15000);
		testMinMaxBrandByCategory(categories);

		productModifyService.modifyProductCategory(product, "양말");
		testMinMaxBrandByCategory(categories);

		productModifyService.modifyProductBrand(product, "D");
		testMinMaxBrandByCategory(categories);
	}

	private Map<String, MinMaxProductByCategoryResponse> getMinMaxProductByCategory() {
		var products = productService.getAllProducts();
		var productMap = products.stream().collect(Collectors.groupingBy(ProductDto.Product::getCategory));
		Map<String, MinMaxProductByCategoryResponse> categoryMap = new HashMap<>();
		for (Map.Entry<String, List<ProductDto.Product>> entry : productMap.entrySet()) {
			String category = entry.getKey();
			List<ProductDto.Product> productsOfCategory = entry.getValue();
			var minPrice = Long.MAX_VALUE;
			var maxPrice = 0L;
			var minMaxOfCategory = MinMaxProductByCategoryResponse.builder()
					.category(category)
					.minBrands(new ArrayList<>())
					.maxBrands(new ArrayList<>())
					.build();
			categoryMap.put(category, minMaxOfCategory);
			for (ProductDto.Product product : productsOfCategory) {
				if (minPrice > product.getPrice()) {
					minMaxOfCategory.getMinBrands().clear();
					minMaxOfCategory.getMinBrands().add(MinMaxProductByCategoryResponse.Brand.builder().brand(product.getBrand()).price(product.getPrice()).build());
					minPrice = product.getPrice();
				} else if (minPrice == product.getPrice()) {
					minMaxOfCategory.getMinBrands().add(MinMaxProductByCategoryResponse.Brand.builder().brand(product.getBrand()).price(product.getPrice()).build());
				}

				if (maxPrice < product.getPrice()) {
					minMaxOfCategory.getMaxBrands().clear();
					minMaxOfCategory.getMaxBrands().add(MinMaxProductByCategoryResponse.Brand.builder().brand(product.getBrand()).price(product.getPrice()).build());
					maxPrice = product.getPrice();
				} else if (maxPrice == product.getPrice()) {
					minMaxOfCategory.getMaxBrands().add(MinMaxProductByCategoryResponse.Brand.builder().brand(product.getBrand()).price(product.getPrice()).build());
				}
			}
		}

		return categoryMap;
	}

	private void testMinMaxBrand(String category, MinMaxProductByCategoryResponse expected) {
		var response = codiService.getMinMaxProductByCategory(category);
		log.info("카테고리 최저/최고가 브랜드 조회 {}\n{}", category, response);
		Assertions.assertNotNull(response);
		Assertions.assertEquals(category, response.getCategory());

		var min = response.getMinBrands().stream().collect(Collectors.toMap(MinMaxProductByCategoryResponse.Brand::getBrand, MinMaxProductByCategoryResponse.Brand::getPrice));
		var expectedMin = expected.getMinBrands().stream().collect(Collectors.toMap(MinMaxProductByCategoryResponse.Brand::getBrand, MinMaxProductByCategoryResponse.Brand::getPrice));;
		testMinMax(min, expectedMin);

		var max = response.getMinBrands().stream().collect(Collectors.toMap(MinMaxProductByCategoryResponse.Brand::getBrand, MinMaxProductByCategoryResponse.Brand::getPrice));;
		var expectedMax = expected.getMinBrands().stream().collect(Collectors.toMap(MinMaxProductByCategoryResponse.Brand::getBrand, MinMaxProductByCategoryResponse.Brand::getPrice));;
		testMinMax(max, expectedMax);
	}

	private void testMinMax(Map<String, Long> value, Map<String, Long> expected) {
		Assertions.assertEquals(expected.size(), value.size());
		for (Map.Entry<String, Long> entry : expected.entrySet()) {
			Long price = value.get(entry.getKey());
			Assertions.assertNotNull(price);
			Assertions.assertEquals(entry.getValue(), price);
		}
	}
}
