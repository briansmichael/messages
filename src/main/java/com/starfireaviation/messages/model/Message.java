package com.starfireaviation.messages.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class Message implements Serializable {

    /**
     * Organization.
     *
     * Note: reserved for future implementation
     */
    private String organization;

    /**
     * Priority.
     *
     * Note: reserved for future implementation
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
}
