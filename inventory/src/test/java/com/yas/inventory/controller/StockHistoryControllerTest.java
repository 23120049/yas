package com.yas.inventory.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;

import org.springframework.test.web.servlet.MockMvc;
import com.yas.inventory.service.StockHistoryService;
import com.yas.inventory.viewmodel.product.ProductInfoVm;
import com.yas.inventory.model.StockHistory;
import com.yas.inventory.viewmodel.stockhistory.StockHistoryVm;
import com.yas.inventory.viewmodel.stockhistory.StockHistoryListVm;

@WebMvcTest(controllers = StockHistoryController.class,
    excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class StockHistoryControllerTest {

    @MockitoBean
    private StockHistoryService stockHistoryService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGetStockHistories_whenParametersAreValid_thenReturnStockHistories() throws Exception {

        Long productId = 1L;
        Long warehouseId = 1L;

        ProductInfoVm productInfoVm = new ProductInfoVm(1L, "Product1", "SKU123", true);

        StockHistory stockHistory1 = StockHistory.builder()
            .id(1L)
            .productId(1L)
            .adjustedQuantity(100L)
            .note("Initial stock")
            .build();

        StockHistory stockHistory2 = StockHistory.builder()
            .id(2L)
            .productId(1L) // Same product ID
            .adjustedQuantity(-50L)
            .note("Removed stock")
            .build();

        StockHistoryVm stockHistoryVm1 = StockHistoryVm.fromModel(stockHistory1, productInfoVm);
        StockHistoryVm stockHistoryVm2 = StockHistoryVm.fromModel(stockHistory2, productInfoVm);

        StockHistoryListVm stockHistoryListVm = new StockHistoryListVm(List.of(stockHistoryVm1, stockHistoryVm2));

        given(stockHistoryService.getStockHistories(productId, warehouseId))
            .willReturn(stockHistoryListVm);

        this.mockMvc.perform(get("/backoffice/stocks/histories")
                .param("productId", String.valueOf(productId))
                .param("warehouseId", String.valueOf(warehouseId))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(2))
            .andExpect(jsonPath("$.data[0].id").value(stockHistoryVm1.id()))
            .andExpect(jsonPath("$.data[0].productName").value(stockHistoryVm1.productName()))
            .andExpect(jsonPath("$.data[0].adjustedQuantity")
                .value(stockHistoryVm1.adjustedQuantity()))
            .andExpect(jsonPath("$.data[1].id").value(stockHistoryVm2.id()))
            .andExpect(jsonPath("$.data[1].productName").value(stockHistoryVm2.productName()))
            .andExpect(jsonPath("$.data[1].adjustedQuantity")
                .value(stockHistoryVm2.adjustedQuantity()));
    }

    @Test
    void testGetStockHistories_whenNoHistoriesFound_thenReturnEmptyList() throws Exception {
        Long productId = 1L;
        Long warehouseId = 1L;

        StockHistoryListVm stockHistoryListVm = new StockHistoryListVm(Collections.emptyList());

        given(stockHistoryService.getStockHistories(productId, warehouseId))
            .willReturn(stockHistoryListVm);

        this.mockMvc.perform(get("/backoffice/stocks/histories")
                .param("productId", String.valueOf(productId))
                .param("warehouseId", String.valueOf(warehouseId))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testGetStockHistories_whenProductIdIsMissing_thenReturnBadRequest() throws Exception {
        this.mockMvc.perform(get("/backoffice/stocks/histories")
                .param("warehouseId", "1")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testGetStockHistories_whenWarehouseIdIsMissing_thenReturnBadRequest() throws Exception {
        this.mockMvc.perform(get("/backoffice/stocks/histories")
                .param("productId", "1")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }
}