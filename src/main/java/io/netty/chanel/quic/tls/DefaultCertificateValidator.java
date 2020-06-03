package io.netty.chanel.quic.tls;

import com.google.common.collect.ImmutableList;

import java.io.ByteArrayInputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.*;
import java.util.List;

public class DefaultCertificateValidator implements CertificateValidator {

  private final KeyStore truststore;

  public DefaultCertificateValidator(final KeyStore truststore) {
    this.truststore = truststore;
  }

  @Override
  public boolean validate(final List<byte[]> certificates) {
    try {
      final CertificateFactory cf = CertificateFactory.getInstance("X.509");

      final List<X509Certificate> certlist =
          certificates
              .stream()
              .map(
                  bytes -> {
                    try {
                      return (X509Certificate)
                          cf.generateCertificate(new ByteArrayInputStream(bytes));
                    } catch (CertificateException e) {
                      throw new RuntimeException(e);
                    }
                  })
              .collect(ImmutableList.toImmutableList());

      // Check the chain
      final CertPath cp = cf.generateCertPath(certlist);

      final PKIXParameters params = new PKIXParameters(truststore);
      params.setRevocationEnabled(false);
      final CertPathValidator cpv =
          CertPathValidator.getInstance(CertPathValidator.getDefaultType());
      cpv.validate(cp, params);
      return true;
    } catch (final CertPathValidatorException e) {
      return false;
    } catch (final GeneralSecurityException e) {
      throw new RuntimeException(e);
    }
  }
}
