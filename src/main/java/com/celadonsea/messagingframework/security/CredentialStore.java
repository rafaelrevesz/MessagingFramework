package com.celadonsea.messagingframework.security;

import java.io.InputStream;

public interface CredentialStore {

	InputStream getCertificate();

	InputStream getPrivateKey();
}