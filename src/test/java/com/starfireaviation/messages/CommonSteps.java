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

import io.cucumber.java.en.Then;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.fail;

@Slf4j
public class CommonSteps extends BaseSteps {

    @Then("^I should receive (.*)$")
    public void iShouldReceive(final String expectedResult) throws Throwable {
        switch (expectedResult) {
            case "a message added response":
                log.info("I should receive a message added response");
                Assertions.assertSame(testContext.getResponse().getStatusCode(), HttpStatus.OK);
                break;
            case "an empty response":
                log.info("I should receive an empty response");
                Assertions.assertSame(testContext.getResponse().getStatusCode(), HttpStatus.NOT_FOUND);
                break;
            case "a message":
                log.info("I should receive a message");
                Assertions.assertSame(testContext.getResponse().getStatusCode(), HttpStatus.OK);
                break;
            case "an InvalidPayloadException":
                log.info("I should receive an InvalidPayloadException");
                Assertions.assertSame(testContext.getResponse().getStatusCode(), HttpStatus.BAD_REQUEST);
                break;
            default:
                fail("Unexpected error");
        }
    }
}
