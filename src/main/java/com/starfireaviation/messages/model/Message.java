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

package com.starfireaviation.messages.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class Message implements Comparable<Message>, Serializable {

    /**
     * Organization.
     */
    private String organization;

    /**
     * Priority.
     */
    private Priority priority;

    /**
     * Type.
     *
     * Note: reserved for future implementation
     */
    private String type;

    /**
     * Payload.
     */
    private String payload;

    /**
     * Compares this message's priority to another message's priority.
     *
     * @param other the object to be compared.
     * @return priority order
     */
    @Override
    public int compareTo(final Message other) {
        if (getPriority() != other.getPriority()) {
            if (other.getPriority() == Priority.HIGH) {
                return -1;
            }
            if (other.getPriority() == Priority.LOW) {
                return 1;
            }
        }
        return 0;
    }
}
