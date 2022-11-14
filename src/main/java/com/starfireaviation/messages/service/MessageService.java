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

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.flakeidgen.FlakeIdGenerator;
import com.hazelcast.map.IMap;
import com.starfireaviation.messages.config.CommonConstants;
import com.starfireaviation.common.model.Message;
import com.starfireaviation.common.model.NotificationType;
import com.starfireaviation.common.model.Priority;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;


@Slf4j
public class MessageService {

    /**
     * Message map.
     * Note: Key = organization; Value = List of Messages
     */
    private final IMap<String, List<Message>> map;

    /**
     * Seen ID map.
     * Note: Outer Key = organization; Inner Key = clientIPAddress; Value(s) = Message ID(s)
     */
    private final IMap<String, Map<String, List<Long>>> seenMap;

    /**
     * ID generator.
     */
    private final FlakeIdGenerator flakeIdGenerator;

    /**
     * MessageService.
     *
     * @param hazelcastInstance HazelcastInstance
     */
    public MessageService(final HazelcastInstance hazelcastInstance) {
        map = hazelcastInstance.getMap("messages");
        seenMap = hazelcastInstance.getMap("seen");
        flakeIdGenerator = hazelcastInstance.getFlakeIdGenerator("messageIds");
    }

    /**
     * Adds a message to the queue.
     *
     * @param message Message
     * @param organization Organization
     * @param correlationId CorrelationID
     * @return message add success
     */
    public boolean addMessage(final Message message, final String organization, final String correlationId) {
        ensureIDIsSet(message);
        ensureExpirationTimeIsSet(message);
        ensurePriorityIsSet(message);
        ensureNotificationTypeIsSet(message);
        log.info("Adding message: {} with ID: {} to organization: {}; correlationId: {}", 
                 message, message.getId(), organization, correlationId);
        List<Message> messages = map.get(organization);
        if (messages == null) {
            messages = new ArrayList<>();
        }
        final boolean success = messages.add(message);
        map.put(organization, messages);
        log.info("Returning success={}", success);
        return success;
    }

    /**
     * Gets a message from the queue, or null if no messages are available.
     *
     * @param organization Organization
     * @param notificationType NotificationType
     * @param clientId ClientID
     * @param correlationId CorrelationID
     * @return Message
     */
    public Message getMessage(final String organization,
                              final NotificationType notificationType,
                              final String clientId,
                              final String correlationId) {
        log.info("Getting message for organization: {}; notificationType: {}; clientId: {}; correlationId: {}",
                organization, notificationType, clientId, correlationId);
        List<Message> messages = map.get(organization);
        if (messages == null) {
            return null;
        }

        // Apply filters
        log.info("Message count before filters: {}", messages.size());
        messages = filterExpired(messages);
        messages = filterNotificationType(messages, notificationType);
        messages = filterSeen(messages, organization, clientId);
        log.info("Message count after filters: {}", messages.size());

        // Get message in priority order
        Message message = getMessageByPriority(messages, Priority.HIGH);
        if (message == null) {
            message = getMessageByPriority(messages, Priority.NORMAL);
        }
        if (message == null) {
            message = getMessageByPriority(messages, Priority.LOW);
        }

        // Cleanup
        if (message != null && message.getNotificationType() != NotificationType.ALL) {
            log.info("Removing and returning message with ID: {} to caller: {}", message.getId(), message);
            map.get(organization).remove(message);
        } else if (message != null && message.getNotificationType() == NotificationType.ALL) {
            log.info("Returning message with ID: {} to caller: {}", message.getId(), message);
            markMessageAsSeenForCaller(clientId, organization, message);
        } else if (message == null) {
            log.info("Returning null");
        }
        return message;
    }

    /**
     * Retrieves first message in the list with a matching priority.
     *
     * @param messages to be searched
     * @param priority search criteria
     * @return matching message
     */
    private Message getMessageByPriority(final List<Message> messages, final Priority priority) {
        return messages.stream().filter(message -> message.getPriority() == priority).findFirst().orElse(null);
    }

