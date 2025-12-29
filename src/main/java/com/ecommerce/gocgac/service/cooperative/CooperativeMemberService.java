package com.ecommerce.gocgac.service.cooperative;

import com.ecommerce.gocgac.common.response.MessageResponse;
import com.ecommerce.gocgac.dto.cooperative.JoinCooperativeRequest;
import com.ecommerce.gocgac.dto.cooperative.MemberResponse;
import com.ecommerce.gocgac.entity.Cooperative;
import com.ecommerce.gocgac.entity.CooperativeMember;
import com.ecommerce.gocgac.entity.User;
import com.ecommerce.gocgac.entity.enums.ApprovalStatus;
import com.ecommerce.gocgac.entity.enums.CooperativeMemberRole;
import com.ecommerce.gocgac.entity.enums.UserType;
import com.ecommerce.gocgac.exception.CooperativeException;
import com.ecommerce.gocgac.external.KeycloakClient;
import com.ecommerce.gocgac.repository.CooperativeMemberRepository;
import com.ecommerce.gocgac.repository.CooperativeRepository;
import com.ecommerce.gocgac.repository.StoreRepository;
import com.ecommerce.gocgac.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CooperativeMemberService {
    
    private final CooperativeMemberRepository memberRepository;
    private final CooperativeRepository cooperativeRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final KeycloakClient keycloakClient;
    
    /**
     * User (CUSTOMER) đăng ký thành viên HTX
     * Sau khi approve, user trở thành thành viên HTX
     * Nếu muốn bán hàng, user cần đăng ký Seller riêng
     */
    @Transactional
    public MessageResponse joinCooperative(Long userId, JoinCooperativeRequest request) {
        // Validate user tồn tại
        if (!userRepository.existsById(userId)) {
            throw new CooperativeException("User không tồn tại");
        }
        
        // Không yêu cầu userType = SELLER, CUSTOMER cũng có thể đăng ký thành viên HTX
        
        // Validate cooperative tồn tại
        if (!cooperativeRepository.existsById(request.getCooperativeId())) {
            throw new CooperativeException("HTX không tồn tại");
        }
        
        // Kiểm tra user đã đăng ký vào HTX này chưa
        if (memberRepository.existsByCooperativeIdAndUserId(request.getCooperativeId(), userId)) {
            throw new CooperativeException("Bạn đã đăng ký vào HTX này rồi");
        }
        
        // Tạo đơn đăng ký
        CooperativeMember member = new CooperativeMember();
        member.setCooperativeId(request.getCooperativeId());
        member.setUserId(userId);
        member.setRole(request.getRole() != null ? request.getRole() : CooperativeMemberRole.MEMBER);
        member.setStatus(ApprovalStatus.PENDING);
        member.setAppliedAt(LocalDateTime.now());
        
        member = memberRepository.save(member);
        
        log.info("User {} đăng ký thành viên HTX {}", userId, request.getCooperativeId());
        
        MessageResponse response = new MessageResponse();
        response.setMessage("Đơn đăng ký thành viên HTX đã được gửi. Vui lòng chờ HTX phê duyệt.");
        response.setStatus(HttpStatus.OK.value());
        response.setData(member);
        
        return response;
    }
    
    /**
     * HTX Manager approve đơn đăng ký thành viên
     * KHÔNG tạo Store - Store chỉ được tạo khi user đăng ký Seller
     */
    @Transactional
    public MessageResponse approveMember(Long memberId, Long cooperativeManagerId) {
        CooperativeMember member = memberRepository.findById(memberId)
            .orElseThrow(() -> new CooperativeException("Đơn đăng ký không tồn tại"));
        
        if (member.getStatus() != ApprovalStatus.PENDING) {
            throw new CooperativeException("Chỉ có thể approve đơn đăng ký ở trạng thái PENDING");
        }
        
        // Validate cooperative manager
        Cooperative cooperative = cooperativeRepository.findById(member.getCooperativeId())
            .orElseThrow(() -> new CooperativeException("HTX không tồn tại"));
        
        if (!cooperative.getUserId().equals(cooperativeManagerId)) {
            throw new CooperativeException("Bạn không có quyền phê duyệt đơn đăng ký này");
        }
        
        // Validate user tồn tại
        User user = userRepository.findById(member.getUserId())
            .orElseThrow(() -> new CooperativeException("User không tồn tại"));
        
        // Update member status (KHÔNG tạo Store ở đây)
        member.setStatus(ApprovalStatus.APPROVED);
        member.setJoinedAt(LocalDateTime.now());
        member.setReviewedAt(LocalDateTime.now());
        member.setReviewedBy(cooperativeManagerId);
        memberRepository.save(member);
        
        // Update User.userType từ CUSTOMER lên MEMBER
        if (user.getUserType() == UserType.CUSTOMER) {
            user.setUserType(UserType.MEMBER);
            userRepository.save(user);
            log.info("User {} userType đã được cập nhật từ CUSTOMER lên MEMBER", user.getId());
        }
        
        // Gán role "MEMBER" trong Keycloak
        try {
            if (user.getKeycloakId() != null) {
                keycloakClient.assignRoleToUser(user.getKeycloakId(), "MEMBER");
                log.info("Đã gán role MEMBER cho user {} trong Keycloak", user.getKeycloakId());
            } else {
                log.warn("User {} không có keycloakId, không thể gán role trong Keycloak", user.getId());
            }
        } catch (Exception e) {
            log.error("Không thể gán role MEMBER trong Keycloak cho user {}: {}", 
                user.getKeycloakId(), e.getMessage(), e);
            // Không throw exception để không rollback transaction
            // Có thể retry sau hoặc admin assign manually
        }
        
        log.info("Đơn đăng ký thành viên {} được approve. User {} trở thành thành viên HTX {}", 
            memberId, member.getUserId(), member.getCooperativeId());
        
        MessageResponse response = new MessageResponse();
        response.setMessage("Đơn đăng ký thành viên đã được phê duyệt. User đã trở thành thành viên HTX.");
        response.setStatus(HttpStatus.OK.value());
        response.setData(member);
        
        return response;
    }
    
    /**
     * HTX Manager reject đơn đăng ký thành viên
     */
    @Transactional
    public MessageResponse rejectMember(Long memberId, Long cooperativeManagerId, String reason) {
        CooperativeMember member = memberRepository.findById(memberId)
            .orElseThrow(() -> new CooperativeException("Đơn đăng ký không tồn tại"));
        
        if (member.getStatus() != ApprovalStatus.PENDING) {
            throw new CooperativeException("Chỉ có thể reject đơn đăng ký ở trạng thái PENDING");
        }
        
        // Validate cooperative manager
        Cooperative cooperative = cooperativeRepository.findById(member.getCooperativeId())
            .orElseThrow(() -> new CooperativeException("HTX không tồn tại"));
        
        if (!cooperative.getUserId().equals(cooperativeManagerId)) {
            throw new CooperativeException("Bạn không có quyền từ chối đơn đăng ký này");
        }
        
        if (reason == null || reason.trim().isEmpty()) {
            throw new CooperativeException("Lý do từ chối không được để trống");
        }
        
        // Update member status
        member.setStatus(ApprovalStatus.REJECTED);
        member.setRejectionReason(reason);
        member.setReviewedAt(LocalDateTime.now());
        member.setReviewedBy(cooperativeManagerId);
        memberRepository.save(member);
        
        log.info("Đơn đăng ký thành viên {} bị reject. Lý do: {}", memberId, reason);
        
        MessageResponse response = new MessageResponse();
        response.setMessage("Đơn đăng ký thành viên đã bị từ chối");
        response.setStatus(HttpStatus.OK.value());
        response.setData(member);
        
        return response;
    }
    
    /**
     * Lấy danh sách members của HTX (cho HTX Manager)
     */
    public Page<MemberResponse> getMembers(Long cooperativeId, ApprovalStatus status, Pageable pageable) {
        Page<CooperativeMember> members;
        
        if (status != null) {
            members = memberRepository.findByCooperativeIdAndStatus(cooperativeId, status, pageable);
        } else {
            // Nếu không có status filter, lấy tất cả
            members = memberRepository.findAll(pageable);
        }
        
        return members.map(this::mapToMemberResponse);
    }
    
    /**
     * Lấy danh sách HTX mà User đã tham gia (APPROVED)
     */
    public List<MemberResponse> getMyCooperatives(Long userId) {
        List<CooperativeMember> members = memberRepository.findByUserIdAndStatus(
            userId, 
            ApprovalStatus.APPROVED
        );
        
        return members.stream()
            .map(this::mapToMemberResponse)
            .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Lấy đơn đăng ký của User (PENDING)
     */
    public List<MemberResponse> getMyPendingApplications(Long userId) {
        List<CooperativeMember> members = memberRepository.findByUserIdAndStatus(
            userId, 
            ApprovalStatus.PENDING
        );
        
        return members.stream()
            .map(this::mapToMemberResponse)
            .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * HTX Manager promote member thành seller (nhân viên bán hàng)
     * Update role từ MEMBER → SELLER và update User.userType → SELLER
     */
    @Transactional
    public MessageResponse promoteToSeller(Long memberId, Long cooperativeManagerId) {
        CooperativeMember member = memberRepository.findById(memberId)
            .orElseThrow(() -> new CooperativeException("Thành viên không tồn tại"));
        
        // Validate member phải ở trạng thái APPROVED
        if (member.getStatus() != ApprovalStatus.APPROVED) {
            throw new CooperativeException("Chỉ có thể promote thành viên đã được approve");
        }
        
        // Validate cooperative manager
        Cooperative cooperative = cooperativeRepository.findById(member.getCooperativeId())
            .orElseThrow(() -> new CooperativeException("HTX không tồn tại"));
        
        if (!cooperative.getUserId().equals(cooperativeManagerId)) {
            throw new CooperativeException("Bạn không có quyền thực hiện thao tác này");
        }
        
        // Validate HTX đã có Store
        if (storeRepository.findByCooperativeId(member.getCooperativeId()).isEmpty()) {
            throw new CooperativeException("HTX chưa có Store. Vui lòng đợi HTX được duyệt và tạo Store trước.");
        }
        
        // Validate user tồn tại
        User user = userRepository.findById(member.getUserId())
            .orElseThrow(() -> new CooperativeException("User không tồn tại"));
        
        // Update role từ MEMBER → SELLER
        if (member.getRole() == CooperativeMemberRole.MEMBER) {
            member.setRole(CooperativeMemberRole.SELLER);
            memberRepository.save(member);
            log.info("Member {} role đã được cập nhật từ MEMBER lên SELLER", memberId);
        } else if (member.getRole() == CooperativeMemberRole.SELLER) {
            throw new CooperativeException("Thành viên đã là nhân viên bán hàng (SELLER)");
        }
        
        // Update User.userType từ MEMBER → SELLER
        if (user.getUserType() == UserType.MEMBER || user.getUserType() == UserType.CUSTOMER) {
            user.setUserType(UserType.SELLER);
            userRepository.save(user);
            log.info("User {} userType đã được cập nhật lên SELLER", user.getId());
        }
        
        // Gán role "SELLER" trong Keycloak
        try {
            if (user.getKeycloakId() != null) {
                keycloakClient.assignRoleToUser(user.getKeycloakId(), "SELLER");
                log.info("Đã gán role SELLER cho user {} trong Keycloak", user.getKeycloakId());
            } else {
                log.warn("User {} không có keycloakId, không thể gán role trong Keycloak", user.getId());
            }
        } catch (Exception e) {
            log.error("Không thể gán role SELLER trong Keycloak cho user {}: {}", 
                user.getKeycloakId(), e.getMessage(), e);
            // Không throw exception để không rollback transaction
        }
        
        log.info("Member {} đã được promote thành seller (SELLER) của HTX {}", 
            memberId, member.getCooperativeId());
        
        MessageResponse response = new MessageResponse();
        response.setMessage("Thành viên đã được thăng cấp thành nhân viên bán hàng (seller).");
        response.setStatus(HttpStatus.OK.value());
        response.setData(member);
        
        return response;
    }
    
    /**
     * Map CooperativeMember to MemberResponse
     */
    private MemberResponse mapToMemberResponse(CooperativeMember member) {
        MemberResponse response = new MemberResponse();
        response.setId(member.getId());
        response.setCooperativeId(member.getCooperativeId());
        response.setUserId(member.getUserId());
        response.setRole(member.getRole());
        response.setStatus(member.getStatus());
        response.setRejectionReason(member.getRejectionReason());
        response.setAppliedAt(member.getAppliedAt());
        response.setJoinedAt(member.getJoinedAt());
        response.setReviewedAt(member.getReviewedAt());
        
        // Load cooperative name
        Optional<Cooperative> cooperative = cooperativeRepository.findById(member.getCooperativeId());
        if (cooperative.isPresent()) {
            response.setCooperativeName(cooperative.get().getCooperativeName());
        }
        
        // Load user info
        Optional<User> user = userRepository.findById(member.getUserId());
        if (user.isPresent()) {
            response.setUserName(user.get().getFullName());
            response.setUserEmail(user.get().getEmail());
        }
        
        return response;
    }
}

