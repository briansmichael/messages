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
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@Slf4j
public class MessageStorageSteps extends AbstractSteps {

    /**
     * ORGANIZATION.
     */
    private static final String ORGANIZATION = "TEST_ORG";

    /**
     * Message.
     */
    private Message message;

    @Given("^I have a message$")
    public void iHaveAMessage() throws Throwable {
        message = new Message();
    }

    @Given("^I provide an organization$")
    public void iProvideAnOrganization() throws Throwable {
        message.setOrganization(ORGANIZATION);
    }

    @When("^I add the message$")
    public void iAddTheMessage() throws Throwable {
        log.info("I add the message");
        String addMessageUrl = "/messages";
        executePost(addMessageUrl);
    }

    @Then("^I should receive (.*)$")
    public void iShouldReceive(final String expectedResult) throws Throwable {
        final Response response = testContext().getResponse();

        switch (expectedResult) {
            case "a message added response":
                log.info("I should receive a message added response");
                assertThat(response.statusCode()).isIn(200, 201);
                break;
            case "an InvalidPayloadException":
                log.info("I should receive an InvalidPayloadException");
                assertThat(response.statusCode()).isBetween(400, 504);
                break;
            default:
                fail("Unexpected error");
        }
    }

}
