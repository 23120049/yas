package com.yas.product.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.product.model.Brand;
import com.yas.product.model.Category;
import com.yas.product.model.Product;
import com.yas.product.model.ProductCategory;
import com.yas.product.model.ProductImage;
import com.yas.product.model.ProductOption;
import com.yas.product.model.enumeration.DimensionUnit;
import com.yas.product.repository.BrandRepository;
import com.yas.product.repository.CategoryRepository;
import com.yas.product.repository.ProductCategoryRepository;
import com.yas.product.repository.ProductImageRepository;
import com.yas.product.repository.ProductOptionCombinationRepository;
import com.yas.product.repository.ProductOptionRepository;
import com.yas.product.repository.ProductOptionValueRepository;
import com.yas.product.repository.ProductRelatedRepository;
import com.yas.product.repository.ProductRepository;
import com.yas.product.viewmodel.NoFileMediaVm;
import com.yas.product.viewmodel.product.ProductGetDetailVm;
import com.yas.product.viewmodel.product.ProductPostVm;
import com.yas.product.viewmodel.product.ProductPutVm;
import com.yas.product.viewmodel.product.ProductQuantityPostVm;
import com.yas.product.viewmodel.product.ProductQuantityPutVm;
import com.yas.product.viewmodel.product.ProductOptionValueDisplay;
import com.yas.product.viewmodel.product.ProductVariationPostVm;
import com.yas.product.viewmodel.product.ProductVariationPutVm;
import com.yas.product.viewmodel.product.ProductDetailVm;
import com.yas.product.viewmodel.productoption.ProductOptionValuePostVm;
import com.yas.product.viewmodel.productoption.ProductOptionValuePutVm;
import java.util.Map;
import java.util.List;
import java.util.Optional;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    private ProductPutVm minimalProductPutVmWithCategoryIds(List<Long> categoryIds) {
        return new ProductPutVm(
            "Product",
            "product",
            1.0,
            true,
            true,
            false,
            true,
            true,
            null,
            categoryIds,
            null,
            null,
            null,
            "SKU",
            null,
            1.0,
            DimensionUnit.CM,
            1.0,
            1.0,
            1.0,
            null,
            null,
            null,
            null,
            List.of(),
            List.of(),
            List.of(new ProductOptionValuePutVm(1L, "text", 0, List.of("Red"))),
            List.of(),
            List.of(),
            1L
        );
    }

    @Mock
    private ProductRepository productRepository;

    @Mock
    private MediaService mediaService;

    @Mock
    private BrandRepository brandRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductCategoryRepository productCategoryRepository;

    @Mock
    private ProductImageRepository productImageRepository;

    @Mock
    private ProductOptionRepository productOptionRepository;

    @Mock
    private ProductOptionValueRepository productOptionValueRepository;

    @Mock
    private ProductOptionCombinationRepository productOptionCombinationRepository;

    @Mock
    private ProductRelatedRepository productRelatedRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void getProductById_whenProductExists_thenBuildProductDetailVmWithFullInfo() {
        // Given
        long productId = 1L;
        long brandId = 10L;
        long categoryId = 100L;
        long thumbnailMediaId = 1000L;
        long imageId1 = 2000L;
        long imageId2 = 3000L;

        Brand brand = new Brand();
        brand.setId(brandId);

        Category category = new Category();
        category.setId(categoryId);
        category.setName("Category A");

        Product product = Product.builder()
            .id(productId)
            .name("Product A")
            .shortDescription("Short")
            .description("Desc")
            .specification("Spec")
            .sku("SKU-1")
            .gtin("GTIN-1")
            .slug("product-a")
            .isAllowedToOrder(true)
            .isPublished(true)
            .isFeatured(true)
            .isVisibleIndividually(true)
            .stockTrackingEnabled(true)
            .weight(1.2)
            .length(10.0)
            .width(5.0)
            .height(2.0)
            .price(99.0)
            .brand(brand)
            .metaTitle("Meta Title")
            .metaKeyword("Meta Keyword")
            .metaDescription("Meta Description")
            .thumbnailMediaId(thumbnailMediaId)
            .taxClassId(5L)
            .build();

        ProductImage productImage1 = ProductImage.builder().imageId(imageId1).product(product).build();
        ProductImage productImage2 = ProductImage.builder().imageId(imageId2).product(product).build();
        product.setProductImages(List.of(productImage1, productImage2));

        ProductCategory productCategory = ProductCategory.builder().product(product).category(category).build();
        product.setProductCategories(List.of(productCategory));

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(mediaService.getMedia(thumbnailMediaId))
            .thenReturn(new NoFileMediaVm(thumbnailMediaId, "", "", "", "http://thumb"));
        when(mediaService.getMedia(imageId1))
            .thenReturn(new NoFileMediaVm(imageId1, "", "", "", "http://img-1"));
        when(mediaService.getMedia(imageId2))
            .thenReturn(new NoFileMediaVm(imageId2, "", "", "", "http://img-2"));

        // When
        ProductDetailVm result = productService.getProductById(productId);

        // Then
        assertEquals(productId, result.id());
        assertEquals("Product A", result.name());
        assertEquals("Short", result.shortDescription());
        assertEquals("Desc", result.description());
        assertEquals("Spec", result.specification());
        assertEquals("SKU-1", result.sku());
        assertEquals("GTIN-1", result.gtin());
        assertEquals("product-a", result.slug());
        assertEquals(true, result.isAllowedToOrder());
        assertEquals(true, result.isPublished());
        assertEquals(true, result.isFeatured());
        assertEquals(true, result.isVisible());
        assertEquals(true, result.stockTrackingEnabled());
        assertEquals(1.2, result.weight());
        assertEquals(10.0, result.length());
        assertEquals(5.0, result.width());
        assertEquals(2.0, result.height());
        assertEquals(99.0, result.price());
        assertEquals(brandId, result.brandId());
        assertEquals("Meta Title", result.metaTitle());
        assertEquals("Meta Keyword", result.metaKeyword());
        assertEquals("Meta Description", result.metaDescription());
        assertEquals(5L, result.taxClassId());
        assertNull(result.parentId());

        assertNotNull(result.thumbnailMedia());
        assertEquals(thumbnailMediaId, result.thumbnailMedia().id());
        assertEquals("http://thumb", result.thumbnailMedia().url());

        assertNotNull(result.productImageMedias());
        assertEquals(2, result.productImageMedias().size());
        assertEquals(imageId1, result.productImageMedias().getFirst().id());
        assertEquals("http://img-1", result.productImageMedias().getFirst().url());
        assertEquals(imageId2, result.productImageMedias().get(1).id());
        assertEquals("http://img-2", result.productImageMedias().get(1).url());

        assertNotNull(result.categories());
        assertEquals(1, result.categories().size());
        assertEquals(categoryId, result.categories().getFirst().getId());
        assertEquals("Category A", result.categories().getFirst().getName());
    }

    @Test
    void getProductById_whenThumbnailIsNull_thenThumbnailMediaIsNull() {
        // Given
        long productId = 2L;

        Product product = Product.builder()
            .id(productId)
            .name("Product B")
            .thumbnailMediaId(null)
            .build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // When
        ProductDetailVm result = productService.getProductById(productId);

        // Then
        assertEquals(productId, result.id());
        assertNull(result.thumbnailMedia());
    }

    @Test
    void getProductById_whenProductNotFound_thenThrowNotFoundException() {
        // Given
        long productId = 999L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When + Then
        assertThrows(NotFoundException.class, () -> productService.getProductById(productId));
    }

    @Test
    void createProduct_whenNoVariations_thenCreateMainProductSuccessfullyAndReturnProductGetDetailVm() {
        // Given
        long savedProductId = 11L;

        ProductPostVm productPostVm = new ProductPostVm(
            "Product A",
            "product-a",
            null,
            List.of(),
            "Short",
            "Desc",
            "Spec",
            "SKU-1",
            null,
            1.2,
            DimensionUnit.CM,
            10.0,
            5.0,
            2.0,
            99.0,
            true,
            true,
            false,
            true,
            true,
            "Meta Title",
            "Meta Keyword",
            "Meta Description",
            null,
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            5L
        );

        Product savedMainProduct = Product.builder()
            .id(savedProductId)
            .name("Product A")
            .slug("product-a")
            .build();

        when(productRepository.findBySlugAndIsPublishedTrue("product-a")).thenReturn(Optional.empty());
        when(productRepository.findBySkuAndIsPublishedTrue("SKU-1")).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenReturn(savedMainProduct);

        // When
        ProductGetDetailVm result = productService.createProduct(productPostVm);

        // Then
        assertEquals(savedProductId, result.id());
        assertEquals("Product A", result.name());
        assertEquals("product-a", result.slug());
    }

    @Test
    void createProduct_whenBrandNotFound_thenThrowNotFoundException() {
        // Given
        long brandId = 999L;

        ProductPostVm productPostVm = new ProductPostVm(
            "Product A",
            "product-a",
            brandId,
            List.of(),
            "Short",
            "Desc",
            "Spec",
            "SKU-1",
            null,
            1.2,
            DimensionUnit.CM,
            10.0,
            5.0,
            2.0,
            99.0,
            true,
            true,
            false,
            true,
            true,
            "Meta Title",
            "Meta Keyword",
            "Meta Description",
            null,
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            5L
        );

        when(productRepository.findBySlugAndIsPublishedTrue("product-a")).thenReturn(Optional.empty());
        when(productRepository.findBySkuAndIsPublishedTrue("SKU-1")).thenReturn(Optional.empty());
        when(brandRepository.findById(brandId)).thenReturn(Optional.empty());

        // When + Then
        assertThrows(NotFoundException.class, () -> productService.createProduct(productPostVm));
    }

    @Test
    void createProduct_whenHasVariationsAndOptionValues_thenSaveMainAndVariationsSetHasOptionsTrue() {
        // Given
        long savedProductId = 12L;
        long savedVariationId = 120L;
        long optionId = 9L;

        ProductVariationPostVm variationVm = new ProductVariationPostVm(
            "Variant A",
            "product-a-var",
            "SKU-VAR-1",
            null,
            109.0,
            null,
            List.of(),
            Map.of(optionId, "Red")
        );

        ProductPostVm productPostVm = new ProductPostVm(
            "Product A",
            "product-a",
            null,
            List.of(),
            "Short",
            "Desc",
            "Spec",
            "SKU-1",
            null,
            1.2,
            DimensionUnit.CM,
            10.0,
            5.0,
            2.0,
            99.0,
            true,
            true,
            false,
            true,
            true,
            "Meta Title",
            "Meta Keyword",
            "Meta Description",
            null,
            List.of(),
            List.of(variationVm),
            List.of(new ProductOptionValuePostVm(optionId, "text", 0, List.of("Red"))),
            List.of(ProductOptionValueDisplay.builder()
                .productOptionId(optionId)
                .displayType("text")
                .displayOrder(0)
                .value("Red")
                .build()),
            List.of(),
            5L
        );

        Product savedMainProduct = Product.builder()
            .id(savedProductId)
            .name("Product A")
            .slug("product-a")
            .hasOptions(false)
            .build();

        Product savedMainProductWithOptions = Product.builder()
            .id(savedProductId)
            .name("Product A")
            .slug("product-a")
            .hasOptions(true)
            .build();

        Product savedVariation = Product.builder()
            .id(savedVariationId)
            .name("Variant A")
            .slug("product-a-var")
            .sku("SKU-VAR-1")
            .price(109.0)
            .parent(savedMainProductWithOptions)
            .build();

        ProductOption option = new ProductOption();
        option.setId(optionId);
        option.setName("Color");

        var savedOptionValue = com.yas.product.model.ProductOptionValue.builder()
            .id(1L)
            .product(savedMainProductWithOptions)
            .productOption(option)
            .displayType("text")
            .displayOrder(0)
            .value("Red")
            .build();

        when(productRepository.findBySlugAndIsPublishedTrue("product-a")).thenReturn(Optional.empty());
        when(productRepository.findBySkuAndIsPublishedTrue("SKU-1")).thenReturn(Optional.empty());
        when(productRepository.findBySlugAndIsPublishedTrue("product-a-var")).thenReturn(Optional.empty());
        when(productRepository.findBySkuAndIsPublishedTrue("SKU-VAR-1")).thenReturn(Optional.empty());
        when(productRepository.findAllById(any())).thenReturn(List.of());

        when(productRepository.save(any(Product.class))).thenReturn(savedMainProduct, savedMainProductWithOptions);
        when(productRepository.saveAll(anyList())).thenReturn(List.of(savedVariation));

        when(productOptionRepository.findAllByIdIn(List.of(optionId))).thenReturn(List.of(option));
        when(productOptionValueRepository.saveAll(anyList())).thenReturn(List.of(savedOptionValue));
        when(productOptionCombinationRepository.saveAll(anyList())).thenReturn(List.of());
        when(productImageRepository.saveAll(anyList())).thenReturn(List.of());
        when(productCategoryRepository.saveAll(anyList())).thenReturn(List.of());

        // When
        ProductGetDetailVm result = productService.createProduct(productPostVm);

        // Then
        assertEquals(savedProductId, result.id());
        assertEquals("Product A", result.name());
        assertEquals("product-a", result.slug());

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository, times(2)).save(productCaptor.capture());
        assertEquals(true, productCaptor.getAllValues().getLast().isHasOptions());

        verify(productRepository).saveAll(anyList());
        verify(productOptionRepository).findAllByIdIn(List.of(optionId));
        verify(productOptionValueRepository).saveAll(anyList());
        verify(productOptionCombinationRepository).saveAll(anyList());
    }

    @Test
    void updateProduct_whenProductExists_thenUpdateBasicFieldsAndSaveToRepository() {
        // Given
        long productId = 77L;
        long optionId = 9L;

        Product existingProduct = Product.builder()
            .id(productId)
            .name("Old Name")
            .slug("old-slug")
            .sku("OLD-SKU")
            .price(10.0)
            .isPublished(true)
            .build();

        ProductVariationPutVm newVariationVm = new ProductVariationPutVm(
            null,
            "Variant A",
            "variant-a",
            "VAR-SKU-1",
            null,
            15.0,
            null,
            List.of(),
            Map.of()
        );

        ProductPutVm productPutVm = new ProductPutVm(
            "New Name",
            "New-Slug",
            99.0,
            true,
            true,
            false,
            true,
            true,
            null,
            List.of(),
            "Short",
            "Desc",
            "Spec",
            "SKU-1",
            null,
            1.2,
            DimensionUnit.CM,
            10.0,
            5.0,
            2.0,
            "Meta Title",
            "Meta Keyword",
            "Meta Description",
            null,
            List.of(),
            List.of(newVariationVm),
            List.of(new ProductOptionValuePutVm(optionId, "text", 0, List.of("Red"))),
            List.of(),
            List.of(),
            5L
        );

        ProductOption option = new ProductOption();
        option.setId(optionId);
        option.setName("Color");

        Product savedVariation = Product.builder()
            .id(101L)
            .name("Variant A")
            .slug("variant-a")
            .sku("VAR-SKU-1")
            .price(15.0)
            .parent(existingProduct)
            .build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        when(productRepository.findBySlugAndIsPublishedTrue("new-slug")).thenReturn(Optional.empty());
        when(productRepository.findBySkuAndIsPublishedTrue("SKU-1")).thenReturn(Optional.empty());
        when(productRepository.findAllById(any())).thenReturn(List.of());
        when(productCategoryRepository.findAllByProductId(productId)).thenReturn(List.of());
        when(productOptionRepository.findAllByIdIn(List.of(optionId))).thenReturn(List.of(option));
        when(productRepository.saveAll(anyList())).thenReturn(List.of(savedVariation));

        // When
        productService.updateProduct(productId, productPutVm);

        // Then
        assertEquals("New Name", existingProduct.getName());
        assertEquals("new-slug", existingProduct.getSlug());
        assertEquals("SKU-1", existingProduct.getSku());
        assertEquals(99.0, existingProduct.getPrice());
        assertEquals(true, existingProduct.isAllowedToOrder());
        assertEquals(true, existingProduct.isPublished());
        assertEquals(false, existingProduct.isFeatured());
        assertEquals(true, existingProduct.isVisibleIndividually());
        assertEquals(true, existingProduct.isStockTrackingEnabled());
        assertEquals("Meta Title", existingProduct.getMetaTitle());
        assertEquals("Meta Keyword", existingProduct.getMetaKeyword());
        assertEquals("Meta Description", existingProduct.getMetaDescription());
        assertEquals(5L, existingProduct.getTaxClassId());

        verify(productRepository).save(existingProduct);
    }

    @Test
    void updateProduct_whenProductNotFound_thenThrowNotFoundException() {
        // Given
        long productId = 999L;

        ProductPutVm productPutVm = new ProductPutVm(
            "New Name",
            "new-slug",
            99.0,
            true,
            true,
            false,
            true,
            true,
            null,
            List.of(),
            null,
            null,
            null,
            "SKU-1",
            null,
            1.2,
            DimensionUnit.CM,
            10.0,
            5.0,
            2.0,
            null,
            null,
            null,
            null,
            List.of(),
            List.of(),
            List.of(new ProductOptionValuePutVm(1L, "text", 0, List.of("Red"))),
            List.of(),
            List.of(),
            5L
        );

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When + Then
        assertThrows(NotFoundException.class, () -> productService.updateProduct(productId, productPutVm));
    }

    @Test
    void updateProductQuantity_whenAllProductIdsValid_thenUpdateStockQuantitySuccessfully() {
        // Given
        long productId1 = 1L;
        long productId2 = 2L;

        Product product1 = Product.builder().id(productId1).stockQuantity(10L).build();
        Product product2 = Product.builder().id(productId2).stockQuantity(20L).build();

        List<ProductQuantityPostVm> request = List.of(
            new ProductQuantityPostVm(productId1, 111L),
            new ProductQuantityPostVm(productId2, 222L)
        );

        when(productRepository.findAllByIdIn(List.of(productId1, productId2)))
            .thenReturn(List.of(product1, product2));

        // When
        productService.updateProductQuantity(request);

        // Then
        ArgumentCaptor<List<Product>> captor = ArgumentCaptor.forClass(List.class);
        verify(productRepository).saveAll(captor.capture());

        List<Product> savedProducts = captor.getValue();
        assertEquals(2, savedProducts.size());

        Product saved1 = savedProducts.stream().filter(p -> p.getId().equals(productId1)).findFirst().orElseThrow();
        Product saved2 = savedProducts.stream().filter(p -> p.getId().equals(productId2)).findFirst().orElseThrow();

        assertEquals(111L, saved1.getStockQuantity());
        assertEquals(222L, saved2.getStockQuantity());
    }

    @Test
    void updateProductQuantity_whenRequestContainsNonExistingProductId_thenIgnoreAndUpdateFoundOnesOnly() {
        // Given
        long existingProductId = 1L;
        long missingProductId = 999L;

        Product existingProduct = Product.builder().id(existingProductId).stockQuantity(10L).build();

        List<ProductQuantityPostVm> request = List.of(
            new ProductQuantityPostVm(existingProductId, 555L),
            new ProductQuantityPostVm(missingProductId, 9999L)
        );

        when(productRepository.findAllByIdIn(List.of(existingProductId, missingProductId)))
            .thenReturn(List.of(existingProduct));

        // When
        productService.updateProductQuantity(request);

        // Then
        ArgumentCaptor<List<Product>> captor = ArgumentCaptor.forClass(List.class);
        verify(productRepository).saveAll(captor.capture());

        List<Product> savedProducts = captor.getValue();
        assertEquals(1, savedProducts.size());
        assertEquals(existingProductId, savedProducts.getFirst().getId());
        assertEquals(555L, savedProducts.getFirst().getStockQuantity());
    }

    @Test
    void updateProductCategories_whenValidCategoryIds_thenDeleteOldAndSaveNewCategories() throws Exception {
        // Given
        long productId = 50L;
        long categoryId1 = 1L;
        long categoryId2 = 2L;

        Product product = Product.builder().id(productId).build();
        ProductPutVm productPutVm = minimalProductPutVmWithCategoryIds(List.of(categoryId1, categoryId2));

        Category category1 = new Category();
        category1.setId(categoryId1);
        Category category2 = new Category();
        category2.setId(categoryId2);

        ProductCategory old1 = ProductCategory.builder().product(product).category(category1).build();
        ProductCategory old2 = ProductCategory.builder().product(product).category(category2).build();
        List<ProductCategory> oldCategories = List.of(old1, old2);

        when(categoryRepository.findAllById(List.of(categoryId1, categoryId2)))
            .thenReturn(List.of(category1, category2));
        when(productCategoryRepository.findAllByProductId(productId)).thenReturn(oldCategories);

        Method method = ProductService.class.getDeclaredMethod(
            "updateProductCategories",
            ProductPutVm.class,
            Product.class
        );
        method.setAccessible(true);

        // When
        method.invoke(productService, productPutVm, product);

        // Then
        verify(productCategoryRepository).deleteAllInBatch(oldCategories);

        ArgumentCaptor<List<ProductCategory>> newCategoriesCaptor = ArgumentCaptor.forClass(List.class);
        verify(productCategoryRepository).saveAll(newCategoriesCaptor.capture());

        List<ProductCategory> newCategories = newCategoriesCaptor.getValue();
        assertEquals(2, newCategories.size());
        assertEquals(
            List.of(categoryId1, categoryId2),
            newCategories.stream().map(pc -> pc.getCategory().getId()).toList()
        );
        assertEquals(
            List.of(productId, productId),
            newCategories.stream().map(pc -> pc.getProduct().getId()).toList()
        );
    }

    @Test
    void updateProductCategories_whenEmptyCategoryIds_thenDeleteOldAndSaveEmptyList() throws Exception {
        // Given
        long productId = 51L;
        long categoryId = 1L;

        Product product = Product.builder().id(productId).build();
        ProductPutVm productPutVm = minimalProductPutVmWithCategoryIds(List.of());

        Category category = new Category();
        category.setId(categoryId);
        List<ProductCategory> oldCategories = List.of(
            ProductCategory.builder().product(product).category(category).build()
        );

        when(productCategoryRepository.findAllByProductId(productId)).thenReturn(oldCategories);

        Method method = ProductService.class.getDeclaredMethod(
            "updateProductCategories",
            ProductPutVm.class,
            Product.class
        );
        method.setAccessible(true);

        // When
        method.invoke(productService, productPutVm, product);

        // Then
        verify(productCategoryRepository).deleteAllInBatch(oldCategories);

        ArgumentCaptor<List<ProductCategory>> newCategoriesCaptor = ArgumentCaptor.forClass(List.class);
        verify(productCategoryRepository).saveAll(newCategoriesCaptor.capture());
        assertEquals(0, newCategoriesCaptor.getValue().size());

        verify(categoryRepository, never()).findAllById(any());
    }

    @Test
    void getProductsWithFilter_whenValidParams_thenCallRepositoryAndMapToProductListGetVm() {
        // Given
        int pageNo = 0;
        int pageSize = 2;
        String productName = "  iPhone  ";
        String brandName = "  Apple  ";

        Product product1 = Product.builder().id(1L).name("iPhone 15").slug("iphone-15").price(100.0).build();
        Product product2 = Product.builder().id(2L).name("iPhone 15 Pro").slug("iphone-15-pro").price(200.0).build();

        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<Product> page = new PageImpl<>(List.of(product1, product2), pageable, 2);

        when(productRepository.getProductsWithFilter(eq("iphone"), eq("Apple"), any(Pageable.class)))
            .thenReturn(page);

        // When
        var result = productService.getProductsWithFilter(pageNo, pageSize, productName, brandName);

        // Then
        assertEquals(pageNo, result.pageNo());
        assertEquals(pageSize, result.pageSize());
        assertEquals(2, result.totalElements());
        assertEquals(1, result.totalPages());
        assertEquals(true, result.isLast());
        assertEquals(2, result.productContent().size());
        assertEquals(1L, result.productContent().getFirst().id());
        assertEquals("iPhone 15", result.productContent().getFirst().name());
        assertEquals("iphone-15", result.productContent().getFirst().slug());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(productRepository).getProductsWithFilter(eq("iphone"), eq("Apple"), pageableCaptor.capture());
        assertEquals(pageable, pageableCaptor.getValue());
    }

    @Test
    void getProductsWithFilter_whenProductNameAndBrandNameNull_thenHandleGracefullyWithoutNpe() {
        // Given
        int pageNo = 0;
        int pageSize = 10;

        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<Product> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(productRepository.getProductsWithFilter(eq(""), isNull(), any(Pageable.class)))
            .thenReturn(emptyPage);

        // When
        var result = assertDoesNotThrow(() -> productService.getProductsWithFilter(pageNo, pageSize, null, null));

        // Then
        assertEquals(0, result.productContent().size());
        verify(productRepository).getProductsWithFilter(eq(""), isNull(), any(Pageable.class));
    }

    @Test
    void subtractStockQuantity_whenMoreThanFiveUpdates_thenPartitionAndSaveAdjustedStock() {
        // Given
        List<ProductQuantityPutVm> updates = List.of(
            new ProductQuantityPutVm(1L, 1L),
            new ProductQuantityPutVm(2L, 2L),
            new ProductQuantityPutVm(3L, 3L),
            new ProductQuantityPutVm(4L, 4L),
            new ProductQuantityPutVm(5L, 5L),
            new ProductQuantityPutVm(6L, 6L)
        );

        when(productRepository.findAllByIdIn(anyList())).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            List<Long> ids = (List<Long>) invocation.getArgument(0);
            return ids.stream()
                .map(id -> Product.builder()
                    .id(id)
                    .stockTrackingEnabled(true)
                    .stockQuantity(10L)
                    .build())
                .toList();
        });
        when(productRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        ArgumentCaptor<List<Long>> idListCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<List<Product>> savedProductsCaptor = ArgumentCaptor.forClass(List.class);

        // When
        productService.subtractStockQuantity(updates);

        // Then
        verify(productRepository, times(2)).findAllByIdIn(idListCaptor.capture());
        List<List<Long>> capturedIdLists = idListCaptor.getAllValues();
        assertEquals(2, capturedIdLists.size());
        assertEquals(5, capturedIdLists.getFirst().size());
        assertEquals(1, capturedIdLists.get(1).size());

        verify(productRepository, times(2)).saveAll(savedProductsCaptor.capture());
        List<List<Product>> savedBatches = savedProductsCaptor.getAllValues();
        assertEquals(2, savedBatches.size());
        assertEquals(5, savedBatches.getFirst().size());
        assertEquals(1, savedBatches.get(1).size());

        List<Product> allSaved = savedBatches.stream().flatMap(List::stream).toList();
        assertEquals(9L, allSaved.stream().filter(p -> p.getId().equals(1L)).findFirst().orElseThrow().getStockQuantity());
        assertEquals(8L, allSaved.stream().filter(p -> p.getId().equals(2L)).findFirst().orElseThrow().getStockQuantity());
        assertEquals(7L, allSaved.stream().filter(p -> p.getId().equals(3L)).findFirst().orElseThrow().getStockQuantity());
        assertEquals(6L, allSaved.stream().filter(p -> p.getId().equals(4L)).findFirst().orElseThrow().getStockQuantity());
        assertEquals(5L, allSaved.stream().filter(p -> p.getId().equals(5L)).findFirst().orElseThrow().getStockQuantity());
        assertEquals(4L, allSaved.stream().filter(p -> p.getId().equals(6L)).findFirst().orElseThrow().getStockQuantity());
    }

    @Test
    void subtractStockQuantity_whenQuantityGreaterThanStock_thenClampRemainingStockToZero() {
        // Given
        ProductQuantityPutVm update = new ProductQuantityPutVm(1L, 100L);
        Product product = Product.builder()
            .id(1L)
            .stockTrackingEnabled(true)
            .stockQuantity(10L)
            .build();

        when(productRepository.findAllByIdIn(List.of(1L))).thenReturn(List.of(product));
        when(productRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        ArgumentCaptor<List<Product>> savedProductsCaptor = ArgumentCaptor.forClass(List.class);

        // When
        productService.subtractStockQuantity(List.of(update));

        // Then
        verify(productRepository).saveAll(savedProductsCaptor.capture());
        Product saved = savedProductsCaptor.getValue().getFirst();
        assertEquals(0L, saved.getStockQuantity());
    }
}
