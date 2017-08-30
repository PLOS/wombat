package org.ambraproject.wombat.util.captcha;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Collections.shuffle;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.hash.Hashing;
import com.google.common.io.Resources;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;


/**
 * A "visual" captcha builder.
 *
 * @see https://github.com/bdotzour/visualCaptcha-java
 */
public class CaptchaBuilder implements InitializingBean {

  private static final Logger LOG = LoggerFactory.getLogger(CaptchaBuilder.class);

  public static final String DEFAULT_AUDIO_PATH = "captcha/audios/";
  public static final String DEFAULT_IMAGE_PATH = "captcha/images/";

  private static final int DEFAULT_NUM_IMAGES = 5;
  private static final String AUDIOS_LIST = "captcha/audios.json";
  private static final String IMAGES_LIST = "captcha/images.json";

  /** Enum to describe an audio file type (i.e. MP3). */
  public enum AudioType {
    MP3, OGG
  };

  /**
   * Hash the value, using the specified salt.
   *
   * @param somethingToHash The value to hash
   * @param salt The salt to use for hashing
   *
   * @return The hashed value
   */
  private static String hash(String somethingToHash, String salt) {
    return Hashing.md5().hashString((somethingToHash + salt), Charsets.UTF_8).toString();
  }

  private String audioPath = DEFAULT_AUDIO_PATH;
  private ImmutableList<CaptchaAnswer> audios;

  private String imagesPath = DEFAULT_IMAGE_PATH;
  private ImmutableList<CaptchaAnswer> images;

  private Random rand = new Random();

  /** Sets the audio endpoint. */
  @Value("${captcha.audioDir:'captcha/audios'}")
  public void setAudioPath(String audioPath) {
    checkNotNull(audioPath, "Audio path cannot be null");
    checkArgument(!audioPath.isEmpty(), "Must specify audio path");
    if (audioPath.endsWith("/")) {
      this.audioPath = audioPath;
    } else {
      this.audioPath = audioPath + '/';
    }
  }

