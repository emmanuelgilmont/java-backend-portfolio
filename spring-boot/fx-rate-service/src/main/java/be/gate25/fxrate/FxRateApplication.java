package be.gate25.fxrate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class FxRateApplication {

    public static void main(String[] args) {
        SpringApplication.run(FxRateApplication.class, args);
    }
}
