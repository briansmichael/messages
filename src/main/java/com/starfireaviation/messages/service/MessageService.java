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
import com.starfireaviation.messages.config.CommonConstants;
import com.starfireaviation.model.Message;
import com.starfireaviation.model.NotificationType;
import com.starfireaviation.model.Priority;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class MessageService {

    /**
     * Message map.
     * Note: Key = organization; Value = List of Messages
     */
    private final Map<String, List<Message>> map;

    /**
     * Seen ID map.
     * Note: Outer Key = organization; Inner Key = clientIPAddress; Value(s) = Message ID(s)
     */
    private final Map<String, Map<String, List<Long>>> seenMap;

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
     * @return message add success
     */
    public boolean addMessage(final Message message) {
        final String org = getOrganization(message);
        ensureIDIsSet(message);
        ensureExpirationTimeIsSet(message);
        ensurePriorityIsSet(message);
        ensureNotificationTypeIsSet(message);
        log.info("Adding message: {} with ID: {} to org: {}", message, message.getId(), org);
        List<Message> messages = map.get(org);
        if (messages == null) {
            messages = new ArrayList<>();
        }
        map.put(org, messages);
        return messages.add(message);
    }

    /**
     * Gets a message from the queue, or null if no messages are available.
     *
     * @param organization Organization
     * @param notificationType NotificationType
     * @param callerIPAddress Caller's IP Address
     * @return Message
     */
    public Message getMessage(final String organization,
                              final NotificationType notificationType,
                              final String callerIPAddress) {
        log.info("Getting message for organization: {}; notificationType: {}; callerIPAddress: {}",
                organization, notificationType, callerIPAddress);
        final String org = getOrganization(organization);
        List<Message> messages = map.get(org);
        if (messages == null) {
            return null;
        }

        // Apply filters
        messages = filterExpired(messages);
        messages = filterNotificationType(messages, notificationType);
        messages = filterSeen(messages, org, callerIPAddress);

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
            map.get(org).remove(message);
        } else if (message != null && message.getNotificationType() == NotificationType.ALL) {
            log.info("Returning message with ID: {} to caller: {}", message.getId(), message);
            markMessageAsSeenForCaller(callerIPAddress, org, message);
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
     * @param callerIPAddress caller's IP address
     * @param org organization
     * @param message Message
     */
    private void markMessageAsSeenForCaller(final String callerIPAddress, final String org, final Message message) {
        final List<Long> seenList = new ArrayList<>();
        seenList.add(message.getId());
        Map<String, List<Long>> ipMap = seenMap.get(org);
        if (ipMap == null) {
            ipMap = new HashMap<>();
            ipMap.put(callerIPAddress, seenList);
        } else {
            final List<Long> priorSeenList = ipMap.get(callerIPAddress);
            if (priorSeenList != null) {
                seenList.addAll(priorSeenList);
            }
            ipMap.put(callerIPAddress, seenList);
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
     * @param callerIPAddress filter criteria
     * @return filtered messages
     */
    private List<Message> filterSeen(final List<Message> messages,
                                     final String organization,
                                     final String callerIPAddress) {
        List<Message> messageList = messages;
        final Map<String, List<Long>> ipMap = seenMap.get(organization);
        if (ipMap != null) {
            final List<Long> seenList = ipMap.get(callerIPAddress);
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
     * Gets organization name.
     *
     * @param message Message
     * @return organization name
     */
    private String getOrganization(final Message message) {
        return getOrganization(message.getOrganization());
    }

    /**
     * Get organization name.
     *
     * @param organization name
     * @return organization name
     */
    private String getOrganization(final String organization) {
        String org = organization;
        if (org == null) {
            org = CommonConstants.DEFAULT_ORGANIZATION;
        }
        return org.toUpperCase();
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
