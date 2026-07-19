package com.sharkdom.offlinePartner.service;

import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.exception.ServiceException;
import com.sharkdom.offlinePartner.dto.CreateExternalPartnerSignDocCommentRequest;
import com.sharkdom.offlinePartner.dto.UpdateExternalPartnerSignDocCommentRequest;
import com.sharkdom.offlinePartner.entity.ExternalPartnerSignDocComment;
import com.sharkdom.offlinePartner.repository.ExternalPartnerSignDocCommentRepository;
import com.sharkdom.util.Util;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExternalPartnerSignDocCommentService {

    private final ExternalPartnerSignDocCommentRepository
            externalPartnerSignDocCommentRepository;


    public ExternalPartnerSignDocComment createComment(
            CreateExternalPartnerSignDocCommentRequest request
    ) {

        Long orgId = Util.getOrgIdFromToken();

        ExternalPartnerSignDocComment comment =
                new ExternalPartnerSignDocComment();

        comment.setExternalPartnerCode(
                request.getExternalPartnerCode()
        );

        comment.setOrgId(orgId);

        comment.setCommentText(
                request.getCommentText()
        );

        return externalPartnerSignDocCommentRepository.save(comment);
    }


    public ExternalPartnerSignDocComment updateComment(
            Long commentId,
            UpdateExternalPartnerSignDocCommentRequest request
    ) {

        Long orgId = Util.getOrgIdFromToken();

        ExternalPartnerSignDocComment comment =
                externalPartnerSignDocCommentRepository
                        .findByIdAndOrgId(commentId, orgId)
                        .orElseThrow(() ->
                                new ServiceException(
                                        ErrorMessages.SH201,
                                        commentId
                                ));

        comment.setCommentText(
                request.getCommentText()
        );

        return externalPartnerSignDocCommentRepository.save(comment);
    }


    public void deleteComment(Long commentId) {

        Long orgId = Util.getOrgIdFromToken();

        ExternalPartnerSignDocComment comment =
                externalPartnerSignDocCommentRepository
                        .findByIdAndOrgId(commentId, orgId)
                        .orElseThrow(() ->
                                new ServiceException(
                                        ErrorMessages.SH201,
                                        commentId
                                ));

        externalPartnerSignDocCommentRepository.delete(comment);
    }


    public ExternalPartnerSignDocComment getCommentById(
            Long commentId
    ) {

        Long orgId = Util.getOrgIdFromToken();

        return externalPartnerSignDocCommentRepository
                .findByIdAndOrgId(commentId, orgId)
                .orElseThrow(() ->
                        new ServiceException(
                                ErrorMessages.SH201,
                                commentId
                        ));
    }


    public List<ExternalPartnerSignDocComment> getAllComments(
            String externalPartnerCode
    ) {

        Long orgId = Util.getOrgIdFromToken();

        return externalPartnerSignDocCommentRepository
                .findAllByOrgIdAndExternalPartnerCode(
                        orgId,
                        externalPartnerCode
                );
    }
}