    /**
     * Denotes a message as having been seen by a caller.
     *
     * This is to allow for message intended to be delivered to multiple recipients to be delivered to more than
     * one receiver, without being seen more than once by any given caller.
     *
     * @param clientId ClientID
     * @param org organization
     * @param message Message
     */
    private void markMessageAsSeenForCaller(final String clientId, final String org, final Message message) {
        final List<Long> seenList = new ArrayList<>();
        seenList.add(message.getId());
        Map<String, List<Long>> ipMap = seenMap.get(org);
        if (ipMap == null) {
            ipMap = new HashMap<>();
            ipMap.put(clientId, seenList);
        } else {
            final List<Long> priorSeenList = ipMap.get(clientId);
            if (priorSeenList != null) {
                seenList.addAll(priorSeenList);
            }
            ipMap.put(clientId, seenList);
        }
        seenMap.put(org, ipMap);
    }

    /**
     * Filter Expired.
     *
     * @param messages to be filtered
     * @return filtered messages
     */
    private List<Message> filterExpired(final List<Message> messages) {
        return messages.stream()
                .filter(message -> message.getExpirationTime().isAfter(Instant.now()))
                .collect(Collectors.toList());
    }

    /**
     * Filter NotificationType.
     *
     * @param messages to be filtered
     * @param notificationType user input
     * @return filtered messages
     */
    private List<Message> filterNotificationType(final List<Message> messages,
                                                 final NotificationType notificationType) {
        return messages
                .stream()
                .filter(message -> message.getNotificationType() == notificationType
                        || message.getNotificationType() == NotificationType.ALL)
                .collect(Collectors.toList());
    }

    /**
     * Filter already seen messages.
     *
     * @param messages to be filtered
     * @param organization filter criteria
     * @param clientId filter criteria
     * @return filtered messages
     */
    private List<Message> filterSeen(final List<Message> messages,
                                     final String organization,
                                     final String clientId) {
        List<Message> messageList = messages;
        final Map<String, List<Long>> ipMap = seenMap.get(organization);
        if (ipMap != null) {
            final List<Long> seenList = ipMap.get(clientId);
            if (seenList != null) {
                messageList = messageList.stream()
                        .filter(message -> !seenList.contains(message.getId()))
                        .collect(Collectors.toList());
            }
        }
        return messageList;
    }

    /**
     * Ensures the message ID is set before being stored.
     *
     * @param message to be modified
     */
    private void ensureIDIsSet(final Message message) {
        message.setId(flakeIdGenerator.newId());
    }

    /**
     * Ensures NotificationType attribute is set.
     *
     * @param message Message
     */
    private void ensureNotificationTypeIsSet(final Message message) {
        if (message.getNotificationType() == null) {
            message.setNotificationType(NotificationType.ALL);
        }
    }

    /**
     * Ensures Priority attribute is set.
     *
     * @param message to be modified
     */
    private void ensurePriorityIsSet(final Message message) {
        if (message.getPriority() == null) {
            message.setPriority(Priority.NORMAL);
        }
    }

    /**
     * Ensures expirationTime attribute is set.
     *
     * @param message to be modified
     */
    private static void ensureExpirationTimeIsSet(final Message message) {
        if (message.getExpirationTime() == null) {
            message.setExpirationTime(Instant.now().plus(CommonConstants.MESSAGE_EXPIRATION_TIME, ChronoUnit.MINUTES));
        }
    }

    /**
     * Performs message cleanup.
     */
    @Scheduled(fixedDelay = CommonConstants.CLEANUP_DELAY)
    private void cleanup() {
        log.info("Performing cleanup");
        map.keySet()
                .forEach(key -> {
                    final List<Message> messages = map.get(key);
                    messages.forEach(message -> {
                        boolean expired = message.getExpirationTime().isBefore(Instant.now());
                        if (expired) {
                            messages.remove(message);
                            final Map<String, List<Long>> ipMap = seenMap.get(key);
                            if (ipMap != null) {
                                ipMap.keySet().forEach(ipKey -> {
                                    final List<Long> seenList = ipMap.get(ipKey);
                                    seenList.remove(message.getId());
                                });
                            }
                        }
                    });
                });
        new ArrayList<>(map.keySet()).forEach(key -> {
            if (map.get(key).isEmpty()) {
                map.remove(key);
            }
        });
    }

}
