package com.rbk.kafka.usecase.singlebroker;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerConfig;

import java.time.Duration;
import java.util.List;
import java.util.Properties;

public class SimpleKafkaConsumer{

    private final KafkaConsumer<String, String> consumer;

    private final Duration pollDuration;

    public SimpleKafkaConsumer(String brokerURI, List<String> topics, String topicGroup, Duration pollDuration) {
        super();
        this.pollDuration = pollDuration;

        Properties config = new Properties();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerURI);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        config.put(ConsumerConfig.GROUP_ID_CONFIG, topicGroup);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        consumer = new KafkaConsumer<>(config);
        try {
            consumer.subscribe(topics);
        } catch (Exception e) {
            consumer.close();
        }
    }

    public void poll() {
        try {
            ConsumerRecords<String, String> records = consumer.poll(this.pollDuration);

            for (ConsumerRecord<String, String> rec : records) {
                System.out.println("GroupId "+consumer.groupMetadata().groupId()+
                        " GrpInst "+consumer.groupMetadata().memberId()+
                        " received record "+
                        " having offset "+rec.offset()+
                        " from topic: " + rec.topic()+
                        " and partition: "+ rec.partition()+
                        " with key: " + rec.key() +
                        " with value: " + rec.value());
            }

            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } catch (Exception e) {
            consumer.close();
        }
    }

}
