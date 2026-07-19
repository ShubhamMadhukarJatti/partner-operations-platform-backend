package com.sharkdom.partnerattribution.service;


import com.sharkdom.partnerattribution.dto.NotificationRequestDTO;

public interface NotificationService {

    void processNotification(NotificationRequestDTO request);

}