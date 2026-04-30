package com.yas.customer.viewmodel.useraddress;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.yas.customer.model.UserAddress;
import com.yas.customer.viewmodel.address.AddressVm;
import org.junit.jupiter.api.Test;

class UserAddressVmTest {

    @Test
    void testFromModel_mapsAllFields() {
        UserAddress userAddress = UserAddress.builder()
            .id(10L)
            .userId("user-1")
            .addressId(99L)
            .isActive(true)
            .build();

        AddressVm addressVm = AddressVm.builder()
            .id(99L)
            .contactName("John Doe")
            .phone("123")
            .addressLine1("123 Street")
            .city("HCM")
            .zipCode("700000")
            .districtId(1L)
            .stateOrProvinceId(2L)
            .countryId(3L)
            .build();

        UserAddressVm vm = UserAddressVm.fromModel(userAddress, addressVm);

        assertNotNull(vm);
        assertEquals(10L, vm.id());
        assertEquals("user-1", vm.userId());
        assertEquals(addressVm, vm.addressGetVm());
        assertEquals(true, vm.isActive());
    }
}
