package org.buratishkin.familyhub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableKafka
@EnableScheduling
@SpringBootApplication
public class AddressServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AddressServiceApplication.class, args);
    }
}
