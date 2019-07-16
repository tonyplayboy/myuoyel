package com.leyou.order.controller;

import com.leyou.order.pojo.Address;
import com.leyou.order.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * @Author: 98050
 * @Time: 2018-10-31 09:44
 * @Feature: 地址CRUD
 */
@RestController
@RequestMapping("address")
public class AddressController {

    @Autowired
    private AddressService addressService;

    /**
     * 创建收货地址
     * @return
     */
    @PostMapping
    public ResponseEntity<Void> addAddressByUserId(@RequestBody @Valid Address address){
        System.out.println(address.getDefaultAddress());
        this.addressService.addAddressByUserId(address);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 根据用户id查询地址列表
     * @return
     */
    @GetMapping
    public ResponseEntity<List<Address>> queryAddressByUserId(){
        List<Address> addresses = this.addressService.queryAddressByUserId();
        if (addresses == null || addresses.size() == 0){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(addresses);
    }

    /**
     * 修改收货地址
     * @param address
     * @return
     */
    @PutMapping
    public ResponseEntity<Void> updateAddressByUserId(@RequestBody Address address){
        this.addressService.updateAddressByUserId(address);
        System.out.println("controller");
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 删除收货地址
     * @param addressId
     * @return
     */
    @DeleteMapping("{addressId}")
    public ResponseEntity<Void> deleteAddress(@PathVariable("addressId") Long addressId){
        this.addressService.deleteAddress(addressId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("{addressId}")
    public ResponseEntity<Address> queryAddressById(@PathVariable("addressId") Long addressId){
        Address address = this.addressService.queryAddressById(addressId);
        if (address == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(address);
    }
}
