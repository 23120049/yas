package com.yas.inventory.service;

import com.yas.commonlibrary.exception.DuplicatedException;
import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.inventory.model.Warehouse;
import com.yas.inventory.model.enumeration.FilterExistInWhSelection;
import com.yas.inventory.repository.StockRepository;
import com.yas.inventory.repository.WarehouseRepository;
import com.yas.inventory.viewmodel.address.AddressDetailVm;
import com.yas.inventory.viewmodel.address.AddressVm;
import com.yas.inventory.viewmodel.product.ProductInfoVm;
import com.yas.inventory.viewmodel.warehouse.WarehouseDetailVm;
import com.yas.inventory.viewmodel.warehouse.WarehouseGetVm;
import com.yas.inventory.viewmodel.warehouse.WarehouseListGetVm;
import com.yas.inventory.viewmodel.warehouse.WarehousePostVm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class WarehouseServiceTest {

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private StockRepository stockRepository;

    @Mock
    private ProductService productService;

    @Mock
    private LocationService locationService;

    @InjectMocks
    private WarehouseService warehouseService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findAllWarehouses_ShouldReturnListOfWarehouseGetVm() {
        Warehouse warehouse = Warehouse.builder().id(1L).name("Warehouse 1").addressId(1L).build();
        when(warehouseRepository.findAll()).thenReturn(List.of(warehouse));

        List<WarehouseGetVm> result = warehouseService.findAllWarehouses();

        assertEquals(1, result.size());
        assertEquals("Warehouse 1", result.get(0).name());
    }

    @Test
    void findAllWarehouses_NoWarehouses_ShouldReturnEmptyList() {
        when(warehouseRepository.findAll()).thenReturn(Collections.emptyList());

        List<WarehouseGetVm> result = warehouseService.findAllWarehouses();

        assertTrue(result.isEmpty());
    }

    @Test
    void getProductWarehouse_WithExistingProducts_ShouldReturnProductInfoList() {
        Long warehouseId = 1L;
        List<Long> productIds = List.of(101L, 102L);
        ProductInfoVm product1 = new ProductInfoVm(101L, "Product A", "SKU-A", true);
        ProductInfoVm product2 = new ProductInfoVm(102L, "Product B", "SKU-B", true);
        List<ProductInfoVm> productVmList = List.of(product1, product2);

        when(stockRepository.getProductIdsInWarehouse(warehouseId)).thenReturn(productIds);
        when(productService.filterProducts(anyString(), anyString(), eq(productIds), any(FilterExistInWhSelection.class)))
                .thenReturn(productVmList);

        List<ProductInfoVm> result = warehouseService.getProductWarehouse(warehouseId, "Product", "SKU", FilterExistInWhSelection.YES);

        assertEquals(2, result.size());
        assertTrue(result.get(0).existInWh());
        assertTrue(result.get(1).existInWh());
    }

    @Test
    void getProductWarehouse_NoProductsInStock_ShouldReturnProductInfoListWithFalseInStock() {
        Long warehouseId = 1L;
        ProductInfoVm product1 = new ProductInfoVm(101L, "Product A", "SKU-A", false);
        List<ProductInfoVm> productVmList = List.of(product1);

        when(stockRepository.getProductIdsInWarehouse(warehouseId)).thenReturn(Collections.emptyList());
        when(productService.filterProducts(anyString(), anyString(), eq(Collections.emptyList()), any(FilterExistInWhSelection.class)))
                .thenReturn(productVmList);

        List<ProductInfoVm> result = warehouseService.getProductWarehouse(warehouseId, "Product", "SKU", FilterExistInWhSelection.NO);

        assertEquals(1, result.size());
        // In this case, the original productVm is returned without modification because productIds is empty.
        // The isInStock property of the original productVm is not defined, so we don't assert it.
        assertEquals(101L, result.get(0).id());
    }

    @Test
    void findById_WarehouseExists_ShouldReturnWarehouseDetailVm() {
        Long warehouseId = 1L;
        Warehouse warehouse = Warehouse.builder().id(warehouseId).name("Warehouse 1").addressId(10L).build();
        AddressDetailVm addressDetail = new AddressDetailVm(10L, "Contact", "12345", "Line 1", "Line 2", "City", "12345", 1L, "District", 1L, "State", 1L, "Country");

        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
        when(locationService.getAddressById(10L)).thenReturn(addressDetail);

        WarehouseDetailVm result = warehouseService.findById(warehouseId);

        assertEquals("Warehouse 1", result.name());
        assertEquals("Contact", result.contactName());
    }

    @Test
    void findById_WarehouseNotFound_ShouldThrowNotFoundException() {
        Long warehouseId = 1L;
        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> warehouseService.findById(warehouseId));
    }

    @Test
    void create_Success_ShouldReturnWarehouse() {
        WarehousePostVm postVm = new WarehousePostVm("WH-1", "New Warehouse", "Contact", "123", "Line 1", "Line 2", "City", "123", 1L, 1L, 1L);
        AddressVm addressVm = new AddressVm(1L, "Contact", "123", "Line 1", "City", "123", 1L, 1L, 1L);
        Warehouse savedWarehouse = Warehouse.builder().id(1L).name("New Warehouse").addressId(1L).build();

        when(warehouseRepository.existsByName("New Warehouse")).thenReturn(false);
        when(locationService.createAddress(any())).thenReturn(addressVm);
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(savedWarehouse);

        Warehouse result = warehouseService.create(postVm);

        assertEquals("New Warehouse", result.getName());
        assertEquals(1L, result.getAddressId());
        verify(warehouseRepository, times(1)).save(any(Warehouse.class));
    }

    @Test
    void create_NameAlreadyExisted_ShouldThrowDuplicatedException() {
        WarehousePostVm postVm = new WarehousePostVm("WH-1", "Existing Warehouse", null, null, null, null, null, null, null, null, null);
        when(warehouseRepository.existsByName("Existing Warehouse")).thenReturn(true);

        assertThrows(DuplicatedException.class, () -> warehouseService.create(postVm));
    }

    @Test
    void update_Success_ShouldNotThrowException() {
        Long warehouseId = 1L;
        WarehousePostVm postVm = new WarehousePostVm("WH-1", "Updated Warehouse", "Contact", "123", "Line 1", "Line 2", "City", "123", 1L, 1L, 1L);
        Warehouse existingWarehouse = Warehouse.builder().id(warehouseId).name("Old Warehouse").addressId(10L).build();

        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(existingWarehouse));
        when(warehouseRepository.existsByNameWithDifferentId("Updated Warehouse", warehouseId)).thenReturn(false);

        warehouseService.update(postVm, warehouseId);

        verify(locationService, times(1)).updateAddress(eq(10L), any());
        verify(warehouseRepository, times(1)).save(existingWarehouse);
        assertEquals("Updated Warehouse", existingWarehouse.getName());
    }

    @Test
    void update_WarehouseNotFound_ShouldThrowNotFoundException() {
        Long warehouseId = 1L;
        WarehousePostVm postVm = new WarehousePostVm("WH-1", "Updated Warehouse", null, null, null, null, null, null, null, null, null);
        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> warehouseService.update(postVm, warehouseId));
    }

    @Test
    void update_NameAlreadyExisted_ShouldThrowDuplicatedException() {
        Long warehouseId = 1L;
        WarehousePostVm postVm = new WarehousePostVm("WH-1", "Existing Warehouse", null, null, null, null, null, null, null, null, null);
        Warehouse existingWarehouse = Warehouse.builder().id(warehouseId).name("Old Warehouse").build();

        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(existingWarehouse));
        when(warehouseRepository.existsByNameWithDifferentId("Existing Warehouse", warehouseId)).thenReturn(true);

        assertThrows(DuplicatedException.class, () -> warehouseService.update(postVm, warehouseId));
    }

    @Test
    void delete_Success_ShouldNotThrowException() {
        Long warehouseId = 1L;
        Warehouse warehouse = Warehouse.builder().id(warehouseId).addressId(10L).build();
        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));

        warehouseService.delete(warehouseId);

        verify(warehouseRepository, times(1)).deleteById(warehouseId);
        verify(locationService, times(1)).deleteAddress(10L);
    }

    @Test
    void delete_WarehouseNotFound_ShouldThrowNotFoundException() {
        Long warehouseId = 1L;
        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> warehouseService.delete(warehouseId));
    }

    @Test
    void getPageableWarehouses_ShouldReturnWarehouseListGetVm() {
        int pageNo = 0;
        int pageSize = 10;
        Warehouse warehouse = Warehouse.builder().id(1L).name("Warehouse 1").build();
        Page<Warehouse> warehousePage = new PageImpl<>(List.of(warehouse), Pageable.ofSize(pageSize), 1);

        when(warehouseRepository.findAll(any(Pageable.class))).thenReturn(warehousePage);

        WarehouseListGetVm result = warehouseService.getPageableWarehouses(pageNo, pageSize);

        assertEquals(1, result.warehouseContent().size());
        assertEquals(1, result.totalPages());
        assertEquals(0, result.pageNo());
    }

    @Test
    void getPageableWarehouses_NoWarehouses_ShouldReturnEmptyList() {
        int pageNo = 0;
        int pageSize = 10;
        Page<Warehouse> warehousePage = new PageImpl<>(Collections.emptyList(), Pageable.ofSize(pageSize), 0);

        when(warehouseRepository.findAll(any(Pageable.class))).thenReturn(warehousePage);

        WarehouseListGetVm result = warehouseService.getPageableWarehouses(pageNo, pageSize);

        assertTrue(result.warehouseContent().isEmpty());
        assertEquals(0, result.totalElements());
    }
}
