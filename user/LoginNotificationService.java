package com.sharkdom.service.user;

import com.sharkdom.entity.user.UserIpHistory;
import com.sharkdom.repository.user.UserIpHistoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
public class LoginNotificationService {

    private final UserIpHistoryRepository ipHistoryRepository;

    public LoginNotificationService(UserIpHistoryRepository ipHistoryRepository) {
        this.ipHistoryRepository = ipHistoryRepository;
    }


    public String checkAndNotifyNewIpLogin(String currentIp, String userEmail) {
        ipHistoryRepository.findFirstByEmailOrderByLastLoginTimeDesc(userEmail)
                .ifPresentOrElse(
                        lastLogin -> {
                            // Check if the current IP is different from the last login IP
                            if (!lastLogin.getIpAddress().equals(currentIp)) {
                                // Save new IP login history
                                saveIpHistory(userEmail, currentIp);

                                // TODO: Implement email notification for new IP login
//                                 sendLoginNotificationEmail(firebaseUid, currentIp, lastLogin.getIpAddress(), userEmail);
                            }
                        },
                        () -> saveIpHistory(userEmail, currentIp)
                );
        return "ok";
    }

    private void saveIpHistory(String userEmail, String ipAddress) {
        UserIpHistory newHistory = new UserIpHistory();
        newHistory.setEmail(userEmail);
        newHistory.setIpAddress(ipAddress);
        newHistory.setLastLoginTime(LocalDateTime.now());
        ipHistoryRepository.save(newHistory);
    }

}
