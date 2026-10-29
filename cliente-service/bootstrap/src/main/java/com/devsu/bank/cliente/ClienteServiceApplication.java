package com.devsu.bank.cliente;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ClienteServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(ClienteServiceApplication.class, args);
  }
}