  /** Sets the images endpoint. */
  @Value("${captcha.imageDir:'captcha/images'}")
  public void setImagesPath(String imagesPath) {
    checkNotNull(imagesPath, "Images path cannot be null");
    checkArgument(!imagesPath.isEmpty(), "Must specify images path");
    if (imagesPath.endsWith("/")) {
      this.imagesPath = imagesPath;
    } else {
      this.imagesPath = imagesPath + '/';
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    try {
      loadImages();
      loadAudios();
    } catch (RuntimeException exception) {
      throw new BeanInitializationException(
          "Unable to initialize CaptchaBuilder. Failed to load resources.", exception);
    }
  }

  /** Loads the images from file. */
  @SuppressWarnings("serial")
  private void loadImages() {
    final URL imagesUrl = Resources.getResource(IMAGES_LIST);
    try (final Reader reader = new InputStreamReader(imagesUrl.openStream())) {
      final Gson gson = new GsonBuilder()
          .registerTypeAdapter(CaptchaAnswer.class, new CaptchaAnswerDeserializer()).create();
      final List<CaptchaAnswer> result =
          gson.fromJson(reader, new TypeToken<List<CaptchaAnswer>>() {}.getType());
      images = ImmutableList.copyOf(result);
    } catch (IOException exception) {
      LOG.info("Caught exception trying to load images.", exception);
    }
  }

  /** Loads the audio from file. */
  @SuppressWarnings("serial")
  private void loadAudios() {
    final URL audioUrl = Resources.getResource(AUDIOS_LIST);
    try (final Reader reader = new InputStreamReader(audioUrl.openStream())) {
      final Gson gson = new GsonBuilder()
          .registerTypeAdapter(CaptchaAnswer.class, new CaptchaAnswerDeserializer()).create();
      final List<CaptchaAnswer> result =
          gson.fromJson(reader, new TypeToken<List<CaptchaAnswer>>() {}.getType());
      audios = ImmutableList.copyOf(result);
    } catch (IOException exception) {
      LOG.info("Caught exception trying to load audio.", exception);
    }
  }

  /** Returns the list of available audio files. */
  List<CaptchaAnswer> getAllAudio() {
    return audios;
  }

  /** Returns the list of available images. */
  List<CaptchaAnswer> getAllImages() {
    return images;
  }

  /** Returns the captcha challenge using the default number of images. */
  public CaptchaInfo createChallenge() {
    return createChallenge(DEFAULT_NUM_IMAGES);
  }

  /**
   * Generates a captcha challenge for the given image count.
   *
   * @param imageCount The number of images to include in the challenge
   *
   * @return The list of images to use as the captcha challenge.
   */
  public CaptchaInfo createChallenge(int imageCount) {
    final String salt = UUID.randomUUID().toString();

    final List<CaptchaAnswer> choices = getRandomCaptchaOptions(imageCount, salt);
    final CaptchaAnswer validChoice = choices.get(rand.nextInt(imageCount));
    final CaptchaAnswer audioOption = getRandomCaptchaAudio(salt);

    final CaptchaInfo captchaInfo = CaptchaInfo.builder().setFieldName(validChoice.value())
        .setValidChoice(validChoice.obfuscatedName())
        .setAudioFieldName(audioOption.obfuscatedName())
        .setAudioAnswer(audioOption).setChoices(choices).build();
    return captchaInfo;
  }

  /**
   * Builds the data required to display the captcha challenge.
   *
   * @param captchaInfo The captcha challenge info
   *
   * @return The data required to display the captcha challenge
   */
  public CaptchaFrontEndData build(CaptchaInfo captchaInfo) {
    checkNotNull(captchaInfo, "CatchaInfo cannot be null");

    final String fieldName = captchaInfo.fieldName();
    final String audioFieldName = captchaInfo.audioFieldName();
    final String validChoice = captchaInfo.validChoice();
    final List<CaptchaAnswer> choices = captchaInfo.choicesAsList();
    final List<String> frontEndOptions = new ArrayList<>(choices.size());
    for (CaptchaAnswer choice : choices) {
      frontEndOptions.add(choice.obfuscatedName());
    }
    final CaptchaFrontEndData frontendData =
        CaptchaFrontEndData.builder().setImageFieldName(validChoice).setImageName(fieldName)
            .setValues(frontEndOptions).setAudioFieldName(audioFieldName).build();
    return frontendData;
  }

  /** Returns a random list of images, chosen from the available images. */
  private List<CaptchaAnswer> getRandomCaptchaOptions(int numberOfChoices, String salt) {
    final List<CaptchaAnswer> options = new ArrayList<>(images);
    shuffle(options);

    final List<CaptchaAnswer> choices = new ArrayList<>(numberOfChoices);
    for (CaptchaAnswer answer : options.subList(0, numberOfChoices)) {
      final String obfuscatedName = hash(answer.value(), salt);
      choices.add(CaptchaAnswer.builder().setValue(answer.value()).setObfuscatedName(obfuscatedName)
          .setPath(answer.path()).build());
    }
    shuffle(choices);
    return choices;
  }

  /** Returns a random list of audio, chosen from the available audio. */
  private CaptchaAnswer getRandomCaptchaAudio(String salt) {
    final List<CaptchaAnswer> options = new ArrayList<>(audios);
    shuffle(options);

    final CaptchaAnswer audioOption = options.get(rand.nextInt(audios.size()));
    final String obfuscatedName = hash(audioOption.value(), salt);
    final CaptchaAnswer audioAnswer = CaptchaAnswer.builder().setValue(audioOption.value())
        .setObfuscatedName(obfuscatedName).setPath(audioOption.path()).build();
    return audioAnswer;
  }

  /** Returns the image path. */
  public URL getImagePath(CaptchaAnswer answer) {
    return getImagePath(answer, false /* useRetina */);
  }

  /** Returns the retina image path. */
  public URL getRetinaImagePath(CaptchaAnswer answer) {
    return getImagePath(answer, true /* useRetina */);
  }

  /**
   * Get the image path.
   *
   * @param answer
   * @param useRetina
   *
   * @return The image path
   */
  private URL getImagePath(CaptchaAnswer answer, boolean useRetina) {
    String fileName = useRetina ? answer.path().replace(".png", "@2x.png") : answer.path();
    final URL imageUrl = Resources.getResource(imagesPath + fileName);
    return imageUrl;
  }

  /**
   * Get the audio path.
   *
   * @param answer The captcha answer
   * @param audioType The {@link CaptchaBuilder.AudioType audio type}
   *
   * @return The audio path
   */
  public URL getAudioPath(CaptchaAnswer answer, AudioType audioType) {
    String path = answer.path();
    if (audioType.equals(AudioType.OGG)) {
      path = path.replace(".mp3", ".ogg");
    }
    final URL audioUrl = Resources.getResource(audioPath + path);
    return audioUrl;
  }

  /** Class to use in de-serializing JSON to a <b>CaptchaAnswer</b>. */
  private class CaptchaAnswerDeserializer implements JsonDeserializer<CaptchaAnswer> {

    @Override
    public CaptchaAnswer deserialize(JsonElement json, Type typeOfT,
        JsonDeserializationContext context) throws JsonParseException {
      final JsonObject data = json.getAsJsonObject();
      LOG.debug("Adding Captcha asset: {}", data);
      final JsonPrimitive value = data.getAsJsonPrimitive("value");
      final JsonPrimitive path = data.getAsJsonPrimitive("path");
      return CaptchaAnswer.builder().setValue(value.getAsString()).setPath(path.getAsString())
          .build();
    }
  }
}
