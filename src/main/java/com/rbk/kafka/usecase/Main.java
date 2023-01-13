package com.rbk.kafka.usecase;


import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;

public class Main {

    public static final String TOPIC_NAME = "usecase.chat.messages";

    public static void main(String[] args) {
        SimpleKafkaProducer producer = new SimpleKafkaProducer("localhost:9092");
        producer.publish(TOPIC_NAME, "new student registration "+System.currentTimeMillis());
        producer.publish(TOPIC_NAME, "studentName", "John Doe");

        SimpleKafkaConsumer consumer = new SimpleKafkaConsumer("localhost:9092",
                Collections.singletonList(TOPIC_NAME),
                "ClientAppGroup",
                Duration.of(1000, ChronoUnit.SECONDS));
        consumer.poll();
    }
}