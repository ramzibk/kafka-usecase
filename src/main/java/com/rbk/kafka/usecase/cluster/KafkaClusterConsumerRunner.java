package com.rbk.kafka.usecase.cluster;

import com.rbk.kafka.usecase.singlebroker.SimpleKafkaConsumer;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;

public class KafkaClusterConsumerRunner implements Runnable{

    SimpleKafkaConsumer consumer;

    public KafkaClusterConsumerRunner() {
         consumer = new SimpleKafkaConsumer(
                "localhost:9092, localhost:9093, localhost:9094",
                 Collections.singletonList("factory.monitoring.temperature"),
                "ClientAppGroup",
                Duration.of(60, ChronoUnit.SECONDS));
    }

    public static void main(String[] args) {
        for(int i=0; i< 2; i++) {
            Thread consumer = new Thread(new KafkaClusterConsumerRunner());
            consumer.start();
        }
    }

    @Override
    public void run() {
        //noinspection InfiniteLoopStatement
        while (true) {
            consumer.poll();
        }
    }
}
