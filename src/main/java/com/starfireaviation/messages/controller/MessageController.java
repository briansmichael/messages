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
import com.starfireaviation.model.Message;
import com.starfireaviation.messages.service.MessageService;
import com.starfireaviation.messages.validation.MessageValidator;
import com.starfireaviation.model.NotificationType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping({
        "/messages"
})
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
     * @param message Message
     * @throws InvalidPayloadException when message payload is not valid
     * @throws InsufficientStorageException when message add fails
     */
    @PostMapping
    public void post(@RequestBody final Message message) throws InvalidPayloadException, InsufficientStorageException {
        messageValidator.validate(message);
        final boolean success = messageService.addMessage(message);
        if (!success) {
            throw new InsufficientStorageException("Message add failed");
        }
    }

    /**
     * Retrieves a message.
     *
     * @param organization optional Organization query parameter
     * @param notificationType optional NotificationType query parameter
     * @return Message
     */
    @GetMapping
    public Message get(@RequestParam(name = "notificationType", required = false) final String notificationType,
                       @RequestParam(name = "organization", required = false) final String organization) {
        return messageService.getMessage(organization, NotificationType.valueOf(notificationType));
    }
}
