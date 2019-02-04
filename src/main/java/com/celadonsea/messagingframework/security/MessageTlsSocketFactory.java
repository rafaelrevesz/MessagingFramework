package com.celadonsea.messagingframework.security;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

/**
 * Socket factory for securing the message communication protocol.
 *
 * @author Rafael Revesz
 * @since 1.0
 */
public class MessageTlsSocketFactory extends SSLSocketFactory {

	/**
	 * SSL socket factory
	 */
	private final SSLSocketFactory sslSocketFactory;

	/**
	 * Constructs the socket factory with the key store pair
	 *
	 * @param keyStorePasswordPair contains the java key store and the corresponding password
	 * @throws SecurityException if the socket creation fails
	 */
	public MessageTlsSocketFactory(CertificateLoader.KeyStorePasswordPair keyStorePasswordPair) {
		try {
			SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
			KeyManagerFactory managerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			managerFactory.init(keyStorePasswordPair.keyStore, keyStorePasswordPair.keyPassword.toCharArray());
			sslContext.init(managerFactory.getKeyManagers(), null, null);

			sslSocketFactory = sslContext.getSocketFactory();
		} catch (NoSuchAlgorithmException | UnrecoverableKeyException | KeyStoreException | KeyManagementException e) {
			throw new SecurityException(e);
		}
	}

	/**
	 * Returns the default cipher suites.
	 * @return the default cipher suites
	 */
	@Override
	public String[] getDefaultCipherSuites() {
		return sslSocketFactory.getDefaultCipherSuites();
	}

	/**
	 * Returns the supported cipher suites.
	 * @return the supported cipher suites
	 */
	@Override
	public String[] getSupportedCipherSuites() {
		return sslSocketFactory.getSupportedCipherSuites();
	}

	/**
	 * Creates and returns a TLSv1.2 socket with the given parameters.
	 * @param socket the source socket
	 * @param host the destination host
	 * @param port the destination port
	 * @param autoClose true if the socket should be closed automatically
	 * @return the new socket
	 * @throws IOException if the socket creation fails
	 */
	@Override
	public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException {
		return socketWithTls12(sslSocketFactory.createSocket(socket, host, port, autoClose));
	}

	/**
	 * Creates and returns a TLSv1.2 socket with the given parameters.
	 *
	 * @param host the destination host
	 * @param port the destination port
	 * @return the new socket
	 * @throws IOException if the socket creation fails
	 */
	@Override
	public Socket createSocket(String host, int port) throws IOException {
		return socketWithTls12(sslSocketFactory.createSocket(host, port));
	}

	/**
	 * Creates and returns a TLSv1.2 socket with the given parameters.
	 *
	 * @param host the destination host
	 * @param port the destination port
	 * @param localAddress the local address
	 * @param localPort the local port
	 * @return the new socket
	 * @throws IOException if the socket creation fails
	 */
	@Override
	public Socket createSocket(String host, int port, InetAddress localAddress, int localPort) throws IOException {
		return socketWithTls12(sslSocketFactory.createSocket(host, port, localAddress, localPort));
	}

	/**
	 * Creates and returns a TLSv1.2 socket with the given parameters.
	 *
	 * @param address the destination address
	 * @param port the destination port
	 * @return the new socket
	 * @throws IOException if the socket creation fails
	 */
	@Override
	public Socket createSocket(InetAddress address, int port) throws IOException {
		return socketWithTls12(sslSocketFactory.createSocket(address, port));
	}

	/**
	 * Creates and returns a TLSv1.2 socket with the given parameters.
	 *
	 * @param address the destination address
	 * @param port the destination port
	 * @param localAddress the local address
	 * @param localPort the local port
	 * @return the new socket
	 * @throws IOException if the socket creation fails
	 */
	@Override
	public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
		return socketWithTls12(sslSocketFactory.createSocket(address, port, localAddress, localPort));
	}

	/**
	 * Configures the SSL socket with the endpoint identification algorithm and
	 * enables the TLSv1.2 protocol.
	 * @param socket
	 * @return
	 */
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
