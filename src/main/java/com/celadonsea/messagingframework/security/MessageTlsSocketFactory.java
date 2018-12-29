package com.celadonsea.messagingframework.security;

import com.celadonsea.messagingframework.core.ConnectionException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

public class MessageTlsSocketFactory extends SSLSocketFactory {

	private final SSLSocketFactory sslSocketFactory;

	public MessageTlsSocketFactory(KeyStore keyStore, String keyPassword) {
		try {
			SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
			KeyManagerFactory managerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			managerFactory.init(keyStore, keyPassword.toCharArray());
			sslContext.init(managerFactory.getKeyManagers(), null, null);

			sslSocketFactory = sslContext.getSocketFactory();
		} catch (NoSuchAlgorithmException | UnrecoverableKeyException | KeyStoreException | KeyManagementException e) {
			throw new ConnectionException(e);
		}
	}

	@Override
	public String[] getDefaultCipherSuites() {
		return sslSocketFactory.getDefaultCipherSuites();
	}

	@Override
	public String[] getSupportedCipherSuites() {
		return sslSocketFactory.getSupportedCipherSuites();
	}

	@Override
	public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException {
		return socketWithTls12(sslSocketFactory.createSocket(socket, host, port, autoClose));
	}

	@Override
	public Socket createSocket(String host, int port) throws IOException {
		return socketWithTls12(sslSocketFactory.createSocket(host, port));
	}

	@Override
	public Socket createSocket(String host, int port, InetAddress localAddress, int localPort) throws IOException {
		return socketWithTls12(sslSocketFactory.createSocket(host, port, localAddress, localPort));
	}

	@Override
	public Socket createSocket(InetAddress host, int port) throws IOException {
		return socketWithTls12(sslSocketFactory.createSocket(host, port));
	}

	@Override
	public Socket createSocket(InetAddress host, int port, InetAddress localAddress, int localPort) throws IOException {
		return socketWithTls12(sslSocketFactory.createSocket(host, port, localAddress, localPort));
	}

	private Socket socketWithTls12(Socket socket) {
		if (socket instanceof SSLSocket) {
			((SSLSocket) socket).setEnabledProtocols(new String[]{"TLSv1.2"});
			SSLParameters sslParams = new SSLParameters();
			sslParams.setEndpointIdentificationAlgorithm("HTTPS");
			((SSLSocket) socket).setSSLParameters(sslParams);
		}
		return socket;
	}
}
