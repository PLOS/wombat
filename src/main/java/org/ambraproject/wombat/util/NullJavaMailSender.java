package org.ambraproject.wombat.util;

import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

import javax.mail.internet.MimeMessage;
import java.io.InputStream;

/**
 * A dummy singleton bean to be wired in place of an actual JavaMailSender in case no host for a mail server is
 * configured. It prevents errors on start-up, but throws an exception if it is used.
 * <p>
 * This is useful for dev/testing environments. It also is fine if all themes disable all functionality that would cause
 * an email to be sent.
 */
public enum NullJavaMailSender implements JavaMailSender {
  INSTANCE;

  public static class MailSenderNotConfiguredException extends RuntimeException {
    private MailSenderNotConfiguredException() {
    }
  }

  @Deprecated
  @Override
  public MimeMessage createMimeMessage() {
    throw new MailSenderNotConfiguredException();
  }

  @Deprecated
  @Override
  public MimeMessage createMimeMessage(InputStream contentStream) throws MailException {
    throw new MailSenderNotConfiguredException();
  }

  @Deprecated
  @Override
  public void send(MimeMessage mimeMessage) throws MailException {
    throw new MailSenderNotConfiguredException();
  }

  @Deprecated
  @Override
  public void send(MimeMessage... mimeMessages) throws MailException {
    throw new MailSenderNotConfiguredException();
  }

  @Deprecated
  @Override
  public void send(MimeMessagePreparator mimeMessagePreparator) throws MailException {
    throw new MailSenderNotConfiguredException();
  }

  @Deprecated
  @Override
  public void send(MimeMessagePreparator... mimeMessagePreparators) throws MailException {
    throw new MailSenderNotConfiguredException();
  }


  @Deprecated
  @Override
  public void send(SimpleMailMessage simpleMessage) throws MailException {
    throw new MailSenderNotConfiguredException();
  }

  @Deprecated
  @Override
  public void send(SimpleMailMessage... simpleMessages) throws MailException {
    throw new MailSenderNotConfiguredException();
  }
}
