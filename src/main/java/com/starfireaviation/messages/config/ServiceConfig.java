package com.starfireaviation.messages.config;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.starfireaviation.messages.service.MessageService;
import com.starfireaviation.messages.validation.MessageValidator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        ApplicationProperties.class,
})
public class ServiceConfig {

    /**
     * MessageService.
     *
     * @return MessageService
     */
    @Bean
    public MessageService messageService(@Qualifier("app") final HazelcastInstance instance) {
        return new MessageService(instance);
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
        ClientConfig clientConfig = new ClientConfig();
        return HazelcastClient.newHazelcastClient(clientConfig);
    }
}
