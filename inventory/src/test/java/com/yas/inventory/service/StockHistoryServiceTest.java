package com.yas.inventory.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yas.inventory.model.Stock;
import com.yas.inventory.model.StockHistory;
import com.yas.inventory.model.Warehouse;
import com.yas.inventory.repository.StockHistoryRepository;
import com.yas.inventory.viewmodel.product.ProductInfoVm;
import com.yas.inventory.viewmodel.stock.StockQuantityVm;
import com.yas.inventory.viewmodel.stockhistory.StockHistoryListVm;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StockHistoryServiceTest {

    @Mock
    private StockHistoryRepository stockHistoryRepository;

    @Mock
    private ProductService productService;

    @InjectMocks
    private StockHistoryService stockHistoryService;

    private Warehouse warehouse;
    private Stock stock1;
    private Stock stock2;
    private StockQuantityVm sqv1;
    private StockQuantityVm sqv2;

    @BeforeEach
    void setUp() {
        warehouse = Warehouse.builder().id(1L).name("Warehouse 1").build();

        stock1 = Stock.builder().id(10L).productId(100L).warehouse(warehouse).build();
        stock2 = Stock.builder().id(20L).productId(200L).warehouse(warehouse).build();

        sqv1 = new StockQuantityVm(10L, 50L, "Restock batch 1");
        sqv2 = new StockQuantityVm(20L, -5L, "Defective items removed");
    }

    @Test
    void createStockHistories_whenStockMatchesQuantityVm_thenSaveStockHistories() {
        List<Stock> stocks = List.of(stock1, stock2);
        List<StockQuantityVm> stockQuantityVms = List.of(sqv1, sqv2);

        stockHistoryService.createStockHistories(stocks, stockQuantityVms);

        ArgumentCaptor<List<StockHistory>> captor = ArgumentCaptor.forClass(List.class);
        verify(stockHistoryRepository, times(1)).saveAll(captor.capture());

        List<StockHistory> savedHistories = captor.getValue();
        assertEquals(2, savedHistories.size());

        assertEquals(100L, savedHistories.get(0).getProductId());
        assertEquals(50L, savedHistories.get(0).getAdjustedQuantity());
        assertEquals("Restock batch 1", savedHistories.get(0).getNote());
        assertEquals(warehouse, savedHistories.get(0).getWarehouse());

        assertEquals(200L, savedHistories.get(1).getProductId());
        assertEquals(-5L, savedHistories.get(1).getAdjustedQuantity());
        assertEquals("Defective items removed", savedHistories.get(1).getNote());
    }

    @Test
    void createStockHistories_whenNoMatchingQuantityVm_thenSaveEmptyList() {
        List<Stock> stocks = List.of(stock1);
        // The VM has a different stockId (99L) than the stock (10L)
        StockQuantityVm unmatchedVm = new StockQuantityVm(99L, 10L, "Mismatch");
        List<StockQuantityVm> stockQuantityVms = List.of(unmatchedVm);

        stockHistoryService.createStockHistories(stocks, stockQuantityVms);

        ArgumentCaptor<List<StockHistory>> captor = ArgumentCaptor.forClass(List.class);
        verify(stockHistoryRepository, times(1)).saveAll(captor.capture());

        List<StockHistory> savedHistories = captor.getValue();
        assertTrue(savedHistories.isEmpty());
    }

    @Test
    void createStockHistories_whenEmptyInputs_thenSaveEmptyList() {
        stockHistoryService.createStockHistories(Collections.emptyList(), Collections.emptyList());

        ArgumentCaptor<List<StockHistory>> captor = ArgumentCaptor.forClass(List.class);
        verify(stockHistoryRepository, times(1)).saveAll(captor.capture());

        List<StockHistory> savedHistories = captor.getValue();
        assertTrue(savedHistories.isEmpty());
    }

    @Test
    void getStockHistories_whenHistoriesFound_thenReturnStockHistoryListVm() {
        Long productId = 100L;
        Long warehouseId = 1L;

        StockHistory history1 = StockHistory.builder()
                .id(1L)
                .productId(productId)
                .warehouse(warehouse)
                .adjustedQuantity(50L)
                .note("Initial stock")
                .build();

        ProductInfoVm productInfoVm = new ProductInfoVm(productId, "Product A", "SKU-A", true);

        when(stockHistoryRepository.findByProductIdAndWarehouseIdOrderByCreatedOnDesc(productId, warehouseId))
                .thenReturn(List.of(history1));
        when(productService.getProduct(productId)).thenReturn(productInfoVm);

        StockHistoryListVm result = stockHistoryService.getStockHistories(productId, warehouseId);

        assertNotNull(result);
        assertEquals(1, result.data().size());
        assertEquals("Product A", result.data().get(0).productName());
        assertEquals(50L, result.data().get(0).adjustedQuantity());
        assertEquals("Initial stock", result.data().get(0).note());
    }

    @Test
    void getStockHistories_whenNoHistoriesFound_thenReturnEmptyListVm() {
        Long productId = 100L;
        Long warehouseId = 1L;

        when(stockHistoryRepository.findByProductIdAndWarehouseIdOrderByCreatedOnDesc(productId, warehouseId))
                .thenReturn(Collections.emptyList());
        when(productService.getProduct(productId))
                .thenReturn(new ProductInfoVm(productId, "Product A", "SKU-A", false));

        StockHistoryListVm result = stockHistoryService.getStockHistories(productId, warehouseId);

        assertNotNull(result);
        assertTrue(result.data().isEmpty());
    }
}
