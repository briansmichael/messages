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

package com.starfireaviation.messages.controller;

import com.starfireaviation.messages.exception.InsufficientStorageException;
import com.starfireaviation.messages.exception.InvalidPayloadException;
import com.starfireaviation.messages.exception.ResourceNotFoundException;
import com.starfireaviation.model.Message;
import com.starfireaviation.messages.service.MessageService;
import com.starfireaviation.messages.validation.MessageValidator;
import com.starfireaviation.model.NotificationType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping({ "/messages" })
public class MessageController {

    /**
     * MessageService.
     */
    private final MessageService messageService;

    /**
     * MessageValidator.
     */
    private final MessageValidator messageValidator;

    /**
     * MessageController.
     *
     * @param mService MessageService
     * @param mValidator MessageValidator
     */
    public MessageController(final MessageService mService,
                             final MessageValidator mValidator) {
        messageService = mService;
        messageValidator = mValidator;
    }

    /**
     * Stores a message for later retrieval.
     *
     * @param organization organization
     * @param correlationId CorrelationID
     * @param message Message
     * @throws InvalidPayloadException when message payload is not valid
     * @throws InsufficientStorageException when message add fails
     */
    @PostMapping
    public void post(@RequestHeader("organization") final String organization,
                     @RequestHeader("correlation-id") final String correlationId,
                     @RequestBody final Message message) throws InvalidPayloadException, InsufficientStorageException {
        messageValidator.validate(message);
        final boolean success = messageService.addMessage(message, organization, correlationId);
        if (!success) {
            throw new InsufficientStorageException("Message add failed");
        }
    }

    /**
     * Retrieves a message.
     *
     * @param organization Organization
     * @param correlationId CorrelationID
     * @param clientId ClientID
     * @param notificationType optional NotificationType query parameter
     * @return Message
     * @throws ResourceNotFoundException when no message is found
     */
    @GetMapping
    public Message get(@RequestHeader("organization") final String organization,
                       @RequestHeader("correlation-id") final String correlationId,
                       @RequestHeader("client-id") final String clientId,
                       @RequestParam(name = "notificationType", required = false) final String notificationType) 
                       throws ResourceNotFoundException {
        final Message message = 
                messageService.getMessage(organization, getType(notificationType), clientId, correlationId);
        if (message == null) {
            throw new ResourceNotFoundException("No message matching provided criteria was found");
        }
        return message;
    }

    /**
     * Get NotificationType.
     *
     * @param type user input
     * @return NotificationType
     */
    private NotificationType getType(final String type) {
        if (type == null) {
            return NotificationType.ALL;
        } else {
            return NotificationType.valueOf(type.toUpperCase());
        }
    }

}
