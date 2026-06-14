package com.ecommerce.gocgac.service.system;

import com.ecommerce.gocgac.common.util.JwtUtils;
import com.ecommerce.gocgac.dto.menu.CreateMenuItemRequest;
import com.ecommerce.gocgac.dto.menu.MenuItemResponse;
import com.ecommerce.gocgac.dto.menu.UpdateMenuItemRequest;
import com.ecommerce.gocgac.entity.MenuItem;
import com.ecommerce.gocgac.entity.enums.MenuType;
import com.ecommerce.gocgac.exception.ResourceNotFoundException;
import com.ecommerce.gocgac.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Quản lý menu động (Sprint 13 - M20).
 *
 * <p>Menu được lọc theo quyền của người dùng: một mục có {@code permissionCode}
 * chỉ hiển thị nếu người dùng có vai trò/quyền tương ứng (lấy từ JWT).
 * Kết quả trả về dạng cây (parent → children).
 */
@Service
@RequiredArgsConstructor
public class MenuItemService {

    private final MenuItemRepository menuItemRepository;

    // ===== Admin CRUD =====

    @Transactional
    public MenuItemResponse create(CreateMenuItemRequest req) {
        MenuItem m = new MenuItem();
        m.setParentId(req.getParentId());
        m.setMenuType(req.getMenuType());
        m.setMenuName(req.getMenuName());
        m.setMenuUrl(req.getMenuUrl());
        m.setIcon(req.getIcon());
        m.setDisplayOrder(req.getDisplayOrder() != null ? req.getDisplayOrder() : 0);
        m.setPermissionCode(req.getPermissionCode());
        m.setIsActive(true);
        return MenuItemResponse.from(menuItemRepository.save(m));
    }

    @Transactional
    public MenuItemResponse update(Long id, UpdateMenuItemRequest req) {
        MenuItem m = getOrThrow(id);
        if (req.getParentId() != null) m.setParentId(req.getParentId());
        if (req.getMenuName() != null) m.setMenuName(req.getMenuName());
        if (req.getMenuUrl() != null) m.setMenuUrl(req.getMenuUrl());
        if (req.getIcon() != null) m.setIcon(req.getIcon());
        if (req.getDisplayOrder() != null) m.setDisplayOrder(req.getDisplayOrder());
        if (req.getPermissionCode() != null) m.setPermissionCode(req.getPermissionCode());
        if (req.getIsActive() != null) m.setIsActive(req.getIsActive());
        return MenuItemResponse.from(menuItemRepository.save(m));
    }

    @Transactional
    public void delete(Long id) {
        if (!menuItemRepository.existsById(id)) {
            throw new ResourceNotFoundException("Menu không tồn tại");
        }
        menuItemRepository.deleteById(id);
    }

    /** Toàn bộ menu theo loại (admin, không lọc quyền) — dạng cây. */
    public List<MenuItemResponse> getAllByType(MenuType menuType) {
        return buildTree(menuItemRepository.findByMenuTypeOrderByDisplayOrderAsc(menuType));
    }

    // ===== Menu cho người dùng hiện tại (lọc theo quyền) =====

    public List<MenuItemResponse> getMenuForCurrentUser(MenuType menuType) {
        Set<String> roles = Set.copyOf(JwtUtils.getRoles());
        List<MenuItem> visible = menuItemRepository
            .findByMenuTypeAndIsActiveTrueOrderByDisplayOrderAsc(menuType).stream()
            .filter(m -> isVisible(m, roles))
            .toList();
        return buildTree(visible);
    }

    /** Lọc theo tập mã quyền cho trước (tách riêng để dễ kiểm thử). */
    public List<MenuItemResponse> filterAndBuild(List<MenuItem> items, Set<String> permissionCodes) {
        List<MenuItem> visible = items.stream().filter(m -> isVisible(m, permissionCodes)).toList();
        return buildTree(visible);
    }

    // ===== Helpers =====

    private boolean isVisible(MenuItem item, Set<String> permissionCodes) {
        return !StringUtils.hasText(item.getPermissionCode())
            || permissionCodes.contains(item.getPermissionCode());
    }

    /** Dựng cây từ danh sách phẳng theo parentId. */
    private List<MenuItemResponse> buildTree(List<MenuItem> items) {
        Map<Long, MenuItemResponse> byId = new LinkedHashMap<>();
        for (MenuItem m : items) {
            byId.put(m.getId(), MenuItemResponse.from(m));
        }
        List<MenuItemResponse> roots = new ArrayList<>();
        for (MenuItem m : items) {
            MenuItemResponse node = byId.get(m.getId());
            if (m.getParentId() != null && byId.containsKey(m.getParentId())) {
                byId.get(m.getParentId()).getChildren().add(node);
            } else {
                roots.add(node);
            }
        }
        return roots;
    }

    private MenuItem getOrThrow(Long id) {
        return menuItemRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Menu không tồn tại"));
    }
}
