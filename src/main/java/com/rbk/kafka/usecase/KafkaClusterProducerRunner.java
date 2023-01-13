package com.rbk.kafka.usecase;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class KafkaClusterProducerRunner implements Runnable{

    public static void main(String[] args) {
        for (int i = 0; i<3; i++) {
            Thread producer = new Thread(new KafkaClusterProducerRunner());
            producer.start();
            sleep(1000);
        }
    }

    @SuppressWarnings("")
    @Override
    public void run() {
        SimpleKafkaProducer producer = new SimpleKafkaProducer("localhost:9092, localhost:9093, localhost:9094");

        //noinspection InfiniteLoopStatement
        while (true) {
            producer.publish("factory.monitoring.temperature",
                    "M-"+Math.round(Math.random()*1000), String.valueOf(getRandomMeasurement()));

            sleep(3000);
        }
    }

    private static double getRandomMeasurement() {
        return BigDecimal.valueOf(100 * Math.random()).setScale(2, RoundingMode.CEILING).doubleValue();
    }

    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
