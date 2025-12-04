package com.ecommerce.gocgac.service.auth;

import com.ecommerce.gocgac.entity.Role;
import com.ecommerce.gocgac.entity.Permission;
import com.ecommerce.gocgac.repository.RoleRepository;
import com.ecommerce.gocgac.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService {
    
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    
    /**
     * Lấy tất cả roles
     */
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }
    
    /**
     * Lấy role theo code
     */
    public Optional<Role> getRoleByCode(String code) {
        return roleRepository.findByCode(code);
    }
    
    /**
     * Tạo role mới
     */
    @Transactional
    public Role createRole(String name, String code, String description) {
        if (roleRepository.existsByCode(code)) {
            throw new RuntimeException("Role với code " + code + " đã tồn tại");
        }
        
        Role role = new Role();
        role.setName(name);
        role.setCode(code);
        role.setDescription(description);
        
        return roleRepository.save(role);
    }
    
    /**
     * Gán permission cho role
     */
    @Transactional
    public Role assignPermissionToRole(Long roleId, Long permissionId) {
        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new RuntimeException("Role không tồn tại"));
        
        Permission permission = permissionRepository.findById(permissionId)
            .orElseThrow(() -> new RuntimeException("Permission không tồn tại"));
        
        if (!role.getPermissions().contains(permission)) {
            role.getPermissions().add(permission);
            roleRepository.save(role);
        }
        
        return role;
    }
    
    /**
     * Lấy tất cả permissions
     */
    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }
    
    /**
     * Tạo permission mới
     */
    @Transactional
    public Permission createPermission(String name, String code, String description, 
                                      String resource, String action) {
        if (permissionRepository.existsByCode(code)) {
            throw new RuntimeException("Permission với code " + code + " đã tồn tại");
        }
        
        Permission permission = new Permission();
        permission.setName(name);
        permission.setCode(code);
        permission.setDescription(description);
        permission.setResource(resource);
        permission.setAction(action);
        
        return permissionRepository.save(permission);
    }
}

