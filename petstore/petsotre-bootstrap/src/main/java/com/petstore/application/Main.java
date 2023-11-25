package com.petstore.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

/** The type Main. */
@SpringBootApplication
@ImportResource({
  "classpath:META-INF/spring/petstore-petstoredb-dal.xml",
  "classpath:META-INF/spring/app-beans.xml"
})
public class Main {
  /**
   * The entry point of application.
   *
   * @param args the input arguments
   */
  public static void main(String[] args) {
    SpringApplication.run(Main.class, args);
  }
}
