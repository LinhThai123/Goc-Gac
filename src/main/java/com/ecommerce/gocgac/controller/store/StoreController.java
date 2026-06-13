package com.ecommerce.gocgac.controller.store;

import com.ecommerce.gocgac.common.response.MessageResponse;
import com.ecommerce.gocgac.common.service.CurrentUserService;
import com.ecommerce.gocgac.dto.store.CreateStoreRequest;
import com.ecommerce.gocgac.dto.store.UpdateStoreRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    private final CurrentUserService currentUserService;

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

    @PostMapping
    @PreAuthorize("hasAnyRole('SELLER', 'SUPER_ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Tạo gian hàng", description = "Người bán cá thể tạo gian hàng mới (mỗi seller 1 gian hàng)")
    public ResponseEntity<MessageResponse> createStore(@Valid @RequestBody CreateStoreRequest request) {
        Long sellerId = currentUserService.getCurrentUserId();
        MessageResponse response = MessageResponse.created("Tạo gian hàng thành công",
            storeService.createStore(sellerId, request));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SELLER', 'SUPER_ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Cập nhật gian hàng", description = "Người bán cập nhật gian hàng của mình")
    public ResponseEntity<MessageResponse> updateStore(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStoreRequest request) {
        Long sellerId = currentUserService.getCurrentUserId();
        MessageResponse response = MessageResponse.ok("Cập nhật gian hàng thành công",
            storeService.updateStore(sellerId, id, request));
        return ResponseEntity.ok(response);
    }
}
