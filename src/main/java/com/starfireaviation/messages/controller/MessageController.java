package com.starfireaviation.messages.controller;

import com.starfireaviation.messages.config.CommonConstants;
import com.starfireaviation.messages.exception.InsufficientStorageException;
import com.starfireaviation.messages.exception.InvalidPayloadException;
import com.starfireaviation.messages.model.Message;
import com.starfireaviation.messages.service.MessageService;
import com.starfireaviation.messages.validation.MessageValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
        message.setOrganization(CommonConstants.DEFAULT_ORGANIZATION);
        final boolean success = messageService.addMessage(message);
        if (!success) {
            throw new InsufficientStorageException("Message add failed");
        }
    }

    /**
     * Retrieves a message.
     *
     * @return Message
     */
    @GetMapping
    public Message get() {
        return messageService.getMessage(CommonConstants.DEFAULT_ORGANIZATION);
    }
}
