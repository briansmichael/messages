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

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractSteps {
    
        /**
     * CucumberTestContext.
     */
    private final TestContext CONTEXT = TestContext.CONTEXT;

    protected String baseUrl() {
        return "http://localhost:8080";
    }

    protected TestContext testContext() {
        return CONTEXT;
    }

    protected void executePost(String apiPath) {
        executePost(apiPath, null, null);
    }

    protected void executePost(String apiPath, Map<String, String> pathParams) {
        executePost(apiPath, pathParams, null);
    }

    protected void executePost(String apiPath, Map<String, String> pathParams, Map<String, String> queryParamas) {
        final RequestSpecification request = CONTEXT.getRequest();
        final Object payload = CONTEXT.getPayload();
        final String url = baseUrl() + apiPath;

        setPayload(request, payload);
        setQueryParams(pathParams, request);
        setPathParams(queryParamas, request);

        Response response = request.accept(ContentType.JSON)
                .log()
                .all()
                .post(url);

        logResponse(response);

        CONTEXT.setResponse(response);
    }

    protected void executeMultiPartPost(String apiPath) {
        final RequestSpecification request = CONTEXT.getRequest();
        final Object payload = CONTEXT.getPayload();
        final String url = baseUrl() + apiPath;

        Response response = request.multiPart("fuelTransfer", payload, "application/json")
                .log()
                .all()
                .post(url);

        logResponse(response);
        CONTEXT.setResponse(response);
    }

    protected void executeDelete(String apiPath) {
        executeDelete(apiPath, null, null);
    }

    protected void executeDelete(String apiPath, Map<String, String> pathParams) {
        executeDelete(apiPath, pathParams, null);
    }

    protected void executeDelete(String apiPath, Map<String, String> pathParams, Map<String, String> queryParams) {
        final RequestSpecification request = CONTEXT.getRequest();
        final Object payload = CONTEXT.getPayload();
        final String url = baseUrl() + apiPath;

        setPayload(request, payload);
        setQueryParams(pathParams, request);
        setPathParams(queryParams, request);

        Response response = request.accept(ContentType.JSON)
                .log()
                .all()
                .delete(url);

        logResponse(response);
        CONTEXT.setResponse(response);
    }

    protected void executePut(String apiPath) {
        executePut(apiPath, null, null);
    }

    protected void executePut(String apiPath, Map<String, String> pathParams) {
        executePut(apiPath, pathParams, null);
    }

    protected void executePut(String apiPath, Map<String, String> pathParams, Map<String, String> queryParams) {
        final RequestSpecification request = CONTEXT.getRequest();
        final Object payload = CONTEXT.getPayload();
        final String url = baseUrl() + apiPath;

        setPayload(request, payload);
        setQueryParams(pathParams, request);
        setPathParams(queryParams, request);

        Response response = request.accept(ContentType.JSON)
                .log()
                .all()
                .put(url);

        logResponse(response);
        CONTEXT.setResponse(response);
    }

    protected void executePatch(String apiPath) {
        executePatch(apiPath, null, null);
    }

    protected void executePatch(String apiPath, Map<String, String> pathParams) {
        executePatch(apiPath, pathParams, null);
    }

    protected void executePatch(String apiPath, Map<String, String> pathParams, Map<String, String> queryParams) {
        final RequestSpecification request = CONTEXT.getRequest();
        final Object payload = CONTEXT.getPayload();
        final String url = baseUrl() + apiPath;

        setPayload(request, payload);
        setQueryParams(pathParams, request);
        setPathParams(queryParams, request);

        Response response = request.accept(ContentType.JSON)
                .log()
                .all()
                .patch(url);

        logResponse(response);
        CONTEXT.setResponse(response);
    }

    protected void executeGet(String apiPath) {
        executeGet(apiPath, null, null);
    }

    protected void executeGet(String apiPath, Map<String, String> pathParams) {
        executeGet(apiPath, pathParams, null);
    }

    protected void executeGet(String apiPath, Map<String, String> pathParams, Map<String, String> queryParams) {
        final RequestSpecification request = CONTEXT.getRequest();
        final String url = baseUrl() + apiPath;

        setQueryParams(pathParams, request);
        setPathParams(queryParams, request);

        Response response = request.accept(ContentType.JSON)
                .log()
                .all()
                .get(url);

        logResponse(response);
        CONTEXT.setResponse(response);
    }

    private void logResponse(Response response) {
        response.then()
                .log()
                .all();
    }

    private void setPathParams(Map<String, String> queryParamas, RequestSpecification request) {
        if (null != queryParamas) {
            request.queryParams(queryParamas);
        }
    }

    private void setQueryParams(Map<String, String> pathParams, RequestSpecification request) {
        if (null != pathParams) {
            request.pathParams(pathParams);
        }
    }

    private void setPayload(RequestSpecification request, Object payload) {
        if (null != payload) {
            request.contentType(ContentType.JSON)
                    .body(payload);
        }
    }

}
