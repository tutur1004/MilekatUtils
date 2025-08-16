package fr.milekat.utils.messaging.adapter.rabbitmq;

import com.rabbitmq.client.Channel;
import fr.milekat.utils.messaging.ReceivedMessage;

import java.io.IOException;

public class RabbitMessage implements ReceivedMessage {
    private final Channel channel;
    private final long deliveryTag; // Delivery tag for acknowledgment
    private final String routingKey;
    private final String message;
    private boolean acknowledged = false;

    public RabbitMessage(Channel channel, long deliveryTag, String routingKey, String message) {
        this.channel = channel;
        this.deliveryTag = deliveryTag;
        this.routingKey = routingKey;
        this.message = message;
    }

    @Override
    public String getRoutingKey() {
        return routingKey;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public void ack() throws IOException {
        if (!acknowledged) {
            channel.basicAck(deliveryTag, false);
            acknowledged = true;
        }
    }

    @Override

    public void reject() throws IOException {
        if (!acknowledged) {
            channel.basicNack(deliveryTag, false, false);
            acknowledged = true;
        }
    }

    @Override
    public boolean isAcknowledged() {
        return acknowledged;
    }
}