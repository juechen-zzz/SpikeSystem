package com.komorebi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
//public class MainApplication extends SpringBootServletInitializer {
public class MainApplication {
    public static void main(String[] args) throws Exception {
        SpringApplication.run(MainApplication.class, args);
    }

//    @Override
//    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
////        return super.configure(builder);
//        return builder.sources(MainApplication.class);
//    }
}
