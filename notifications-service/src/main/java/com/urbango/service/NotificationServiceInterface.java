package com.urbango.service;

import com.urbango.dto.GroupNotificationRequestDto;
import com.urbango.dto.NotificationRequestDto;

public interface NotificationServiceInterface {
    String sendNotification(NotificationRequestDto notificationRequestDto);
    String sendToGroup(GroupNotificationRequestDto request);
}
