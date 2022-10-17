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

import static io.restassured.RestAssured.given;
import static java.lang.ThreadLocal.withInitial;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.util.HashMap;
import java.util.Map;

/**
 * Singleton to manage objects and share their state between step definitions.
 */
public enum TestContext {
    CONTEXT;

    private static final String PAYLOAD = "PAYLOAD";
    private static final String REQUEST = "REQUEST";
    private static final String RESPONSE = "RESPONSE";

    private final ThreadLocal<Map<String, Object>> threadLocal = withInitial(HashMap::new);

    private Map<String, Object> testContextMap() {
        return threadLocal.get();
    }

    public void set(String key, Object value) {
        testContextMap().put(key, value);
    }

    public Object get(String key) {
        return testContextMap().get(key);
    }

    public <T> T get(String key, Class<T> clazz) {
        return clazz.cast(testContextMap().get(key));
    }

    public void setPayload(Object value) {
        set(PAYLOAD, value);
    }

    public Object getPayload() {
        return testContextMap().get(PAYLOAD);
    }

    public <T> T getPayload(Class<T> clazz) {
        return get(PAYLOAD, clazz);
    }

    public RequestSpecification getRequest() {
        RequestSpecification req = get(REQUEST, RequestSpecification.class);
        return (null == req) ? given() : req;
    }

    public void setResponse(Response response) {
        set(RESPONSE, response);
    }

    public Response getResponse() {
        return get(RESPONSE, Response.class);
    }

    public void reset() {
        testContextMap().clear();
    }

}