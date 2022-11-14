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

package com.starfireaviation.messages;

import com.starfireaviation.common.model.Message;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@Data
public class TestContext {

    /**
     * Message.
     */
    private Message message;

    /**
     * Response.
     */
    private ResponseEntity<?> response;

    /**
     * Organization.
     */
    private String organization;

    /**
     * CorrelationID.
     */
    private String correlationId;

    /**
     * ClientID.
     */
    private String clientId;

    /**
     * Resets attributes values to their defaults.
     */
    public void reset() {
        message = null;
        response = null;
        organization = null;
        correlationId = null;
        clientId = null;
    }
}
