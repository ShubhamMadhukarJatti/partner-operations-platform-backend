package com.sharkdom.service.user;

import com.sharkdom.constants.user.ApprovalRequestHistoryStatus;
import com.sharkdom.entity.user.ApprovalRequestHistory;
import com.sharkdom.entity.user.User;
import com.sharkdom.repository.user.ApprovalRequestHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ApprovalRequestHistoryService {

    @Autowired
    ApprovalRequestHistoryRepository approvalRequestHistoryRepository;
    @Autowired
    UserService userService;

    public List<ApprovalRequestHistory> findAllByUserId(String userId) {
        return approvalRequestHistoryRepository.findAllByUserId(userId);
    }

    public List<ApprovalRequestHistory> findAllByUserIdAndRequestType(String userId, String requestType) {
        return approvalRequestHistoryRepository.findAllByUserIdAndRequestType(userId, requestType);
    }

    public List<ApprovalRequestHistory> findAllByStatus(ApprovalRequestHistoryStatus status) {
        return approvalRequestHistoryRepository.findAllByStatus(status);
    }

    @Transactional
    public ApprovalRequestHistory createOrUpdate(ApprovalRequestHistory approvalRequestHistory) {
        User u = userService.findByUserId(approvalRequestHistory.getUserId()).getBody();
        if (approvalRequestHistory.getRequestType().equals("ENABLE_COLLAB")) {
            if (approvalRequestHistory.getStatus() == ApprovalRequestHistoryStatus.APPROVED) {
                u.setCanCollaborate(true);
            } else {
                u.setCanCollaborate(false);
            }
        }
        userService.create(u);
        return approvalRequestHistoryRepository.save(approvalRequestHistory);
    }
}
