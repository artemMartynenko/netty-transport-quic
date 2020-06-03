package io.netty.chanel.quic.tls;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.List;

public interface CertificateValidator {

  static CertificateValidator defaults() {
    String trustStorePath = System.getProperty("javax.net.ssl.trustStore");
    String trustStorePwd = System.getProperty("javax.net.ssl.trustStorePassword");
    if (trustStorePath == null) {
      trustStorePath = System.getProperty("java.home") + "/lib/security/cacerts";
      trustStorePwd = "changeit";
    }

    final char[] pwd;
    if (trustStorePwd != null) {
      pwd = trustStorePwd.toCharArray();
    } else {
      pwd = null;
    }

    try {
      KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
      ks.load(new FileInputStream(trustStorePath), pwd);
      return new DefaultCertificateValidator(ks);
    } catch (final KeyStoreException
        | NoSuchAlgorithmException
        | CertificateException
        | IOException e) {
      throw new RuntimeException(e);
    }
  }

  boolean validate(List<byte[]> certificates);
}
