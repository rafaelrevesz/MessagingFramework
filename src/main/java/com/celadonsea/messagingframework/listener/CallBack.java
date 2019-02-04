package com.celadonsea.messagingframework.listener;

import com.celadonsea.messagingframework.annotation.Listener;
import com.celadonsea.messagingframework.annotation.MessagingController;
import com.celadonsea.messagingframework.client.MessageClient;
import com.celadonsea.messagingframework.config.MessageClientConfig;
import com.celadonsea.messagingframework.core.ConsumingProperties;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

/**
 * Provides functionality for arrived message callback.
 *
 * @author Rafael Revesz
 * @since 1.0
 */
@Slf4j
public class CallBack implements MqttCallback, Consumer {

    /**
     * Default maximal number of threads for processing incoming messages per client.
     */
    private static final int DEFAULT_MAX_THREAD_FOR_INCOMING_MESSAGES_PER_CLIENT = 200;

    /**
     * Default keep alive time in seconds for thread pool executor service.
     */
    public static final int DEFAULT_KEEP_ALIVE_TIME = 1;

    /**
     * Message client.
     */
    private final MessageClient messageClient;

    /**
     * The subscription map contains the subscription topic templates as keys and the callable
     * message process functions as values.
     */
    private Map<String, BiConsumer<String, byte[]>> subscriptionMap = new HashMap<>();

    /**
     * The subscription name mapping contains the subscription topic template as keys
     * and the transformed topic for matching with the incoming topic.
     * @see com.celadonsea.messagingframework.topic.TopicTransformer
     * @see com.celadonsea.messagingframework.scanner.ListenerCallbackPostProcessor#processListenerMethod(Object, MessageClient, Method, Listener, MessagingController)
     */
    private Map<String, String> subscriptionNameMapping = new HashMap<>();

    /**
     * Thread pool executor for multi thread message processing
     */
    private ThreadPoolExecutor executorService;

    /**
     * Constructor sets the message client and the executor service for
     * the multi thread processing.
     *
     * @param messageClient message client
     * @param messageClientConfig message client configuration
     */
    public CallBack(MessageClient messageClient, MessageClientConfig messageClientConfig) {
        this.messageClient = messageClient;

        int maxThread = messageClientConfig.getMaxThread();
        if (maxThread <= 0) {
            maxThread = DEFAULT_MAX_THREAD_FOR_INCOMING_MESSAGES_PER_CLIENT;
        }

        executorService = (ThreadPoolExecutor )Executors.newFixedThreadPool(maxThread);

        int keepAliveTime = messageClientConfig.getThreadKeepAliveTime() < 0
            ? DEFAULT_KEEP_ALIVE_TIME
            : messageClientConfig.getThreadKeepAliveTime();

        executorService.setKeepAliveTime(keepAliveTime, TimeUnit.SECONDS);

    }

    /**
     * Stores a subscription topic mapped to a call back function and to a comparable topic.
     * Comparable topic does not contain topic prefixes which won't come if a message arrives,
     * eg. shared subscriptions ($share/group/real/topic/parts/...)
     *      *
     * @param topic subscription topic
     * @param topicToParse comparable topic
     * @param consumer a call back function
     */
    public void subscribe(String topic, String topicToParse, BiConsumer<String, byte[]> consumer) {
        subscriptionMap.put(topic, consumer);
        subscriptionNameMapping.put(topic, topicToParse);
    }

    /**
     * Stores a topic mapped to a call back function.
     * @param topic subscription topic
     * @param consumer a call back function
     */
    public void subscribe(String topic, BiConsumer<String, byte[]> consumer) {
        subscribe(topic, topic, consumer);
    }

    /**
     * Processes the message arrive event. The incoming topic will be checked if it matches
     * to one of the stored subscription. If yes then the corresponding call back function
     * will be called.
     *
     * @param topic name of the topic on the message was published to
     * @param message arrived message
     */
    public void messageArrived(String topic, byte[] message) {
        Iterator<Map.Entry<String, BiConsumer<String, byte[]>>> iterator = subscriptionMap.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, BiConsumer<String, byte[]>> subscription = iterator.next();
            if (topic.matches(subscriptionNameMapping.get(subscription.getKey())
                .replaceAll("/", "\\/")
                .replaceAll("\\.", "\\.")
                .replaceAll("\\*", "(.*)")
                .replaceAll("\\+", "(.*)"))) {
                executorService.submit(() -> subscription.getValue().accept(topic, message));
                log.debug("Queue size: {}", executorService.getQueue().size());
                log.debug("Pool size: {}", executorService.getPoolSize());
            }
        }
    }

    /**
     * This method is called when the MQTT connection to the server is lost.
     * The method tries to reconnect to the broker, and resubscribe to
     * all stored topics.
     *
     * @param cause the reason behind the loss of connection.
     */
    @Override
    public void connectionLost(Throwable cause) {
        log.error("Connection lost", cause);
        messageClient.reconnect(this);
        subscriptionMap.forEach((topic, biConsumer) -> messageClient.subscribe(new ConsumingProperties(topic), biConsumer));
    }

    /**
     * This method is called when a message arrives from the server.
     *
     * @param topic name of the topic on the message was published to
     * @param message the actual message.
     * @throws Exception if a terminal error has occurred, and the client should be
     * shut down.
     */
    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        messageArrived(topic, message.getPayload());
    }

    /**
     * Called when delivery for a message has been completed, and all
     * acknowledgments have been received. For QoS 0 messages it is
     * called once the message has been handed to the network for
     * delivery. For QoS 1 it is called when PUBACK is received and
     * for QoS 2 when PUBCOMP is received. The token will be the same
     * token as that returned when the message was published.
     *
     * @param token the delivery token associated with the message.
     */
    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        log.debug("Message delivered: " + token.toString());
    }

    /**
     * @see Consumer#handleConsumeOk(String)
     */
    @Override
    public void handleConsumeOk(String consumerTag) {
        log.debug("Consumer Ok: {}", consumerTag);
    }

    /**
     * @see Consumer#handleCancelOk(String)
     */
    @Override
    public void handleCancelOk(String consumerTag) {
        log.debug("Cancel Ok: {}", consumerTag);
    }

    /**
     * @see Consumer#handleCancel(String)
     */
    @Override
    public void handleCancel(String consumerTag) throws IOException {
        log.debug("Cancel: {}", consumerTag);
    }

    /**
     * @see Consumer#handleShutdownSignal(String, ShutdownSignalException)
     */
    @Override
    public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
        log.debug("Shutdown signal: {}", consumerTag);
    }

    /**
     * @see Consumer#handleRecoverOk(String)
     */
    @Override
    public void handleRecoverOk(String consumerTag) {
        if(log.isDebugEnabled()) {
            log.debug("Recovery Ok: {}", consumerTag);
        }
    }

    /**
     * Called when a <code><b>basic.deliver</b></code> is received for this consumer.
     * @param consumerTag the <i>consumer tag</i> associated with the consumer
     * @param envelope packaging data for the message
     * @param properties content header data for the message
     * @param body the message body (opaque, client-specific byte array)
     * @throws IOException if the consumer encounters an I/O error while processing the message
     * @see Envelope
     */
    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        messageArrived(envelope.getRoutingKey(), body);
    }
}
