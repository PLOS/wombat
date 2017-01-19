/*
 * Copyright (c) 2017 Public Library of Science
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

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
