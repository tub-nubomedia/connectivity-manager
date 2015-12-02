package org.nubomedia.qosmanager.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.nubomedia.qosmanager.beans.OpenbatonEventSubscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
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

    private Logger logger;
    @Autowired private Environment env;

    @PostConstruct
    private void init(){

        this.logger = LoggerFactory.getLogger(this.getClass());

    }

    @Bean
    public Gson getMapper(){
        return new GsonBuilder().create();
    }

    @Bean
    public ConnectionFactory getConnectionFactory(Environment env){
        CachingConnectionFactory factory = new CachingConnectionFactory(env.getProperty("spring.rabbitmq.host"));
        factory.setPassword(env.getProperty("spring.rabbitmq.password"));
        factory.setUsername(env.getProperty("spring.rabbitmq.username"));
        return factory;
    }

    @Bean
    public TopicExchange getTopic(){
        return new TopicExchange("openbaton-exchange");
    }

    @Bean
    public Queue getSubscriptionQueue(){
        return new Queue("nsr-creation",false);
    }

    @Bean
    public Binding setBinding(Queue queue, TopicExchange topicExchange){
        return BindingBuilder.bind(queue).to(topicExchange).with("ns-creation");
    }

    @Bean
    public MessageListenerAdapter setMessageListenerAdapter(OpenbatonEventSubscription subscription){
        return new MessageListenerAdapter(subscription,"receiveNewNsr");
    }

}
