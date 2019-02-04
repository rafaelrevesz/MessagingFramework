package com.celadonsea.messagingframework.security;

import org.springframework.util.Assert;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.List;

/**
 * This is a helper class to facilitate reading of the configurations and
 * certificate from the resource files.
 *
 * @author Rafael Revesz
 * @since 1.0
 */
public class CertificateLoader {

	/**
	 * Default constructor is hidden to enforce using static methods
	 */
	private CertificateLoader() {
	}

	/**
	 * Creates and returns a key store with a certificate and a private key
	 * loaded from files with the given file names.
	 *
	 * @param certificateFile the name of the file which contains the certificate
	 * @param privateKeyFile the name of the file which contains the private key
	 * @return the new key store
	 * @throws IllegalArgumentException if certificate file or private key file is null
	 * @throws SecurityException if the key store creation fails
	 */
	public static KeyStorePasswordPair getKeyStorePasswordPair(final String certificateFile, final String privateKeyFile) {
		Assert.notNull(certificateFile, "Certificate file must not be null");
		Assert.notNull(privateKeyFile, "Private key file must not be null");

		final PrivateKey privateKey = loadPrivateKeyFromFile(privateKeyFile, null);

		final List<Certificate> certChain = loadCertificatesFromFile(certificateFile);

		return getKeyStorePasswordPair(certChain, privateKey);
	}

	/**
	 * Creates and returns a key store with a certificate and a private key read from
	 * the given input streams.
	 *
	 * @param certificateStream input stream which contains the certificate
	 * @param privateKeyStream input stream which contains the private key
	 * @return the new key store
	 * @throws IllegalArgumentException if certificate stream or private key stream is null
	 * @throws SecurityException if the key store creation fails
	 */
	public static KeyStorePasswordPair getKeyStorePasswordPair(
			final InputStream certificateStream,
			final InputStream privateKeyStream) {

		Assert.notNull(certificateStream, "Certificate stream must not be null");
		Assert.notNull(privateKeyStream, "Private key stream must not be null");

		final PrivateKey privateKey = loadPrivateKeyFromStream(privateKeyStream, null);

		final List<Certificate> certChain = loadCertificatesFromStream(certificateStream);

		return getKeyStorePasswordPair(certChain, privateKey);
	}

	/**
	 * Creates and returns a key store with a certificate chain and a private key.
	 *
	 * @param certificates certificate chain
	 * @param privateKey private key
	 * @return the new key store
	 * @throws IllegalArgumentException if certificate chain or private key is null
	 * @throws SecurityException if the key store creation fails
	 */
	private static KeyStorePasswordPair getKeyStorePasswordPair(final List<Certificate> certificates, final PrivateKey privateKey) {

		Assert.notNull(certificates, "Certificate chain must not be null");
		Assert.notNull(privateKey, "Private key must not be null");

		KeyStore keyStore;
		String keyPassword;
		try {
			keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
			keyStore.load(null);

			// randomly generated key password for the key in the KeyStore
			keyPassword = new BigInteger(128, new SecureRandom()).toString(32);

			Certificate[] certChain = new Certificate[certificates.size()];
			certChain = certificates.toArray(certChain);
			keyStore.setKeyEntry("alias", privateKey, keyPassword.toCharArray(), certChain);
		} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
			throw new SecurityException("Failed to create key store", e);
		}

		return new KeyStorePasswordPair(keyStore, keyPassword);
	}

	/**
	 * Loads and returns a certificate chain with the given name.
	 *
	 * @param filename the name of the file which contains the certificate chain
	 * @return the certificate chain
	 * @throws SecurityException if the file does not exists or the certificate chain cannot be load
	 */
	private static List<Certificate> loadCertificatesFromFile(final String filename) {
		File file = new File(filename);
		if (!file.exists()) {
			throw new SecurityException("Certificate file: " + filename + " is not found.");
		}
		try {
			return loadCertificatesFromStream(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			throw new SecurityException("Failed to load certificate file", e);
		}
	}

	/**
	 * Reads and returns a certificate chain from the given input stream.
	 *
	 * @param fileStream the input stream which contains the certificate chain
	 * @return the certificate chain
	 * @throws SecurityException if the read of the stream fails
	 */
	private static List<Certificate> loadCertificatesFromStream(final InputStream fileStream) {

		try (BufferedInputStream stream = new BufferedInputStream(fileStream)) {
			final CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
			return (List<Certificate>) certFactory.generateCertificates(stream);
		} catch (IOException | CertificateException e) {
			throw new SecurityException("Failed to load certificate stream", e);
		}
	}

	/**
	 * Loads and returns the private key from the file with the given name.
	 *
	 * @param filename the name of the file which contains the private key
	 * @param algorithm the coding algorithm of the private key
	 * @return the private key
	 * @throws SecurityException if the file not exists or the load fails
	 */
	private static PrivateKey loadPrivateKeyFromFile(final String filename, final String algorithm) {

		File file = new File(filename);
		if (!file.exists()) {
			throw new SecurityException("Private key file not found: " + filename);
		}
		try {
			return loadPrivateKeyFromStream(new FileInputStream(file), algorithm);
		} catch (FileNotFoundException e) {
			throw new SecurityException("Failed to load private key from file " + filename, e);
		}
	}

	/**
	 * Loads and returns the private key from the given input stream with the given algorithm.
	 *
	 * @param fileStream the input stream which contains the private key
	 * @param algorithm the coding algorithm of the private key
	 * @return the private key
	 * @throws SecurityException if the load fails
	 */
	private static PrivateKey loadPrivateKeyFromStream(final InputStream fileStream, final String algorithm) {
		PrivateKey privateKey;

		try (DataInputStream stream = new DataInputStream(fileStream)) {
			privateKey = PrivateKeyReader.getPrivateKey(stream, algorithm);
		} catch (IOException | GeneralSecurityException e) {
			throw new SecurityException("Failed to load private key from stream", e);
		}

		return privateKey;
	}

	/**
	 * Encapsulates the java key store and the corresponding password.
	 *
	 * @author Rafael Revesz
	 * @since 1.0
	 */
	public static class KeyStorePasswordPair {
		/**
		 * The java key store
		 */
		public final KeyStore keyStore;
		/**
		 * The raw password for the key store
		 */
		public final String keyPassword;

		/**
		 * Constructs a key store pair with the given java key store and pasword.
		 *
		 * @param keyStore the java key store
		 * @param keyPassword the password for the key store
		 */
		KeyStorePasswordPair(KeyStore keyStore, String keyPassword) {
			this.keyStore = keyStore;
			this.keyPassword = keyPassword;
		}
	}

}