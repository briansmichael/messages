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
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@Slf4j
public class MessageStorageSteps extends BaseSteps {

    @Before
    public void init() {
        testContext.reset();
    }

    @Given("^I have a message$")
    public void iHaveAMessage() throws Throwable {
        testContext.setMessage(new Message());
    }

    @When("^I add the message$")
    public void iAddTheMessage() throws Throwable {
        log.info("I add the message");
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (testContext.getOrganization() != null) {
            headers.add("organization", testContext.getOrganization());
        }
        if (testContext.getCorrelationId() != null) {
            headers.add("correlation-id", testContext.getCorrelationId());
        }
        final HttpEntity<Message> httpEntity = new HttpEntity<>(testContext.getMessage(), headers);
        testContext.setResponse(restTemplate.postForEntity(URL, httpEntity, Void.class));
    }

}
