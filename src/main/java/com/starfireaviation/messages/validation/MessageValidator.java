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

package com.starfireaviation.messages.validation;

import com.starfireaviation.messages.exception.InvalidPayloadException;
import com.starfireaviation.model.Message;
import lombok.extern.slf4j.Slf4j;

/**
 * MessageValidator.
 */
@Slf4j
public class MessageValidator {

    /**
     * Message Validation.
     *
     * @param message Message
     * @throws InvalidPayloadException when message information is invalid
     */
    public void validate(final Message message) throws InvalidPayloadException {
        empty(message);
        emptyOrganization(message);
    }

    /**
     * Ensures organization is not null.
     *
     * @param message Message
     */
    private static void emptyOrganization(final Message message) throws InvalidPayloadException {
        if (message.getOrganization() == null) {
            String msg = "No organization was provided";
            log.warn(msg);
            throw new InvalidPayloadException(msg);
        }
    }

    /**
     * Ensures message object is not null.
     *
     * @param message Message
     * @throws InvalidPayloadException when message is null
     */
    private static void empty(final Message message) throws InvalidPayloadException {
        if (message == null) {
            String msg = "No message information was provided";
            log.warn(msg);
            throw new InvalidPayloadException(msg);
        }
    }

}
