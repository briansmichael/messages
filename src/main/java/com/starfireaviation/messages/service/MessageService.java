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
import com.starfireaviation.messages.model.Message;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class MessageService {

    /**
     * Message queue amp.
     */
    private final Map<String, BlockingQueue<Message>> messageQueueMap;

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
        final String org = message.getOrganization();
        BlockingQueue<Message> messageQueue = null;
        boolean success = false;
        try {
            if (messageQueueMap.containsKey(org)) {
                messageQueue = messageQueueMap.get(org);
            } else {
                messageQueue = new ArrayBlockingQueue<>(CommonConstants.MAX_QUEUE_SIZE);
            }
            success = messageQueue.offer(message);
        } catch (IllegalArgumentException iae) {
            log.error(iae.getMessage());
        } finally {
            messageQueueMap.put(org, messageQueue);
        }
        return success;
    }

    /**
     * Gets a message from the queue, or null if no messages are available.
     *
     * @param organization Organization
     * @return Message
     */
    public Message getMessage(final String organization) {
        if (messageQueueMap.containsKey(organization)) {
            return messageQueueMap.get(organization).poll();
        }
        return null;
    }
}
