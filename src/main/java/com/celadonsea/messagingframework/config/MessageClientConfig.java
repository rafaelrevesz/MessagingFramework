package com.celadonsea.messagingframework.config;

import java.util.concurrent.TimeUnit;

/**
 * Configuration interface for messaging clients.
 *
 * @author Rafael Revesz
 * @since 1.0
 */
public interface MessageClientConfig {

    /**
     * Returns the client type whish is a mandatory field.
     * The {@link com.celadonsea.messagingframework.client.MessageClientFactory}
     * can instantiate the client only if this field contains a supported client type.
     *
     * @return client type
     */
    String getClientType();

    /**
     * Returns the client ID which is unique in a message broker.
     *
     * @return client ID
     */
    String getClientId();

    /**
     * Returns the URL text of the message broker where the subscription and the publishing is possible
     *
     * @return message broker URL
     */
    String getBrokerUrl();

    /**
     * Returns the maximal size of the in flight window (MQTT)
     *
     * @return maximal size of the in flight window
     * @see org.eclipse.paho.client.mqttv3.MqttConnectOptions#setMaxInflight(int)
     */
    int getMaxInFlight();

    /**
     * Returns the connection timeout in seconds
     *
     * @return connection timeout in seconds
     * @see org.eclipse.paho.client.mqttv3.MqttConnectOptions#setConnectionTimeout(int)
     * @see com.rabbitmq.client.ConnectionFactory#setConnectionTimeout(int)
     */
    int getConnectionTimeout();

    /**
     * Returns the keep alive interval in seconds
     *
     * @return the keep alive interval in seconds
     * @see org.eclipse.paho.client.mqttv3.MqttConnectOptions#setKeepAliveInterval(int)
     */
    int getKeepAliveInterval();

    /**
     * Returns the quality of service
     *
     * @return the quality of service
     */
    int getQos();

    /**
     * Returns true if the connection should be secured otherwise false
     *
     * @return true if connection is secured, false if not
     */
    boolean isConnectionSecured();

    /**
     * Returns the maximal number of threads for the message handler pool.
     *
     * @return the maximal number of thread pool for message handling
     */
    int getMaxThread();

    /**
     * Returns the time limit in seconds for which message processing threads may remain idle before
     * being terminated.
     *
     * @return the keep alive time in seconds for message processing threads
     * @see java.util.concurrent.ThreadPoolExecutor#setKeepAliveTime(long, TimeUnit)
     */
    int getThreadKeepAliveTime();
}
