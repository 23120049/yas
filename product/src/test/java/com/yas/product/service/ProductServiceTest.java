package com.yas.product.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

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
import com.yas.product.viewmodel.product.ProductVariationPutVm;
import com.yas.product.viewmodel.product.ProductDetailVm;
import com.yas.product.viewmodel.productoption.ProductOptionValuePutVm;
import java.util.Map;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

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
}
