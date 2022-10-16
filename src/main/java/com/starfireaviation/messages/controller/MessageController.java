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

import com.starfireaviation.messages.config.CommonConstants;
import com.starfireaviation.messages.exception.InsufficientStorageException;
import com.starfireaviation.messages.exception.InvalidPayloadException;
import com.starfireaviation.messages.exception.ResourceNotFoundException;
import com.starfireaviation.model.Message;
import com.starfireaviation.messages.service.MessageService;
import com.starfireaviation.messages.validation.MessageValidator;
import com.starfireaviation.model.NotificationType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping({
        "/messages"
})
public class MessageController {

    /**
     * IPv4 Localhost.
     */
    private static final String LOCALHOST_IPV4 = "127.0.0.1";

    /**
     * IPv6 Localhost.
     */
    private static final String LOCALHOST_IPV6 = "0:0:0:0:0:0:0:1";

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
     * @param request HttpServletRequest
     * @return Message
     * @throws ResourceNotFoundException when no message is found
     */
    @GetMapping
    public Message get(@RequestParam(name = "notificationType", required = false) final String notificationType,
                       @RequestParam(name = "organization", required = false) final String organization,
                       final HttpServletRequest request) throws ResourceNotFoundException {
        final Message message = messageService.getMessage(getOrganization(organization),
                getType(notificationType), getClientIp(request));
        if (message == null) {
            throw new ResourceNotFoundException("No message matching provided criteria was found");
        }
        return message;
    }

    /**
     * Get organization.
     *
     * @param org user input
     * @return organization
     */
    private String getOrganization(final String org) {
        return Objects.requireNonNullElse(org, CommonConstants.DEFAULT_ORGANIZATION);
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

    /**
     * Gets the IP Address for the caller.
     *
     * @param request HttpServletRequest
     * @return client IP Address
     */
    public String getClientIp(final HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ObjectUtils.isEmpty(ipAddress) || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }

        if (ObjectUtils.isEmpty(ipAddress) || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }

        if (ObjectUtils.isEmpty(ipAddress) || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
            if (LOCALHOST_IPV4.equals(ipAddress) || LOCALHOST_IPV6.equals(ipAddress)) {
                try {
                    InetAddress inetAddress = InetAddress.getLocalHost();
                    ipAddress = inetAddress.getHostAddress();
                } catch (UnknownHostException e) {
                    log.error(e.getMessage());
                }
            }
        }

        if (!ObjectUtils.isEmpty(ipAddress)
                && ipAddress.length() > CommonConstants.FIFTEEN && ipAddress.indexOf(",") > 0) {
            ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
        }

        return ipAddress;
    }
}
