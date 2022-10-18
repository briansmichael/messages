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

import com.starfireaviation.model.Message;
import com.starfireaviation.model.NotificationType;
import com.starfireaviation.model.Priority;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

@Slf4j
public class MessageRetrievalSteps extends BaseSteps {

    @Before
    public void init() {
        testContext.reset();
    }

    @Given("^No messages are available$")
    public void noMessagesAreAvailable() throws Throwable {
    }

    @Given("^A message is available$")
    public void aMessageIsAvailable() throws Throwable {
        testContext.setMessage(new Message());
    }

    @And("^with the (.*) set to (.*)$")
    public void withTheSetTo(final String key, final String value) throws Throwable {
        switch (key) {
            case "priority":
                if (!"null".equalsIgnoreCase(value)) {
                    testContext.getMessage().setPriority(Priority.valueOf(value));
                } else {
                    testContext.getMessage().setPriority(null);
                }
                break;
            case "notificationType":
                if (!"null".equalsIgnoreCase(value)) {
                    testContext.getMessage().setNotificationType(NotificationType.valueOf(value));
                } else {
                    testContext.getMessage().setNotificationType(null);
                }
                break;
            default:
                // Do nothing
        }
    }

    @And("^the message is next in the queue$")
    public void theMessageIsNextInTheQueue() throws Throwable {
        final HttpEntity<Message> httpEntity = new HttpEntity<>(testContext.getMessage(), getHeaders());
        restTemplate.postForEntity(URL, httpEntity, Void.class);
    }

    @When("^I get a message$")
    public void iGetAMessage() throws Throwable {
        log.info("I get a message");
        testContext.setResponse(restTemplate.exchange(URL, HttpMethod.GET, new HttpEntity<Object>(getHeaders()), Message.class));
    }

    @When("^I get a message with (.*)$")
    public void iGetAMessage(final String queryParams) throws Throwable {
        log.info("I get a message with queryParams: {}", queryParams);
        testContext.setResponse(restTemplate.exchange(URL + "?" + queryParams, HttpMethod.GET, new HttpEntity<Object>(getHeaders()), Message.class));
    }

    private HttpHeaders getHeaders() {
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        if (testContext.getClientId() != null) {
            httpHeaders.add("client-id", testContext.getClientId());
        }
        if (testContext.getOrganization() != null) {
            httpHeaders.add("organization", testContext.getOrganization());
        }
        if (testContext.getCorrelationId() != null) {
            httpHeaders.add("correlation-id", testContext.getCorrelationId());
        }
        return httpHeaders;
    }
}
