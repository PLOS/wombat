package org.ambraproject.wombat.service;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.Multipart;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.Objects;
import java.util.Optional;

public class EmailMessage {
  private static final Logger log = LoggerFactory.getLogger(EmailMessage.class);

  private final ImmutableSet<InternetAddress> toEmailAddresses;
  private final Optional<InternetAddress> bccAddress;
  private final InternetAddress senderAddress;
  private final String subject;
  private final Multipart content;
  private final String encoding;

  private EmailMessage(Builder builder) {
    this.toEmailAddresses = builder.toEmailAddresses.build();
    this.bccAddress = Optional.ofNullable(builder.bccAddress);
    this.senderAddress = Objects.requireNonNull(builder.senderAddress);
    this.subject = Objects.requireNonNull(builder.subject);
    this.content = Objects.requireNonNull(builder.content);
    this.encoding = Objects.requireNonNull(builder.encoding);
  }

  public static InternetAddress createAddress(String name, String email) {
    email = email.trim();
    Preconditions.checkArgument(!email.isEmpty());
    try {
      return Strings.isNullOrEmpty(name) ? new InternetAddress(email) : new InternetAddress(email, name.trim());
    } catch (AddressException | UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  public void send(JavaMailSender mailSender) {
    for (InternetAddress toEmailAddress : toEmailAddresses) {
      mailSender.send((MimeMessage mimeMessage) -> {
        MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, encoding);
        message.setTo(toEmailAddress);

        if (bccAddress.isPresent()) {
          message.setBcc(bccAddress.get());
        }

        message.setFrom(senderAddress);
        message.setSubject(subject);

        mimeMessage.setContent(content);
      });

      log.debug("Mail sent to: {}", toEmailAddress);
    }
  }


  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private final ImmutableSet.Builder<InternetAddress> toEmailAddresses = ImmutableSet.builder();
    private InternetAddress bccAddress;
    private InternetAddress senderAddress;
    private String subject;
    private Multipart content;
    private String encoding;

    private Builder() {
    }

    public Builder addToEmailAddress(InternetAddress toEmailAddress) {
      this.toEmailAddresses.add(toEmailAddress);
      return this;
    }

    public Builder addToEmailAddresses(Iterable<? extends InternetAddress> toEmailAddresses) {
      this.toEmailAddresses.addAll(toEmailAddresses);
      return this;
    }

    public Builder setBccAddress(InternetAddress bccAddress) {
      this.bccAddress = bccAddress;
      return this;
    }

    public Builder setSenderAddress(InternetAddress senderAddress) {
      this.senderAddress = senderAddress;
      return this;
    }

    public Builder setSubject(String subject) {
      this.subject = subject;
      return this;
    }

    public Builder setContent(Multipart content) {
      this.content = content;
      return this;
    }

    public Builder setEncoding(String encoding) {
      this.encoding = encoding;
      return this;
    }

    public EmailMessage build() {
      return new EmailMessage(this);
    }
  }

}
