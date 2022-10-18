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
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;

@Slf4j
public class MessageRetrievalSteps extends BaseSteps {

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
            case "organization":
                if (!"null".equalsIgnoreCase(value)) {
                    testContext.getMessage().setOrganization(value);
                } else {
                    testContext.getMessage().setOrganization(null);
                }
                break;
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
        restTemplate.postForEntity(URL, new HttpEntity<>(testContext.getMessage()), Void.class);
    }

    @When("^I get a message$")
    public void iGetAMessage() throws Throwable {
        log.info("I get a message");
        testContext.setResponse(restTemplate.getForEntity(URL, Message.class));
    }

    @When("^I get a message with (.*)$")
    public void iGetAMessage(final String queryParams) throws Throwable {
        log.info("I get a message with queryParams: {}", queryParams);
        testContext.setResponse(restTemplate.getForEntity(URL + "?" + queryParams, Message.class));
    }

}
