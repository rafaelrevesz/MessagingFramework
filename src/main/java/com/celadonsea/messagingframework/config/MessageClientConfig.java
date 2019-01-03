package com.celadonsea.messagingframework.config;

public interface MessageClientConfig {

    String getClientType();

    String getClientId();

    String getBrokerUrl();

    int getMaxInFlight();

    int getConnectionTimeout();

    int getKeepAliveInterval();

    int getQos();

    boolean isConnectionSecured();
}
