package org.nubomedia.qosmanager.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.nubomedia.qosmanager.beans.openbaton.OpenbatonEventSubscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;

/**
 * Created by maa on 10.11.15.
 */

@Configuration
@ComponentScan("org.nubomedia.qosmanager")
public class ConfigurationBeans {

    public static final String queueName_eventInstatiateFinish = "nfvo.qos.nsr.create";
    public static final String queueName_eventResourcesReleaseFinish = "nfvo.qos.nsr.delete";
    private Logger logger;
    @Autowired private Environment env;

    @PostConstruct
    private void init(){

        this.logger = LoggerFactory.getLogger(this.getClass());

    }

    @Bean
    public Gson getMapper(){
        return new GsonBuilder().serializeNulls().create();
    }

    @Bean
    public ConnectionFactory getConnectionFactory(Environment env){
        logger.debug("Created ConnectionFactory");
        CachingConnectionFactory factory = new CachingConnectionFactory(env.getProperty("spring.rabbitmq.host"));
        factory.setPassword(env.getProperty("spring.rabbitmq.password"));
        factory.setUsername(env.getProperty("spring.rabbitmq.username"));
        return factory;
    }

    @Bean
    public TopicExchange getTopic(){
        logger.debug("Created Topic Exchange");
        return new TopicExchange("openbaton-exchange");
    }

    @Bean
    public Queue getCreationQueue(){
        logger.debug("Created Queue for NSR Create event");
        return new Queue(queueName_eventInstatiateFinish,false,false,true);
    }

    @Bean
    public Queue getDeletionQueue(){
        logger.debug("Created Queue for NSR Delete Event");
        return new Queue(queueName_eventResourcesReleaseFinish,false,false,true);
    }

    @Bean
    public Binding setCreationBinding(@Qualifier("getCreationQueue") Queue queue, TopicExchange topicExchange){
        logger.debug("Created Binding for NSR Creation event");
        return BindingBuilder.bind(queue).to(topicExchange).with("ns-creation");
    }

    @Bean
    public Binding setDeletionBinding(@Qualifier("getDeletionQueue") Queue queue, TopicExchange topicExchange){
        logger.debug("Created Binding for NSR Deletion event");
        return BindingBuilder.bind(queue).to(topicExchange).with("ns-deletion");
    }

    @Bean
    public MessageListenerAdapter setCreationMessageListenerAdapter(OpenbatonEventSubscription subscription){
        return new MessageListenerAdapter(subscription,"receiveNewNsr");
    }

    @Bean
    public MessageListenerAdapter setDeletionMessageListenerAdapter(OpenbatonEventSubscription subscription){
        return new MessageListenerAdapter(subscription,"deleteNsr");
    }

    @Bean
    public SimpleMessageListenerContainer setCreationMessageContainer(ConnectionFactory connectionFactory, @Qualifier("getCreationQueue") Queue queue, @Qualifier("setCreationMessageListenerAdapter") MessageListenerAdapter adapter){
        logger.debug("Created MessageContainer for NSR Creation event");
        SimpleMessageListenerContainer res = new SimpleMessageListenerContainer();
        res.setConnectionFactory(connectionFactory);
        res.setQueues(queue);
        res.setMessageListener(adapter);
        return res;
    }

    @Bean
    public SimpleMessageListenerContainer setDeletionMessageContainer(ConnectionFactory connectionFactory, @Qualifier("getDeletionQueue") Queue queue, @Qualifier("setDeletionMessageListenerAdapter") MessageListenerAdapter messageListenerAdapter){
        logger.debug("Created MessageContainer for NSR Deletion event");
        SimpleMessageListenerContainer res = new SimpleMessageListenerContainer();
        res.setConnectionFactory(connectionFactory);
        res.setQueues(queue);
        res.setMessageListener(messageListenerAdapter);
        return res;
    }

}
