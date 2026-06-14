package com.ecommerce.gocgac.service.system;

import com.ecommerce.gocgac.dto.menu.MenuItemResponse;
import com.ecommerce.gocgac.entity.MenuItem;
import com.ecommerce.gocgac.entity.enums.MenuType;
import com.ecommerce.gocgac.repository.MenuItemRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class MenuItemServiceTest {

    @Mock private MenuItemRepository menuItemRepository;
    @InjectMocks private MenuItemService menuItemService;

    private MenuItem item(Long id, Long parentId, String permissionCode) {
        MenuItem m = new MenuItem();
        m.setId(id);
        m.setParentId(parentId);
        m.setMenuType(MenuType.ADMIN);
        m.setMenuName("Menu " + id);
        m.setPermissionCode(permissionCode);
        m.setDisplayOrder(0);
        m.setIsActive(true);
        return m;
    }

    @Test
    @DisplayName("Lọc menu theo quyền & dựng cây: mục có quyền phù hợp mới hiển thị")
    void filterAndBuild_byPermission() {
        List<MenuItem> items = List.of(
            item(1L, null, null),       // hiển thị cho mọi người
            item(2L, 1L, "ADMIN"),      // con của 1, cần quyền ADMIN
            item(3L, null, "SELLER")    // gốc khác, cần quyền SELLER
        );

        List<MenuItemResponse> tree = menuItemService.filterAndBuild(items, Set.of("ADMIN"));

        // root2 (SELLER) bị ẩn → còn 1 gốc
        assertThat(tree).hasSize(1);
        assertThat(tree.get(0).getId()).isEqualTo(1L);
        // child (ADMIN) hiển thị
        assertThat(tree.get(0).getChildren()).hasSize(1);
        assertThat(tree.get(0).getChildren().get(0).getId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("Không có quyền → ẩn mục yêu cầu quyền")
    void filterAndBuild_noPermission_hidesRestricted() {
        List<MenuItem> items = List.of(
            item(1L, null, null),
            item(2L, 1L, "ADMIN"),
            item(3L, null, "SELLER")
        );

        List<MenuItemResponse> tree = menuItemService.filterAndBuild(items, Set.of());

        assertThat(tree).hasSize(1);
        assertThat(tree.get(0).getId()).isEqualTo(1L);
        assertThat(tree.get(0).getChildren()).isEmpty();
    }
}
