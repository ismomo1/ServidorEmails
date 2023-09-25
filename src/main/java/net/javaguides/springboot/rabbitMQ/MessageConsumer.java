package net.javaguides.springboot.rabbitMQ;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class MessageConsumer {

    @RabbitListener(queues = "my-queue")
    public void handleMessage(String message) {
        System.out.println("Mensaje recibido: " + message);
    }
}
