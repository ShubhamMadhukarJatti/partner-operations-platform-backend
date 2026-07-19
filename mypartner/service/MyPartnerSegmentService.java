package com.sharkdom.mypartner.service;

import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.exception.ResourceNotFoundException;
import com.sharkdom.mypartner.dto.CreateMyPartnerSegmentDTO;
import com.sharkdom.mypartner.dto.PaginatedResponse;
import com.sharkdom.mypartner.entity.MyPartnerSegment;
import com.sharkdom.mypartner.repository.MyPartnerSegmentRepository;
import com.sharkdom.util.Util;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class MyPartnerSegmentService {

    private final MyPartnerSegmentRepository myPartnerSegmentRepository;

    @Transactional
    public List<MyPartnerSegment> createOrUpdateSegments(List<CreateMyPartnerSegmentDTO> dtoList) {
        log.info("createOrUpdateSegments called with {} segments", dtoList.size());

        Long orgId = Util.getOrgIdFromToken();
        List<MyPartnerSegment> savedSegments = new ArrayList<>();

        for (CreateMyPartnerSegmentDTO dto : dtoList) {
            log.info("Processing segmentName={}", dto.getSegmentName());

            MyPartnerSegment segment = myPartnerSegmentRepository
                    .findByOrganizationIdAndSegmentName(orgId, dto.getSegmentName())
                    .map(existing -> {
                        // Update existing
                        log.info("Updating existing segment: {}", existing.getId());
                        existing.setColor(dto.getColor());
                        existing.setMinDeals(dto.getMinDeals());
                        existing.setMaxDeals(dto.getMaxDeals());
                        existing.setActiveCoMarketingCampaign(dto.getActiveCoMarketingCampaign());
                        // existing.setAccess(dto.getAccess());
                        return existing;
                    })
                    .orElseGet(() -> {
                        // Create new
                        log.info("Creating new segment for orgId={} and segmentName={}", orgId, dto.getSegmentName());
                        MyPartnerSegment newSegment = new MyPartnerSegment();
                        newSegment.setOrganizationId(orgId);
                        newSegment.setSegmentName(dto.getSegmentName());
                        newSegment.setColor(dto.getColor());
                        newSegment.setMinDeals(dto.getMinDeals());
                        newSegment.setMaxDeals(dto.getMaxDeals());
                        newSegment.setActiveCoMarketingCampaign(dto.getActiveCoMarketingCampaign());
                        // newSegment.setAccess(dto.getAccess());
                        return newSegment;
                    });

            savedSegments.add(myPartnerSegmentRepository.save(segment));
        }

        return savedSegments;
    }


    public PaginatedResponse<MyPartnerSegment> getSegmentsByOrganizationId(
            int page, int size, String sortBy, String sortDir) {
        log.info("Fetching paginated segments for page={}, size={}", page, size);
        Long orgIdFromToken = Util.getOrgIdFromToken();
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<MyPartnerSegment> segmentPage =
                myPartnerSegmentRepository.findAllByOrganizationId(orgIdFromToken, pageable);
        List<MyPartnerSegment> content = segmentPage.getContent();
        return new PaginatedResponse<>(
                content,
                segmentPage.getNumber(),
                segmentPage.getSize(),
                segmentPage.getTotalElements(),
                segmentPage.getTotalPages(),
                segmentPage.isLast()
        );
    }

    public void deleteSegmentById(Long id) {
        Long orgId = Util.getOrgIdFromToken();
        log.info("Attempting to delete segment id={} for orgId={}", id, orgId);

        MyPartnerSegment segment = myPartnerSegmentRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorMessages.valueOf("Segment not found with id " + id + " for this organization.")));

        myPartnerSegmentRepository.delete(segment);
        log.info("Segment deleted successfully with id={}", id);
    }

    @Transactional
    public MyPartnerSegment updateSegment(Long id, CreateMyPartnerSegmentDTO dto) {
        log.info("Updating segment with id={}", id);

        MyPartnerSegment existing = myPartnerSegmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Segment not found with id=" + id));

        existing.setColor(dto.getColor());
        existing.setSegmentName(dto.getSegmentName());
        existing.setMinDeals(dto.getMinDeals());
        existing.setMaxDeals(dto.getMaxDeals());
        existing.setActiveCoMarketingCampaign(dto.getActiveCoMarketingCampaign());
        // existing.setAccess(dto.getAccess());

        return myPartnerSegmentRepository.save(existing);
    }




}
