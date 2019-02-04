package com.celadonsea.messagingframework.security;

import org.junit.Test;

public class CertificateLoaderTest {

	@Test(expected = SecurityException.class)
	public void shouldNotLoadInvalidCertificateFile() {
		CertificateLoader.getKeyStorePasswordPair("anyCert", "anyKey");
	}

	@Test
	public void shouldLoadValidCertificateFile() {
		String dummyCert = this.getClass().getResource("dummyCert").getFile();
		String dummyKey = this.getClass().getResource("dummyKey").getFile();
		CertificateLoader.getKeyStorePasswordPair(dummyCert, dummyKey);
	}
}
