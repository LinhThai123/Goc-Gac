package com.ecommerce.gocgac.controller.store;

import com.ecommerce.gocgac.common.response.MessageResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import com.ecommerce.gocgac.service.store.StoreService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;


@RestController
@RequestMapping("/api/store")
@RequiredArgsConstructor
@Tag(name = "Store", description = "API quản lý Store")
public class StoreController {
    private final StoreService storeService;

    @GetMapping("")
    @Operation(summary = "Lấy danh sách Store", description = "Lấy danh sách Store")
    public ResponseEntity<MessageResponse> getStores() {
        MessageResponse response = new MessageResponse();
        response.setMessage("Lấy danh sách Store thành công");
        response.setStatus(HttpStatus.OK.value());
        response.setData(storeService.getStores());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy Store theo ID", description = "Lấy Store theo ID")
    public ResponseEntity<MessageResponse> getStoreById(@PathVariable Long id) {
        MessageResponse response = new MessageResponse();
        response.setMessage("Lấy gian hàng thành công");
        response.setStatus(HttpStatus.OK.value());
        response.setData(storeService.getStoreById(id));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/cooperative/{cooperativeId}")
    @Operation(summary = "Lấy Store theo ID hợp tác xã", description = "Lấy Store theo ID hợp tác xã")
    public ResponseEntity<MessageResponse> getStoreByCooperativeId(@PathVariable Long cooperativeId) {
        MessageResponse response = new MessageResponse();
        response.setMessage("Lấy gian hàng thành công");
        response.setStatus(HttpStatus.OK.value());
        response.setData(storeService.getStoreByCooperativeId(cooperativeId));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/seller/{sellerId}")
    @Operation(summary = "Lấy Store theo ID người bán", description = "Lấy Store theo ID người bán")
    public ResponseEntity<MessageResponse> getStoreBySellerId(@PathVariable Long sellerId) {
        MessageResponse response = new MessageResponse();
        response.setMessage("Lấy gian hàng thành công");
        response.setStatus(HttpStatus.OK.value());
        response.setData(storeService.getStoreBySellerId(sellerId));
        return ResponseEntity.ok(response);
    }
}
