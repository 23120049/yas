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

import com.yas.commonlibrary.exception.BadRequestException;
import com.yas.commonlibrary.exception.DuplicatedException;
import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.product.model.Brand;
import com.yas.product.model.Category;
import com.yas.product.model.Product;
import com.yas.product.model.ProductCategory;
import com.yas.product.model.ProductImage;
import com.yas.product.model.ProductOption;
import com.yas.product.model.ProductOptionCombination;
import com.yas.product.model.ProductRelated;
import com.yas.product.model.attribute.ProductAttribute;
import com.yas.product.model.attribute.ProductAttributeGroup;
import com.yas.product.model.attribute.ProductAttributeValue;
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
import com.yas.product.viewmodel.product.ProductExportingDetailVm;
import com.yas.product.viewmodel.product.ProductPostVm;
import com.yas.product.viewmodel.product.ProductPutVm;
import com.yas.product.viewmodel.product.ProductQuantityPostVm;
import com.yas.product.viewmodel.product.ProductQuantityPutVm;
import com.yas.product.viewmodel.product.ProductOptionValueDisplay;
import com.yas.product.viewmodel.product.ProductVariationPostVm;
import com.yas.product.viewmodel.product.ProductVariationPutVm;
import com.yas.product.viewmodel.product.ProductDetailVm;
import com.yas.product.viewmodel.product.ProductDetailGetVm;
import com.yas.product.viewmodel.product.ProductEsDetailVm;
import com.yas.product.viewmodel.productoption.ProductOptionValuePostVm;
import com.yas.product.viewmodel.productoption.ProductOptionValuePutVm;
import java.util.Map;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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

    @Test
    void updateProductRelations_whenValidRelatedProductIds_thenDeleteRemovedAndSaveNewRelations() throws Exception {
        // Given
        long productId = 100L;
        long oldKeepId = 1L;
        long oldRemoveId = 2L;
        long newAddId = 3L;

        Product product = Product.builder().id(productId).build();
        Product oldKeepProduct = Product.builder().id(oldKeepId).build();
        Product oldRemoveProduct = Product.builder().id(oldRemoveId).build();

        ProductRelated keepRelation = ProductRelated.builder()
            .product(product)
            .relatedProduct(oldKeepProduct)
            .build();
        ProductRelated removeRelation = ProductRelated.builder()
            .product(product)
            .relatedProduct(oldRemoveProduct)
            .build();
        product.setRelatedProducts(List.of(keepRelation, removeRelation));

        ProductPutVm productPutVm = new ProductPutVm(
            "Product",
            "product",
            1.0,
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
            List.of(oldKeepId, newAddId),
            1L
        );

        Product newRelatedProduct = Product.builder().id(newAddId).build();
        when(productRepository.findAllById(Set.of(newAddId))).thenReturn(List.of(newRelatedProduct));

        Method method = ProductService.class.getDeclaredMethod(
            "updateProductRelations",
            ProductPutVm.class,
            Product.class
        );
        method.setAccessible(true);

        // When
        method.invoke(productService, productPutVm, product);

        // Then
        ArgumentCaptor<List<ProductRelated>> deletedCaptor = ArgumentCaptor.forClass(List.class);
        verify(productRelatedRepository).deleteAll(deletedCaptor.capture());
        List<ProductRelated> deletedRelations = deletedCaptor.getValue();
        assertEquals(1, deletedRelations.size());
        assertEquals(oldRemoveId, deletedRelations.getFirst().getRelatedProduct().getId());

        ArgumentCaptor<List<ProductRelated>> savedCaptor = ArgumentCaptor.forClass(List.class);
        verify(productRelatedRepository).saveAll(savedCaptor.capture());
        List<ProductRelated> savedRelations = savedCaptor.getValue();
        assertEquals(1, savedRelations.size());
        assertEquals(productId, savedRelations.getFirst().getProduct().getId());
        assertEquals(newAddId, savedRelations.getFirst().getRelatedProduct().getId());
    }

    @Test
    void updateProductRelations_whenRelatedProductIdsEmpty_thenDeleteAllExistingRelationsAndSaveNone() throws Exception {
        // Given
        long productId = 101L;
        long oldId1 = 11L;
        long oldId2 = 12L;

        Product product = Product.builder().id(productId).build();
        Product oldProduct1 = Product.builder().id(oldId1).build();
        Product oldProduct2 = Product.builder().id(oldId2).build();

        ProductRelated oldRelation1 = ProductRelated.builder()
            .product(product)
            .relatedProduct(oldProduct1)
            .build();
        ProductRelated oldRelation2 = ProductRelated.builder()
            .product(product)
            .relatedProduct(oldProduct2)
            .build();
        product.setRelatedProducts(List.of(oldRelation1, oldRelation2));

        ProductPutVm productPutVm = new ProductPutVm(
            "Product",
            "product",
            1.0,
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

        when(productRepository.findAllById(Set.of())).thenReturn(List.of());

        Method method = ProductService.class.getDeclaredMethod(
            "updateProductRelations",
            ProductPutVm.class,
            Product.class
        );
        method.setAccessible(true);

        // When
        method.invoke(productService, productPutVm, product);

        // Then
        ArgumentCaptor<List<ProductRelated>> deletedCaptor = ArgumentCaptor.forClass(List.class);
        verify(productRelatedRepository).deleteAll(deletedCaptor.capture());
        List<ProductRelated> deletedRelations = deletedCaptor.getValue();
        assertEquals(2, deletedRelations.size());
        assertEquals(
            List.of(oldId1, oldId2),
            deletedRelations.stream().map(it -> it.getRelatedProduct().getId()).toList()
        );

        ArgumentCaptor<List<ProductRelated>> savedCaptor = ArgumentCaptor.forClass(List.class);
        verify(productRelatedRepository).saveAll(savedCaptor.capture());
        assertEquals(0, savedCaptor.getValue().size());
    }

    @Test
    void exportProducts_whenProductsFound_thenMapToProductExportingDetailVmSuccessfully() {
        // Given
        String productName = "  iPhone  ";
        String brandName = "  Apple  ";

        Brand brand = new Brand();
        brand.setId(7L);
        brand.setName("Apple");

        Product product = Product.builder()
            .id(1L)
            .name("iPhone 15")
            .shortDescription("Short")
            .description("Description")
            .specification("Specification")
            .sku("SKU-IPHONE-15")
            .gtin("GTIN-IPHONE-15")
            .slug("iphone-15")
            .isAllowedToOrder(true)
            .isPublished(true)
            .isFeatured(false)
            .isVisibleIndividually(true)
            .stockTrackingEnabled(true)
            .price(999.0)
            .brand(brand)
            .metaTitle("Meta Title")
            .metaKeyword("Meta Keyword")
            .metaDescription("Meta Description")
            .build();

        when(productRepository.getExportingProducts(eq("iphone"), eq("Apple")))
            .thenReturn(List.of(product));

        // When
        List<ProductExportingDetailVm> result = productService.exportProducts(productName, brandName);

        // Then
        assertEquals(1, result.size());
        ProductExportingDetailVm vm = result.getFirst();
        assertEquals(1L, vm.id());
        assertEquals("iPhone 15", vm.name());
        assertEquals("Short", vm.shortDescription());
        assertEquals("Description", vm.description());
        assertEquals("Specification", vm.specification());
        assertEquals("SKU-IPHONE-15", vm.sku());
        assertEquals("GTIN-IPHONE-15", vm.gtin());
        assertEquals("iphone-15", vm.slug());
        assertEquals(true, vm.isAllowedToOrder());
        assertEquals(true, vm.isPublished());
        assertEquals(false, vm.isFeatured());
        assertEquals(true, vm.isVisible());
        assertEquals(true, vm.stockTrackingEnabled());
        assertEquals(999.0, vm.price());
        assertEquals(7L, vm.brandId());
        assertEquals("Apple", vm.brandName());
        assertEquals("Meta Title", vm.metaTitle());
        assertEquals("Meta Keyword", vm.metaKeyword());
        assertEquals("Meta Description", vm.metaDescription());

        verify(productRepository).getExportingProducts(eq("iphone"), eq("Apple"));
    }

    @Test
    void getProductEsDetailById_whenProductExists_thenReturnVmWithCategoryAndAttributeNames() {
        // Given
        long productId = 200L;

        Category category1 = new Category();
        category1.setId(1L);
        category1.setName("Phones");

        Category category2 = new Category();
        category2.setId(2L);
        category2.setName("Electronics");

        ProductAttribute attribute1 = ProductAttribute.builder().id(11L).name("Color").build();
        ProductAttribute attribute2 = ProductAttribute.builder().id(12L).name("Storage").build();

        Product product = Product.builder()
            .id(productId)
            .name("iPhone 15")
            .slug("iphone-15")
            .price(999.0)
            .isPublished(true)
            .isVisibleIndividually(true)
            .isAllowedToOrder(true)
            .isFeatured(false)
            .thumbnailMediaId(777L)
            .build();

        ProductCategory productCategory1 = ProductCategory.builder().product(product).category(category1).build();
        ProductCategory productCategory2 = ProductCategory.builder().product(product).category(category2).build();
        product.setProductCategories(List.of(productCategory1, productCategory2));

        ProductAttributeValue attributeValue1 = new ProductAttributeValue();
        attributeValue1.setProduct(product);
        attributeValue1.setProductAttribute(attribute1);
        attributeValue1.setValue("Red");

        ProductAttributeValue attributeValue2 = new ProductAttributeValue();
        attributeValue2.setProduct(product);
        attributeValue2.setProductAttribute(attribute2);
        attributeValue2.setValue("128GB");

        product.setAttributeValues(List.of(attributeValue1, attributeValue2));

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // When
        ProductEsDetailVm result = productService.getProductEsDetailById(productId);

        // Then
        assertEquals(productId, result.id());
        assertEquals("iPhone 15", result.name());
        assertEquals("iphone-15", result.slug());
        assertEquals(999.0, result.price());
        assertEquals(true, result.isPublished());
        assertEquals(true, result.isVisibleIndividually());
        assertEquals(true, result.isAllowedToOrder());
        assertEquals(false, result.isFeatured());
        assertEquals(777L, result.thumbnailMediaId());
        assertEquals(List.of("Phones", "Electronics"), result.categories());
        assertEquals(List.of("Color", "Storage"), result.attributes());
    }

    @Test
    void getProductEsDetailById_whenProductNotFound_thenThrowNotFoundException() {
        // Given
        long productId = 9999L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When + Then
        assertThrows(NotFoundException.class, () -> productService.getProductEsDetailById(productId));
    }

    @Test
    void validateLengthMustGreaterThanWidth_whenLengthGreaterThanWidth_thenNotThrow() throws Exception {
        // Given
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
            1.0,
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

        Method method = ProductService.class.getDeclaredMethod(
            "validateLengthMustGreaterThanWidth",
            com.yas.product.viewmodel.product.ProductSaveVm.class
        );
        method.setAccessible(true);

        // When + Then
        assertDoesNotThrow(() -> method.invoke(productService, productPostVm));
    }

    @Test
    void validateLengthMustGreaterThanWidth_whenLengthLessThanWidth_thenThrowBadRequestException() throws Exception {
        // Given
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
            1.0,
            DimensionUnit.CM,
            4.0,
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

        Method method = ProductService.class.getDeclaredMethod(
            "validateLengthMustGreaterThanWidth",
            com.yas.product.viewmodel.product.ProductSaveVm.class
        );
        method.setAccessible(true);

        // When + Then
        InvocationTargetException thrown = assertThrows(
            InvocationTargetException.class,
            () -> method.invoke(productService, productPostVm)
        );
        assertEquals(BadRequestException.class, thrown.getCause().getClass());
    }

    @Test
    void validateProductVm_whenSlugDuplicatedInDatabase_thenThrowDuplicatedException() throws Exception {
        // Given
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
            1.0,
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

        Product existingProduct = Product.builder().id(999L).slug("product-a").build();
        when(productRepository.findBySlugAndIsPublishedTrue("product-a")).thenReturn(Optional.of(existingProduct));

        Method method = ProductService.class.getDeclaredMethod(
            "validateProductVm",
            com.yas.product.viewmodel.product.ProductSaveVm.class
        );
        method.setAccessible(true);

        // When + Then
        InvocationTargetException thrown = assertThrows(
            InvocationTargetException.class,
            () -> method.invoke(productService, productPostVm)
        );
        assertEquals(DuplicatedException.class, thrown.getCause().getClass());
    }

    @Test
    void restoreStockQuantity_whenValidProducts_thenIncreaseStockAndSaveAll() {
        // Given
        Product product1 = Product.builder()
            .id(1L)
            .stockTrackingEnabled(true)
            .stockQuantity(10L)
            .build();
        Product product2 = Product.builder()
            .id(2L)
            .stockTrackingEnabled(true)
            .stockQuantity(20L)
            .build();

        List<ProductQuantityPutVm> request = List.of(
            new ProductQuantityPutVm(1L, 5L),
            new ProductQuantityPutVm(2L, 7L)
        );

        when(productRepository.findAllByIdIn(List.of(1L, 2L))).thenReturn(List.of(product1, product2));
        when(productRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        ArgumentCaptor<List<Product>> savedCaptor = ArgumentCaptor.forClass(List.class);

        // When
        productService.restoreStockQuantity(request);

        // Then
        verify(productRepository).saveAll(savedCaptor.capture());
        List<Product> savedProducts = savedCaptor.getValue();
        assertEquals(2, savedProducts.size());

        Product saved1 = savedProducts.stream().filter(p -> p.getId().equals(1L)).findFirst().orElseThrow();
        Product saved2 = savedProducts.stream().filter(p -> p.getId().equals(2L)).findFirst().orElseThrow();
        assertEquals(15L, saved1.getStockQuantity());
        assertEquals(27L, saved2.getStockQuantity());
    }

    @Test
    void getProductVariationsByParentId_whenParentExistsAndHasOptionsTrue_thenReturnVariationVmsWithOptions() {
        // Given
        long parentId = 100L;
        long variationId = 101L;
        long optionId = 9L;
        long thumbnailId = 1000L;
        long imageId = 2000L;

        Product parentProduct = Product.builder()
            .id(parentId)
            .hasOptions(true)
            .build();

        Product variation = Product.builder()
            .id(variationId)
            .name("iPhone 15 - Red")
            .slug("iphone-15-red")
            .sku("SKU-RED")
            .gtin("GTIN-RED")
            .price(1099.0)
            .isPublished(true)
            .thumbnailMediaId(thumbnailId)
            .build();
        ProductImage variationImage = ProductImage.builder().imageId(imageId).product(variation).build();
        variation.setProductImages(List.of(variationImage));
        parentProduct.setProducts(List.of(variation));

        ProductOption option = new ProductOption();
        option.setId(optionId);
        option.setName("Color");

        ProductOptionCombination combination = ProductOptionCombination.builder()
            .product(variation)
            .productOption(option)
            .value("Red")
            .displayOrder(0)
            .build();

        when(productRepository.findById(parentId)).thenReturn(Optional.of(parentProduct));
        when(productOptionCombinationRepository.findAllByProduct(variation)).thenReturn(List.of(combination));
        when(mediaService.getMedia(thumbnailId))
            .thenReturn(new NoFileMediaVm(thumbnailId, "", "", "", "http://thumb-red"));
        when(mediaService.getMedia(imageId))
            .thenReturn(new NoFileMediaVm(imageId, "", "", "", "http://img-red"));

        // When
        var result = productService.getProductVariationsByParentId(parentId);

        // Then
        assertEquals(1, result.size());
        var vm = result.getFirst();
        assertEquals(variationId, vm.id());
        assertEquals("iPhone 15 - Red", vm.name());
        assertEquals("iphone-15-red", vm.slug());
        assertEquals("SKU-RED", vm.sku());
        assertEquals("GTIN-RED", vm.gtin());
        assertEquals(1099.0, vm.price());
        assertEquals(thumbnailId, vm.thumbnail().id());
        assertEquals("http://thumb-red", vm.thumbnail().url());
        assertEquals(1, vm.productImages().size());
        assertEquals(imageId, vm.productImages().getFirst().id());
        assertEquals("http://img-red", vm.productImages().getFirst().url());
        assertEquals(Map.of(optionId, "Red"), vm.options());
    }

    @Test
    void getLatestProducts_whenRepositoryReturnsProducts_thenMapToProductListVmSuccessfully() {
        // Given
        int count = 2;
        Product product = Product.builder()
            .id(1L)
            .name("Latest Product")
            .slug("latest-product")
            .price(99.0)
            .isAllowedToOrder(true)
            .isPublished(true)
            .isFeatured(false)
            .isVisibleIndividually(true)
            .taxClassId(1L)
            .build();

        when(productRepository.getLatestProducts(PageRequest.of(0, count))).thenReturn(List.of(product));

        // When
        var result = productService.getLatestProducts(count);

        // Then
        assertEquals(1, result.size());
        assertEquals(1L, result.getFirst().id());
        assertEquals("Latest Product", result.getFirst().name());
        assertEquals("latest-product", result.getFirst().slug());
        assertEquals(99.0, result.getFirst().price());
    }

    @Test
    void getProductsByBrand_whenBrandFound_thenMapToProductThumbnailVmSuccessfully() {
        // Given
        String brandSlug = "apple";
        long thumbnailId = 1000L;

        Brand brand = new Brand();
        brand.setId(7L);
        brand.setSlug(brandSlug);
        brand.setName("Apple");

        Product product = Product.builder()
            .id(1L)
            .name("iPhone 15")
            .slug("iphone-15")
            .thumbnailMediaId(thumbnailId)
            .build();

        when(brandRepository.findBySlug(brandSlug)).thenReturn(Optional.of(brand));
        when(productRepository.findAllByBrandAndIsPublishedTrueOrderByIdAsc(brand)).thenReturn(List.of(product));
        when(mediaService.getMedia(thumbnailId))
            .thenReturn(new NoFileMediaVm(thumbnailId, "", "", "", "http://thumb-iphone"));

        // When
        var result = productService.getProductsByBrand(brandSlug);

        // Then
        assertEquals(1, result.size());
        assertEquals(1L, result.getFirst().id());
        assertEquals("iPhone 15", result.getFirst().name());
        assertEquals("iphone-15", result.getFirst().slug());
        assertEquals("http://thumb-iphone", result.getFirst().thumbnailUrl());
    }

    @Test
    void getProductsFromCategory_whenCategoryFound_thenMapToProductListGetFromCategoryVmSuccessfully() {
        // Given
        int pageNo = 0;
        int pageSize = 10;
        String categorySlug = "phones";
        long thumbnailId = 2000L;

        Category category = new Category();
        category.setId(3L);
        category.setSlug(categorySlug);
        category.setName("Phones");

        Product product = Product.builder()
            .id(1L)
            .name("Galaxy S")
            .slug("galaxy-s")
            .thumbnailMediaId(thumbnailId)
            .build();

        ProductCategory productCategory = ProductCategory.builder()
            .product(product)
            .category(category)
            .build();

        PageRequest pageable = PageRequest.of(pageNo, pageSize);
        Page<ProductCategory> productCategoryPage = new PageImpl<>(List.of(productCategory), pageable, 1);

        when(categoryRepository.findBySlug(categorySlug)).thenReturn(Optional.of(category));
        when(productCategoryRepository.findAllByCategory(any(Pageable.class), eq(category))).thenReturn(productCategoryPage);
        when(mediaService.getMedia(thumbnailId))
            .thenReturn(new NoFileMediaVm(thumbnailId, "", "", "", "http://thumb-galaxy"));

        // When
        var result = productService.getProductsFromCategory(pageNo, pageSize, categorySlug);

        // Then
        assertEquals(pageNo, result.pageNo());
        assertEquals(pageSize, result.pageSize());
        assertEquals(1, result.totalElements());
        assertEquals(1, result.totalPages());
        assertEquals(true, result.isLast());
        assertEquals(1, result.productContent().size());
        assertEquals(1L, result.productContent().getFirst().id());
        assertEquals("Galaxy S", result.productContent().getFirst().name());
        assertEquals("galaxy-s", result.productContent().getFirst().slug());
        assertEquals("http://thumb-galaxy", result.productContent().getFirst().thumbnailUrl());
    }

    @Test
    void getRelatedProductsBackoffice_whenProductExists_thenReturnRelatedProductsAndMapParentIdCorrectly() {
        // Given
        long productId = 500L;
        long parentId = 900L;

        Product parent = Product.builder().id(parentId).build();

        Product relatedProduct = Product.builder()
            .id(501L)
            .name("Related A")
            .slug("related-a")
            .isAllowedToOrder(true)
            .isPublished(true)
            .isFeatured(false)
            .isVisibleIndividually(true)
            .price(199.0)
            .taxClassId(3L)
            .parent(parent)
            .build();

        Product ownerProduct = Product.builder().id(productId).build();
        ProductRelated relation = ProductRelated.builder()
            .product(ownerProduct)
            .relatedProduct(relatedProduct)
            .build();
        ownerProduct.setRelatedProducts(List.of(relation));

        when(productRepository.findById(productId)).thenReturn(Optional.of(ownerProduct));

        // When
        var result = productService.getRelatedProductsBackoffice(productId);

        // Then
        assertEquals(1, result.size());
        assertEquals(501L, result.getFirst().id());
        assertEquals("Related A", result.getFirst().name());
        assertEquals("related-a", result.getFirst().slug());
        assertEquals(199.0, result.getFirst().price());
        assertEquals(parentId, result.getFirst().parentId());
    }

    @Test
    void getRelatedProductsStorefront_whenMixedPublishedStatus_thenReturnPagedResultWithPublishedProductsOnly() {
        // Given
        long productId = 600L;
        int pageNo = 0;
        int pageSize = 2;
        long publishedThumbnailId = 7000L;

        Product ownerProduct = Product.builder().id(productId).build();

        Product publishedRelatedProduct = Product.builder()
            .id(601L)
            .name("Published Related")
            .slug("published-related")
            .isPublished(true)
            .thumbnailMediaId(publishedThumbnailId)
            .price(299.0)
            .build();

        Product unpublishedRelatedProduct = Product.builder()
            .id(602L)
            .name("Unpublished Related")
            .slug("unpublished-related")
            .isPublished(false)
            .thumbnailMediaId(8000L)
            .price(399.0)
            .build();

        ProductRelated publishedRelation = ProductRelated.builder()
            .product(ownerProduct)
            .relatedProduct(publishedRelatedProduct)
            .build();

        ProductRelated unpublishedRelation = ProductRelated.builder()
            .product(ownerProduct)
            .relatedProduct(unpublishedRelatedProduct)
            .build();

        PageRequest pageable = PageRequest.of(pageNo, pageSize);
        Page<ProductRelated> relatedProductsPage = new PageImpl<>(
            List.of(publishedRelation, unpublishedRelation),
            pageable,
            2
        );

        when(productRepository.findById(productId)).thenReturn(Optional.of(ownerProduct));
        when(productRelatedRepository.findAllByProduct(ownerProduct, pageable)).thenReturn(relatedProductsPage);
        when(mediaService.getMedia(publishedThumbnailId))
            .thenReturn(new NoFileMediaVm(publishedThumbnailId, "", "", "", "http://thumb-published"));

        // When
        var result = productService.getRelatedProductsStorefront(productId, pageNo, pageSize);

        // Then
        assertEquals(pageNo, result.pageNo());
        assertEquals(pageSize, result.pageSize());
        assertEquals(2, result.totalElements());
        assertEquals(1, result.totalPages());
        assertEquals(true, result.isLast());
        assertEquals(1, result.productContent().size());
        assertEquals(601L, result.productContent().getFirst().id());
        assertEquals("Published Related", result.productContent().getFirst().name());
        assertEquals("published-related", result.productContent().getFirst().slug());
        assertEquals("http://thumb-published", result.productContent().getFirst().thumbnailUrl());
        assertEquals(299.0, result.productContent().getFirst().price());
    }

    @Test
    void getProductDetail_whenPublishedProductFound_thenMapToProductDetailGetVmWithImageUrlsAndAttributeGroups() {
        // Given
        String slug = "iphone-15";
        long thumbnailId = 1000L;
        long imageId1 = 2000L;
        long imageId2 = 3000L;

        Brand brand = new Brand();
        brand.setId(7L);
        brand.setName("Apple");

        Category category = new Category();
        category.setId(1L);
        category.setName("Phones");

        ProductAttributeGroup group = new ProductAttributeGroup();
        group.setId(10L);
        group.setName("General");

        ProductAttribute colorAttribute = ProductAttribute.builder()
            .id(11L)
            .name("Color")
            .productAttributeGroup(group)
            .build();

        Product product = Product.builder()
            .id(1L)
            .name("iPhone 15")
            .slug(slug)
            .shortDescription("Short")
            .description("Description")
            .specification("Specification")
            .isAllowedToOrder(true)
            .isPublished(true)
            .isFeatured(false)
            .hasOptions(true)
            .price(999.0)
            .thumbnailMediaId(thumbnailId)
            .brand(brand)
            .build();

        ProductCategory productCategory = ProductCategory.builder().product(product).category(category).build();
        product.setProductCategories(List.of(productCategory));

        ProductImage productImage1 = ProductImage.builder().imageId(imageId1).product(product).build();
        ProductImage productImage2 = ProductImage.builder().imageId(imageId2).product(product).build();
        product.setProductImages(List.of(productImage1, productImage2));

        ProductAttributeValue attributeValue = new ProductAttributeValue();
        attributeValue.setProduct(product);
        attributeValue.setProductAttribute(colorAttribute);
        attributeValue.setValue("Red");
        product.setAttributeValues(List.of(attributeValue));

        when(productRepository.findBySlugAndIsPublishedTrue(slug)).thenReturn(Optional.of(product));
        when(mediaService.getMedia(thumbnailId))
            .thenReturn(new NoFileMediaVm(thumbnailId, "", "", "", "http://thumb"));
        when(mediaService.getMedia(imageId1))
            .thenReturn(new NoFileMediaVm(imageId1, "", "", "", "http://img-1"));
        when(mediaService.getMedia(imageId2))
            .thenReturn(new NoFileMediaVm(imageId2, "", "", "", "http://img-2"));

        // When
        ProductDetailGetVm result = productService.getProductDetail(slug);

        // Then
        assertEquals(1L, result.id());
        assertEquals("iPhone 15", result.name());
        assertEquals("Apple", result.brandName());
        assertEquals(List.of("Phones"), result.productCategories());
        assertEquals("Short", result.shortDescription());
        assertEquals("Description", result.description());
        assertEquals("Specification", result.specification());
        assertEquals(true, result.isAllowedToOrder());
        assertEquals(true, result.isPublished());
        assertEquals(false, result.isFeatured());
        assertEquals(true, result.hasOptions());
        assertEquals(999.0, result.price());
        assertEquals("http://thumb", result.thumbnailMediaUrl());
        assertEquals(List.of("http://img-1", "http://img-2"), result.productImageMediaUrls());
        assertEquals(1, result.productAttributeGroups().size());
        assertEquals("General", result.productAttributeGroups().getFirst().name());
        assertEquals(1, result.productAttributeGroups().getFirst().productAttributeValues().size());
        assertEquals("Color", result.productAttributeGroups().getFirst().productAttributeValues().getFirst().name());
        assertEquals("Red", result.productAttributeGroups().getFirst().productAttributeValues().getFirst().value());
    }

    @Test
    void getProductDetail_whenSlugNotFound_thenThrowNotFoundException() {
        // Given
        String slug = "not-found";
        when(productRepository.findBySlugAndIsPublishedTrue(slug)).thenReturn(Optional.empty());

        // When + Then
        assertThrows(NotFoundException.class, () -> productService.getProductDetail(slug));
    }
}
