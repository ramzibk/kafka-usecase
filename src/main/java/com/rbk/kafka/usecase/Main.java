package com.rbk.kafka.usecase;

public class Main {

    public static void main(String[] args) {
        SimpleKafkaProducer producer = new SimpleKafkaProducer("localhost:9092");
        producer.publish("usecase.chat.messages", "new student registration "+System.currentTimeMillis());
        producer.publish("usecase.chat.messages", "studentName", "John Doe");
    }
}