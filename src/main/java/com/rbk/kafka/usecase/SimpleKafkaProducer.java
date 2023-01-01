package com.rbk.kafka.usecase;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Properties;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class SimpleKafkaProducer{

    private final KafkaProducer<String, String> producer;

    public SimpleKafkaProducer(String brokerURI) {
        super();

        Properties config = new Properties();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerURI);
        config.put(ProducerConfig.ACKS_CONFIG, "all");

        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");

        producer = new KafkaProducer<>(config);
    }

    public void publish(String topic, String key, String value) {
        Future<RecordMetadata> recordMetadaFuture;

        recordMetadaFuture = producer.send(new ProducerRecord<>(topic, key, value));

        printMetadata(recordMetadaFuture);
    }

    public void publish(String topic, String value) {
        Future<RecordMetadata> recordMetadaFuture;

        recordMetadaFuture = producer.send(new ProducerRecord<>(topic, value));

        printMetadata(recordMetadaFuture);
    }

    private static void printMetadata(Future<RecordMetadata> recordMetadaFuture) {
        try {
            RecordMetadata recordMetadata = recordMetadaFuture.get(3000, TimeUnit.MILLISECONDS);
            System.out.println("published record to topic "+recordMetadata.topic() + ", partition " +recordMetadata.partition());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
