/*
 *  Copyright (C) 2022 Starfire Aviation, LLC
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.starfireaviation.messages.service;

import com.starfireaviation.messages.config.CommonConstants;
import com.starfireaviation.model.Message;
import com.starfireaviation.model.NotificationType;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

@Slf4j
public class MessageService {

    /**
     * Message queue amp.
     */
    private final Map<String, PriorityBlockingQueue<Message>> messageQueueMap;

    /**
     * MessageService.
     */
    public MessageService() {
        messageQueueMap = new ConcurrentHashMap<>();
    }

    /**
     * Adds a message to the queue.
     *
     * @param message Message
     * @return message add success
     */
    public boolean addMessage(final Message message) {
        PriorityBlockingQueue<Message> messageQueue = null;
        boolean success = false;
        final String key = getKey(message.getOrganization(), message.getNotificationType());
        try {
            if (messageQueueMap.containsKey(key)) {
                messageQueue = messageQueueMap.get(key);
            } else {
                messageQueue = new PriorityBlockingQueue<>(CommonConstants.MAX_QUEUE_SIZE);
            }
            success = messageQueue.offer(message);
        } catch (IllegalArgumentException iae) {
            log.error(iae.getMessage());
        } finally {
            messageQueueMap.put(key, messageQueue);
        }
        return success;
    }

    /**
     * Gets a message from the queue, or null if no messages are available.
     *
     * @param organization Organization
     * @param notificationType NotificationType
     * @return Message
     */
    public Message getMessage(final String organization, final NotificationType notificationType) {
        final String key = getKey(organization, notificationType);
        final String fallbackKey = getKey(organization, NotificationType.ALL);
        Message message = null;
        if (messageQueueMap.containsKey(key)) {
            message = messageQueueMap.get(key).poll();
        }
        if (message == null && messageQueueMap.containsKey(fallbackKey)) {
            message = messageQueueMap.get(fallbackKey).poll();
        }
        return message;
    }

    /**
     * Derives the messageQueueMap key from message attributes.
     *
     * @param organization Organization
     * @param notificationType NotificationType
     * @return messageQueueMap key
     */
    private String getKey(final String organization, final NotificationType notificationType) {
        String org = organization;
        if (org == null) {
            org = CommonConstants.DEFAULT_ORGANIZATION;
        }
        NotificationType type = notificationType;
        if (type == null) {
            type = NotificationType.ALL;
        }
        return org + "-" + type;
    }

}
