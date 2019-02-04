package com.celadonsea.messagingframework.security;

import java.io.InputStream;

/**
 * Interface to handle certificate and private key as pairs.
 *
 * @author Rafael Revesz
 * @since 1.0
 */
public interface CredentialStore {

	/**
	 * Returns the input stream containing the certificate chain
	 *
	 * @return the input stream containing the certificate chain
	 */
	InputStream getCertificate();

	/**
	 * Returns the input stream containing the private key
	 * @return the input stream containing the private key
	 */
	InputStream getPrivateKey();
}