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

package com.starfireaviation.messages.config;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.starfireaviation.messages.service.MessageService;
import com.starfireaviation.messages.validation.MessageValidator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EnableConfigurationProperties({
        ApplicationProperties.class,
})
public class ServiceConfig {

    /**
     * MessageService.
     *
     * @param hazelcastInstance HazelcastInstance
     * @return MessageService
     */
    @Bean
    public MessageService messageService(@Qualifier("app") final HazelcastInstance hazelcastInstance) {
        return new MessageService(hazelcastInstance);
    }

    /**
     * MessageValidator.
     *
     * @return MessageValidator
     */
    @Bean
    public MessageValidator messageValidator() {
        return new MessageValidator();
    }

    /**
     * HazelcastInstance.
     *
     * @return HazelcastInstance
     */
    @Bean("app")
    public HazelcastInstance hazelcastInstance() {
        return Hazelcast.newHazelcastInstance();
    }
}
