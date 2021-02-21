package org.example.app;

import org.example.app.loggers.MyFormatter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;

@SpringBootApplication
public class App {

    public final static Logger logger = Logger.getLogger(App.class.getName());

    static {
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new MyFormatter());
        logger.setUseParentHandlers(false);
        logger.addHandler(handler);
    }

    public static void main(String[] args) {
        SpringApplication.run(App.class);
        logger.info("start authentication server");
    }
}
