package com.celadonsea.messagingframework.security;

import com.celadonsea.messagingframework.core.ConnectionException;

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
 */
public class CertificateLoader {

	private CertificateLoader() {
	}

	public static KeyStorePasswordPair getKeyStorePasswordPair(final String certificateFile, final String privateKeyFile) {
		if (certificateFile == null || privateKeyFile == null) {
			throw new ConnectionException("Certificate or private key file missing");
		}

		final PrivateKey privateKey = loadPrivateKeyFromFile(privateKeyFile, null);

		final List<Certificate> certChain = loadCertificatesFromFile(certificateFile);

		return getKeyStorePasswordPair(certChain, privateKey);
	}

	public static KeyStorePasswordPair getKeyStorePasswordPair(
			final InputStream certificateStream,
			final InputStream privateKeyStream) {

		if (certificateStream == null || privateKeyStream == null) {
			throw new ConnectionException("Certificate or private key stream missing");
		}

		final PrivateKey privateKey = loadPrivateKeyFromStream(privateKeyStream, null);

		final List<Certificate> certChain = loadCertificatesFromStream(certificateStream);

		return getKeyStorePasswordPair(certChain, privateKey);
	}

	private static KeyStorePasswordPair getKeyStorePasswordPair(final List<Certificate> certificates, final PrivateKey privateKey) {

		if (certificates == null || privateKey == null)
			throw new ConnectionException("Either certificate chain or private key is missing");

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
			throw new ConnectionException("Failed to create key store", e);
		}

		return new KeyStorePasswordPair(keyStore, keyPassword);
	}

	private static List<Certificate> loadCertificatesFromFile(final String filename) {
		File file = new File(filename);
		if (!file.exists()) {
			throw new ConnectionException("Certificate file: " + filename + " is not found.");
		}
		try {
			return loadCertificatesFromStream(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			throw new ConnectionException("Failed to load certificate file", e);
		}
	}

	private static List<Certificate> loadCertificatesFromStream(final InputStream fileStream) {

		try (BufferedInputStream stream = new BufferedInputStream(fileStream)) {
			final CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
			return (List<Certificate>) certFactory.generateCertificates(stream);
		} catch (IOException | CertificateException e) {
			throw new ConnectionException("Failed to load certificate stream", e);
		}
	}

	private static PrivateKey loadPrivateKeyFromFile(final String filename, final String algorithm) {

		File file = new File(filename);
		if (!file.exists()) {
			throw new ConnectionException("Private key file not found: " + filename);
		}
		try {
			return loadPrivateKeyFromStream(new FileInputStream(file), algorithm);
		} catch (FileNotFoundException e) {
			throw new ConnectionException("Failed to load private key from file " + filename, e);
		}
	}

	private static PrivateKey loadPrivateKeyFromStream(final InputStream fileStream, final String algorithm) {
		PrivateKey privateKey;

		try (DataInputStream stream = new DataInputStream(fileStream)) {
			privateKey = PrivateKeyReader.getPrivateKey(stream, algorithm);
		} catch (IOException | GeneralSecurityException e) {
			throw new ConnectionException("Failed to load private key from stream", e);
		}

		return privateKey;
	}

	public static class KeyStorePasswordPair {
		public final KeyStore keyStore;
		public final String keyPassword;

		KeyStorePasswordPair(KeyStore keyStore, String keyPassword) {
			this.keyStore = keyStore;
			this.keyPassword = keyPassword;
		}
	}

}