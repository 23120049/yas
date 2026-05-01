package com.yas.inventory.service;

import com.yas.commonlibrary.exception.BadRequestException;
import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.commonlibrary.exception.StockExistingException;
import com.yas.inventory.model.Stock;
import com.yas.inventory.model.Warehouse;
import com.yas.inventory.model.enumeration.FilterExistInWhSelection;
import com.yas.inventory.repository.StockRepository;
import com.yas.inventory.repository.WarehouseRepository;
import com.yas.inventory.viewmodel.product.ProductInfoVm;
import com.yas.inventory.viewmodel.product.ProductQuantityPostVm;
import com.yas.inventory.viewmodel.stock.StockPostVm;
import com.yas.inventory.viewmodel.stock.StockQuantityUpdateVm;
import com.yas.inventory.viewmodel.stock.StockQuantityVm;
import com.yas.inventory.viewmodel.stock.StockVm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class StockServiceTest {

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private StockRepository stockRepository;

    @Mock
    private ProductService productService;

    @Mock
    private WarehouseService warehouseService;

    @Mock
    private StockHistoryService stockHistoryService;

    @InjectMocks
    private StockService stockService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void addProductIntoWarehouse_Success() {
        StockPostVm stockPostVm = new StockPostVm(101L, 1L);
        List<StockPostVm> postVms = List.of(stockPostVm);

        when(stockRepository.existsByWarehouseIdAndProductId(1L, 101L)).thenReturn(false);
        when(productService.getProduct(101L)).thenReturn(new ProductInfoVm(101L, "Product A", "SKU-A", false));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(Warehouse.builder().id(1L).build()));

        stockService.addProductIntoWarehouse(postVms);

        verify(stockRepository, times(1)).saveAll(anyList());
    }

    @Test
    void addProductIntoWarehouse_StockAlreadyExisted_ShouldThrowStockExistingException() {
        StockPostVm stockPostVm = new StockPostVm(101L, 1L);
        List<StockPostVm> postVms = List.of(stockPostVm);

        when(stockRepository.existsByWarehouseIdAndProductId(1L, 101L)).thenReturn(true);

        assertThrows(StockExistingException.class, () -> stockService.addProductIntoWarehouse(postVms));
    }

    @Test
    void addProductIntoWarehouse_ProductNotFound_ShouldThrowNotFoundException() {
        StockPostVm stockPostVm = new StockPostVm(101L, 1L);
        List<StockPostVm> postVms = List.of(stockPostVm);

        when(stockRepository.existsByWarehouseIdAndProductId(1L, 101L)).thenReturn(false);
        when(productService.getProduct(101L)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> stockService.addProductIntoWarehouse(postVms));
    }

    @Test
    void addProductIntoWarehouse_WarehouseNotFound_ShouldThrowNotFoundException() {
        StockPostVm stockPostVm = new StockPostVm(101L, 1L);
        List<StockPostVm> postVms = List.of(stockPostVm);

        when(stockRepository.existsByWarehouseIdAndProductId(1L, 101L)).thenReturn(false);
        when(productService.getProduct(101L)).thenReturn(new ProductInfoVm(101L, "Product A", "SKU-A", false));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> stockService.addProductIntoWarehouse(postVms));
    }

    @Test
    void addProductIntoWarehouse_EmptyList_ShouldDoNothing() {
        stockService.addProductIntoWarehouse(Collections.emptyList());
        verify(stockRepository, times(1)).saveAll(Collections.emptyList());
    }

    @Test
    void getStocksByWarehouseIdAndProductNameAndSku_Success() {
        Long warehouseId = 1L;
        String productName = "Product";
        String productSku = "SKU";
        ProductInfoVm productInfoVm = new ProductInfoVm(101L, productName, productSku, true);
        List<ProductInfoVm> productInfoVms = List.of(productInfoVm);

        when(warehouseService.getProductWarehouse(warehouseId, productName, productSku, FilterExistInWhSelection.YES))
                .thenReturn(productInfoVms);

        Stock stock = Stock.builder().id(1L).productId(101L).warehouse(Warehouse.builder().id(warehouseId).build()).quantity(10L).build();
        when(stockRepository.findByWarehouseIdAndProductIdIn(warehouseId, List.of(101L))).thenReturn(List.of(stock));

        List<StockVm> result = stockService.getStocksByWarehouseIdAndProductNameAndSku(warehouseId, productName, productSku);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(101L, result.get(0).productId());
    }

    @Test
    void getStocksByWarehouseIdAndProductNameAndSku_NoProductsFound_ShouldReturnEmptyList() {
        Long warehouseId = 1L;
        String productName = "Product";
        String productSku = "SKU";

        when(warehouseService.getProductWarehouse(warehouseId, productName, productSku, FilterExistInWhSelection.YES))
                .thenReturn(Collections.emptyList());

        List<StockVm> result = stockService.getStocksByWarehouseIdAndProductNameAndSku(warehouseId, productName, productSku);

        assertTrue(result.isEmpty());
        verify(stockRepository, times(1)).findByWarehouseIdAndProductIdIn(warehouseId, Collections.emptyList());
    }

    @Test
    void getStocksByWarehouseIdAndProductNameAndSku_NoStocksFound_ShouldReturnEmptyList() {
        Long warehouseId = 1L;
        String productName = "Product";
        String productSku = "SKU";
        ProductInfoVm productInfoVm = new ProductInfoVm(101L, productName, productSku, true);
        List<ProductInfoVm> productInfoVms = List.of(productInfoVm);

        when(warehouseService.getProductWarehouse(warehouseId, productName, productSku, FilterExistInWhSelection.YES))
                .thenReturn(productInfoVms);
        when(stockRepository.findByWarehouseIdAndProductIdIn(warehouseId, List.of(101L))).thenReturn(Collections.emptyList());

        List<StockVm> result = stockService.getStocksByWarehouseIdAndProductNameAndSku(warehouseId, productName, productSku);

        assertTrue(result.isEmpty());
    }

    @Test
    void updateProductQuantityInStock_Success() {
        StockQuantityVm stockQuantityVm = new StockQuantityVm(1L, 5L, "note");
        StockQuantityUpdateVm requestBody = new StockQuantityUpdateVm(List.of(stockQuantityVm));
        Stock stock = Stock.builder().id(1L).quantity(10L).productId(101L).build();

        when(stockRepository.findAllById(List.of(1L))).thenReturn(List.of(stock));

        stockService.updateProductQuantityInStock(requestBody);

        assertEquals(15L, stock.getQuantity());
        verify(stockRepository, times(1)).saveAll(List.of(stock));
        verify(stockHistoryService, times(1)).createStockHistories(anyList(), anyList());
        verify(productService, times(1)).updateProductQuantity(anyList());
    }
    
    @Test
    void updateProductQuantityInStock_WithNegativeAdjustedQuantity_Success() {
        StockQuantityVm stockQuantityVm = new StockQuantityVm(1L, -5L, "note");
        StockQuantityUpdateVm requestBody = new StockQuantityUpdateVm(List.of(stockQuantityVm));
        Stock stock = Stock.builder().id(1L).quantity(10L).productId(101L).build();

        when(stockRepository.findAllById(List.of(1L))).thenReturn(List.of(stock));

        stockService.updateProductQuantityInStock(requestBody);

        assertEquals(5L, stock.getQuantity());
        verify(stockRepository, times(1)).saveAll(List.of(stock));
        verify(stockHistoryService, times(1)).createStockHistories(anyList(), anyList());
        verify(productService, times(1)).updateProductQuantity(anyList());
    }

    @Test
    void updateProductQuantityInStock_InvalidAdjustedQuantity_ShouldThrowBadRequestException() {
        StockQuantityVm stockQuantityVm = new StockQuantityVm(1L, -15L, "note");
        StockQuantityUpdateVm requestBody = new StockQuantityUpdateVm(List.of(stockQuantityVm));
        Stock stock = Stock.builder().id(1L).quantity(-20L).build();

        when(stockRepository.findAllById(List.of(1L))).thenReturn(List.of(stock));

        assertThrows(BadRequestException.class, () -> stockService.updateProductQuantityInStock(requestBody));
    }

    @Test
    void updateProductQuantityInStock_StockQuantityVmIsNull_ShouldContinue() {
        StockQuantityVm stockQuantityVm = new StockQuantityVm(2L, 5L, "note"); // Different stockId
        StockQuantityUpdateVm requestBody = new StockQuantityUpdateVm(List.of(stockQuantityVm));
        Stock stock = Stock.builder().id(1L).quantity(10L).build();

        when(stockRepository.findAllById(List.of(2L))).thenReturn(List.of(stock));

        stockService.updateProductQuantityInStock(requestBody);

        assertEquals(10L, stock.getQuantity()); // Quantity should not change
        verify(stockRepository, times(1)).saveAll(anyList());
    }

    @Test
    void updateProductQuantityInStock_AdjustedQuantityIsNull_ShouldUseZero() {
        StockQuantityVm stockQuantityVm = new StockQuantityVm(1L, null, "note");
        StockQuantityUpdateVm requestBody = new StockQuantityUpdateVm(List.of(stockQuantityVm));
        Stock stock = Stock.builder().id(1L).quantity(10L).productId(101L).build();

        when(stockRepository.findAllById(List.of(1L))).thenReturn(List.of(stock));

        stockService.updateProductQuantityInStock(requestBody);

        assertEquals(10L, stock.getQuantity());
        verify(stockRepository, times(1)).saveAll(List.of(stock));
    }

    @Test
    void updateProductQuantityInStock_EmptyList_ShouldDoNothing() {
        StockQuantityUpdateVm requestBody = new StockQuantityUpdateVm(Collections.emptyList());

        stockService.updateProductQuantityInStock(requestBody);

        verify(stockRepository, times(1)).findAllById(Collections.emptyList());
        verify(stockRepository, times(1)).saveAll(Collections.emptyList());
        verify(stockHistoryService, times(1)).createStockHistories(Collections.emptyList(), Collections.emptyList());
        verify(productService, never()).updateProductQuantity(anyList());
    }
    
    @Test
    void updateProductQuantityInStock_ProductQuantityPostVmsIsEmpty_ShouldNotUpdate() {
        StockQuantityVm stockQuantityVm = new StockQuantityVm(1L, 5L, "note");
        StockQuantityUpdateVm requestBody = new StockQuantityUpdateVm(List.of(stockQuantityVm));

        // Return empty list so productQuantityPostVms is empty
        when(stockRepository.findAllById(List.of(1L))).thenReturn(Collections.emptyList());

        stockService.updateProductQuantityInStock(requestBody);

        verify(productService, never()).updateProductQuantity(anyList());
    }
}
