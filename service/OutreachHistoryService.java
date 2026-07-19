package com.sharkdom.agenticai.service;

import com.sharkdom.agenticai.entity.OutreachHistory;
import com.sharkdom.agenticai.enums.OutreachChannel;
import com.sharkdom.agenticai.enums.OutreachStatus;
import com.sharkdom.agenticai.model.OutreachTransactionSummaryResponse;
import com.sharkdom.agenticai.repository.OutreachHistoryRepository;
import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutreachHistoryService {

    private final OutreachHistoryRepository outreachHistoryRepository;

    public OutreachHistory upsertOutreachHistory(
            String companyName,
            String recipientName,
            String recipientTitle,
            OutreachChannel channel,
            OutreachStatus status,
            String userId,
            Long orgId
    ) {

        log.info("Upsert outreach history started. companyName={}, channel={}, userId={}, orgId={}",
                companyName, channel, userId, orgId);

        try {

            Optional<OutreachHistory> existingRecord =
                    outreachHistoryRepository
                            .findByCompanyNameAndChannelAndOrgId(
                                    companyName,
                                    channel,
                                    orgId
                            );

            OutreachHistory outreachHistory;

            if (existingRecord.isPresent()) {

                log.info("Existing outreach history found. Updating record. companyName={}, channel={}, userId={}, orgId={}",
                        companyName, channel, userId, orgId);

                outreachHistory = existingRecord.get();

                outreachHistory.setRecipientName(recipientName);
                outreachHistory.setRecipientTitle(recipientTitle);
                outreachHistory.setStatus(status);
                outreachHistory.setSentAt(LocalDateTime.now());

            } else {

                log.info("Creating new outreach history record. companyName={}, channel={}, userId={}, orgId={}",
                        companyName, channel, userId, orgId);

                outreachHistory = new OutreachHistory();

                outreachHistory.setCompanyName(companyName);
                outreachHistory.setRecipientName(recipientName);
                outreachHistory.setRecipientTitle(recipientTitle);
                outreachHistory.setChannel(channel);
                outreachHistory.setStatus(status);
                outreachHistory.setUserId(userId);
                outreachHistory.setOrgId(orgId);
                outreachHistory.setSentAt(LocalDateTime.now());
            }

            OutreachHistory saved =
                    outreachHistoryRepository.save(outreachHistory);

            log.info("Upsert outreach history successful. id={}, companyName={}, channel={}",
                    saved.getId(), saved.getCompanyName(), saved.getChannel());

            return saved;

        } catch (Exception ex) {

            log.error("Error while upserting outreach history. companyName={}, userId={}, orgId={}, error={}",
                    companyName, userId, orgId, ex.getMessage(), ex);

            throw new ServiceException(ErrorMessages.valueOf("Failed to upsert outreach history"));
        }
    }

    public List<OutreachHistory> getOutreachHistoryByOrgId(Long orgId) {

        log.info("Fetching outreach history for orgId={}", orgId);

        if (orgId == null ) {

            log.error("Failed to fetch outreach history. orgId is null or empty");

            throw new ServiceException(ErrorMessages.SH106);
        }

        try {

            List<OutreachHistory> records =
                    outreachHistoryRepository.findByOrgId(orgId);

            log.info("Outreach history fetched successfully. orgId={}, recordCount={}",
                    orgId, records.size());

            return records;

        } catch (Exception ex) {

            log.error("Error fetching outreach history. orgId={}, error={}",
                    orgId, ex.getMessage(), ex);

            throw new ServiceException(ErrorMessages.SH160);
        }
    }

    public OutreachTransactionSummaryResponse getTransactionSummary(Long orgId) {

        log.info("Fetching outreach transaction summary. orgId={}", orgId);

        if (orgId == null ) {

            log.error("Failed to fetch transaction summary. orgId is null or empty");

            throw new ServiceException(ErrorMessages.SH106);
        }

        try {

            long emailCount =
                    outreachHistoryRepository.countByOrgIdAndChannel(
                            orgId,
                            OutreachChannel.EMAIL
                    );

            long linkedinCount =
                    outreachHistoryRepository.countByOrgIdAndChannel(
                            orgId,
                            OutreachChannel.LINKEDIN
                    );

            OutreachTransactionSummaryResponse response =
                    new OutreachTransactionSummaryResponse();

            response.setOrgId(orgId);
            response.setEmailCount(emailCount);
            response.setLinkedinCount(linkedinCount);
            response.setTotalTransactions(emailCount + linkedinCount);

            log.info("Outreach transaction summary fetched. orgId={}, total={}",
                    orgId, response.getTotalTransactions());

            return response;

        } catch (Exception ex) {

            log.error("Error fetching outreach transaction summary. orgId={}, error={}",
                    orgId, ex.getMessage(), ex);

            throw new ServiceException(ErrorMessages.SH160);
        }
    }

    public OutreachHistory saveOutreachHistory(
            String companyName,
            String recipientName,
            String recipientTitle,
            OutreachChannel channel,
            OutreachStatus status,
            String userId,
            Long orgId
    ) {

        log.info("Saving outreach history. companyName={}, channel={}, userId={}, orgId={}",
                companyName, channel, userId, orgId);

        try {

            OutreachHistory outreachHistory = new OutreachHistory();

            outreachHistory.setCompanyName(companyName);
            outreachHistory.setRecipientName(recipientName);
            outreachHistory.setRecipientTitle(recipientTitle);
            outreachHistory.setChannel(channel);
            outreachHistory.setStatus(status);
            outreachHistory.setUserId(userId);
            outreachHistory.setOrgId(orgId);
            outreachHistory.setSentAt(LocalDateTime.now());

            OutreachHistory saved = outreachHistoryRepository.save(outreachHistory);

            log.info("Outreach history saved successfully. id={}", saved.getId());

            return saved;

        } catch (Exception ex) {

            log.error("Error saving outreach history. companyName={}, error={}",
                    companyName, ex.getMessage(), ex);

            throw new ServiceException(ErrorMessages.SH160);
        }
    }
}
