package com.ecommerce.gocgac.service.loyalty;

import com.ecommerce.gocgac.dto.memberrank.CreateMemberRankRequest;
import com.ecommerce.gocgac.dto.memberrank.MemberRankResponse;
import com.ecommerce.gocgac.dto.memberrank.UpdateMemberRankRequest;
import com.ecommerce.gocgac.entity.MemberRank;
import com.ecommerce.gocgac.exception.ResourceNotFoundException;
import com.ecommerce.gocgac.repository.MemberRankRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Quản lý hạng thành viên (M13) — dành cho admin cấu hình các bậc khách hàng thân thiết.
 */
@Service
@RequiredArgsConstructor
public class MemberRankService {

    private final MemberRankRepository memberRankRepository;

    private static final String ACTIVE = "active";

    @Transactional
    public MemberRankResponse create(CreateMemberRankRequest req) {
        MemberRank r = new MemberRank();
        r.setRankName(req.getRankName());
        r.setMinSpending(req.getMinSpending());
        r.setPeriodMonths(req.getPeriodMonths() != null ? req.getPeriodMonths() : 12);
        r.setDescription(req.getDescription());
        r.setBenefits(req.getBenefits());
        r.setRankOrder(req.getRankOrder());
        r.setStatus(ACTIVE);
        return MemberRankResponse.from(memberRankRepository.save(r));
    }

    @Transactional
    public MemberRankResponse update(Long id, UpdateMemberRankRequest req) {
        MemberRank r = getOrThrow(id);
        if (req.getRankName() != null) r.setRankName(req.getRankName());
        if (req.getMinSpending() != null) r.setMinSpending(req.getMinSpending());
        if (req.getPeriodMonths() != null) r.setPeriodMonths(req.getPeriodMonths());
        if (req.getDescription() != null) r.setDescription(req.getDescription());
        if (req.getBenefits() != null) r.setBenefits(req.getBenefits());
        if (req.getRankOrder() != null) r.setRankOrder(req.getRankOrder());
        if (req.getStatus() != null) r.setStatus(req.getStatus());
        return MemberRankResponse.from(memberRankRepository.save(r));
    }

    @Transactional
    public void delete(Long id) {
        MemberRank r = getOrThrow(id);
        r.setStatus("inactive");
        memberRankRepository.save(r);
    }

    public List<MemberRankResponse> listActive() {
        return memberRankRepository.findByStatusOrderByRankOrderAsc(ACTIVE)
            .stream().map(MemberRankResponse::from).toList();
    }

    public List<MemberRankResponse> listAll() {
        return memberRankRepository.findAll().stream().map(MemberRankResponse::from).toList();
    }

    private MemberRank getOrThrow(Long id) {
        return memberRankRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Hạng thành viên không tồn tại"));
    }
}
