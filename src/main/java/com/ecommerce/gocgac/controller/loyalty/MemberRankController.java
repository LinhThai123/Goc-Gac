package com.ecommerce.gocgac.controller.loyalty;

import com.ecommerce.gocgac.common.response.MessageResponse;
import com.ecommerce.gocgac.dto.memberrank.CreateMemberRankRequest;
import com.ecommerce.gocgac.dto.memberrank.UpdateMemberRankRequest;
import com.ecommerce.gocgac.service.loyalty.MemberRankService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller hạng thành viên (M13).
 * - Mọi người dùng: xem danh sách hạng đang hoạt động.
 * - Admin: CRUD hạng thành viên.
 */
@RestController
@RequestMapping("/api/member-ranks")
@RequiredArgsConstructor
@Tag(name = "Member Rank", description = "API hạng thành viên")
@SecurityRequirement(name = "bearerAuth")
public class MemberRankController {

    private final MemberRankService memberRankService;

    private static final String ADMIN_ROLES = "hasAnyRole('ADMIN', 'SUPER_ADMIN')";

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Danh sách hạng thành viên đang hoạt động")
    public ResponseEntity<MessageResponse> listActive() {
        return ResponseEntity.ok(MessageResponse.ok("Lấy danh sách hạng thành công",
            memberRankService.listActive()));
    }

    @GetMapping("/all")
    @PreAuthorize(ADMIN_ROLES)
    @Operation(summary = "Tất cả hạng (gồm ngừng hoạt động)")
    public ResponseEntity<MessageResponse> listAll() {
        return ResponseEntity.ok(MessageResponse.ok("Lấy tất cả hạng thành công",
            memberRankService.listAll()));
    }

    @PostMapping
    @PreAuthorize(ADMIN_ROLES)
    @Operation(summary = "Tạo hạng thành viên")
    public ResponseEntity<MessageResponse> create(@Valid @RequestBody CreateMemberRankRequest request) {
        return ResponseEntity.ok(MessageResponse.created("Tạo hạng thành công",
            memberRankService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize(ADMIN_ROLES)
    @Operation(summary = "Cập nhật hạng thành viên")
    public ResponseEntity<MessageResponse> update(@PathVariable Long id,
            @Valid @RequestBody UpdateMemberRankRequest request) {
        return ResponseEntity.ok(MessageResponse.ok("Cập nhật hạng thành công",
            memberRankService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(ADMIN_ROLES)
    @Operation(summary = "Ngừng hạng thành viên")
    public ResponseEntity<MessageResponse> delete(@PathVariable Long id) {
        memberRankService.delete(id);
        return ResponseEntity.ok(MessageResponse.ok("Đã ngừng hạng thành viên"));
    }
}
