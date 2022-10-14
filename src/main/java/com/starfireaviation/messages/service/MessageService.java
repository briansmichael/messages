package com.starfireaviation.messages.service;

import com.hazelcast.collection.IQueue;
import com.hazelcast.core.HazelcastInstance;
import com.starfireaviation.messages.model.Message;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class MessageService {

    /**
     * HazelcastInstance.
     */
    private final HazelcastInstance hazelcastInstance;

    /**
     * Message queue amp.
     */
    private final Map<String, IQueue<Message>> messageQueueMap;

    /**
     * MessageService.
     *
     * @param instance HazelcastInstance
     */
    public MessageService(final HazelcastInstance instance) {
        hazelcastInstance = instance;
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
        IQueue<Message> messageQueue = null;
        boolean success = false;
        try {
            if (messageQueueMap.containsKey(org)) {
                messageQueue = messageQueueMap.get(org);
            } else {
                messageQueue = hazelcastInstance.getQueue(org);
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
     * @return Message
     */
    public Message getMessage(final String organization) {
        if (messageQueueMap.containsKey(organization)) {
            return messageQueueMap.get(organization).poll();
        }
        return null;
    }
}
