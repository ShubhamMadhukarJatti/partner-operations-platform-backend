package com.sharkdom.repository.user;

import com.sharkdom.constants.user.ApprovalRequestHistoryStatus;
import com.sharkdom.entity.user.ApprovalRequestHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApprovalRequestHistoryRepository extends JpaRepository<ApprovalRequestHistory, Long> {

    public List<ApprovalRequestHistory> findAllByUserId(String userId);

    public List<ApprovalRequestHistory> findAllByStatus(ApprovalRequestHistoryStatus status);

    public List<ApprovalRequestHistory> findAllByUserIdAndRequestType(String userId, String requestType);

}