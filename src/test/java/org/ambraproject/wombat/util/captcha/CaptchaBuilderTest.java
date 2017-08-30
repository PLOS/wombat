package org.ambraproject.wombat.util.captcha;

import static com.google.common.truth.Truth.assertThat;

import java.net.URL;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;

public class CaptchaBuilderTest {

  private static final Logger LOG = LoggerFactory.getLogger(CaptchaBuilderTest.class);

  private CaptchaBuilder captchaBuilder;

  /**
   * Set up the dependencies needed for the test.
   *
   * @throws Exception if failed to initialize Captcha builder
   */
  @BeforeTest
  public void setUp() throws Exception {
    captchaBuilder = new CaptchaBuilder();
    captchaBuilder.afterPropertiesSet();
  }

  /** Test successful loading of Captcha assets. */
  @Test
  public void loadAssetsShouldSucceed() {
    final List<CaptchaAnswer> images = captchaBuilder.getAllImages();
    final List<CaptchaAnswer> audioFiles = captchaBuilder.getAllAudio();
    assertThat(images).isNotNull();
    assertThat(images).hasSize(37);
    assertThat(audioFiles).isNotNull();
    assertThat(audioFiles).hasSize(20);
  }

  /** Test successful creation of Captcha challenge. */
  @Test
  public void createChallengeShouldSucceed() {
    final int imageCount = 3;
    final CaptchaInfo challenge = captchaBuilder.createChallenge(imageCount);
    LOG.info("**** createChallengeShouldSucceed: challenge: {}", challenge);
    final CaptchaFrontEndData frontEndData = captchaBuilder.build(challenge);
    LOG.info("**** createChallengeShouldSucceed: data: {}", frontEndData);

    final ImmutableMap<String, CaptchaAnswer> expectedChoices = challenge.choices();
    assertThat(expectedChoices).hasSize(imageCount);
    assertThat(expectedChoices).containsKey(frontEndData.imageFieldName());

    final CaptchaAnswer imageChoice = expectedChoices.get(frontEndData.imageFieldName());
    final URL imagePath = captchaBuilder.getImagePath(imageChoice);
    assertThat(imageChoice.value()).isEqualTo(frontEndData.imageName());
    assertThat(imagePath.toString()).endsWith(imageChoice.path());

    final CaptchaAnswer audioChoice = challenge.audioAnswer();
    assertThat(frontEndData.audioFieldName()).isEqualTo(audioChoice.obfuscatedName());
    final URL audioPath = captchaBuilder.getAudioPath(audioChoice, CaptchaBuilder.AudioType.MP3);
    assertThat(audioPath.toString()).endsWith(audioChoice.path());
  }
}
