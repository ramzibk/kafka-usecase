package com.rbk.kafka.usecase;


public class Main {

    public static final String TOPIC_NAME = "usecase.chat.messages";

    public static void main(String[] args) {
        SimpleKafkaProducer producer = new SimpleKafkaProducer("localhost:9092");
        producer.publish(TOPIC_NAME, "new student registration "+System.currentTimeMillis());
        producer.publish(TOPIC_NAME, "studentName", "John Doe");

        SimpleKafkaConsumer consumer = new SimpleKafkaConsumer("localhost:9092", TOPIC_NAME);
        consumer.poll();
    }
}