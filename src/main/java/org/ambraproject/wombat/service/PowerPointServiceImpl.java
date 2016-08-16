package org.ambraproject.wombat.service;

import io.swagger.annotations.Api;
import org.ambraproject.wombat.service.remote.ArticleApi;
import org.ambraproject.wombat.util.Citations;
import org.ambraproject.wombat.util.TextUtil;
import org.apache.commons.io.IOUtils;
import org.apache.poi.hslf.model.Hyperlink;
import org.apache.poi.hslf.model.Picture;
import org.apache.poi.hslf.model.Slide;
import org.apache.poi.hslf.model.TextBox;
import org.apache.poi.hslf.usermodel.RichTextRun;
import org.apache.poi.hslf.usermodel.SlideShow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PowerPointServiceImpl implements PowerPointService {
  private static final Logger log = LoggerFactory.getLogger(PowerPointServiceImpl.class);

  @Autowired
  private ArticleApi articleApi;

  @Override
  public SlideShow createPowerPointFile(Map<String, ?> figureMetadata,
                                        URL downloadLink,
                                        JournalLogoCallback logoCallback)
      throws IOException {
    //make the new slide
    SlideShow slideShow = new SlideShow();
    slideShow.setPageSize(constructLetterSizeDimension());
    Slide slide = slideShow.createSlide();

    //set the picture box on particular location
    byte[] image = readFigureImage(figureMetadata);
    Picture picture = setPictureBox(image, slideShow);
    slide.addShape(picture);

    addTitle(figureMetadata, slide);

    TextBox pptCitationText = buildCitation(figureMetadata, downloadLink, slideShow);
    slide.addShape(pptCitationText);

    Picture logo = buildLogo(logoCallback, slideShow);
    if (logo != null) {
      slide.addShape(logo);
    }

    return slideShow;
  }

  // Letter size = 11 inches by 8.5 inches. One inch = 72 pixels.
  private static final int LETTER_SIZE_WIDTH = 792;
  private static final int LETTER_SIZE_HEIGHT = 612;

  private static Dimension constructLetterSizeDimension() {
    return new Dimension(LETTER_SIZE_WIDTH, LETTER_SIZE_HEIGHT);
  }

  private byte[] readFigureImage(Map<String, ?> figureMetadata) throws IOException {
    Map<String, ?> thumbnails = (Map<String, ?>) figureMetadata.get("thumbnails");
    Map<String, ?> medium = (Map<String, ?>) thumbnails.get("medium");
    String file = (String) medium.get("file");
    try (InputStream stream = articleApi.requestStream(ApiAddress.builder("assetfiles").addToken(file).build())) {
      return IOUtils.toByteArray(stream);
    }
  }

  /**
   * set the dimension of picture box
   *
   * @param image
   * @param slideShow
   * @return
   * @throws IOException
   */
  private static Picture setPictureBox(byte[] image, SlideShow slideShow) throws IOException {

    int index = slideShow.addPicture(image, Picture.PNG);

    Dimension dimension = getImageDimension(image);

    //get the image size
    int imageWidth = dimension.width;
    int imageHeight = dimension.height;
    double imageRatio = (double) imageWidth / (double) imageHeight;

    //add the image to picture and add picture to shape
    Picture picture = new Picture(index);

    // Image box size 750x432 at xy=21,68
    final int boxWidth = 750;
    final int boxHeight = 432;
    final double boxRatio = (double) boxWidth / (double) boxHeight;
    final int boxHorizontal = 21;
    final int boxVertical = 68;

    if (imageWidth > 0 && imageHeight > 0) {
      Rectangle anchor;
      if (boxRatio >= imageRatio) {
        // horizontal center
        int mw = (int) ((double) boxHeight * imageRatio);
        int mx = boxHorizontal + (boxWidth - mw) / 2;

        anchor = new Rectangle(mx, boxVertical, mw, boxHeight);
      } else {
        // vertical center
        int mh = (int) ((double) boxWidth / imageRatio);
        int my = boxVertical + (boxHeight - mh) / 2;

        anchor = new Rectangle(boxHorizontal, my, boxWidth, mh);
      }
      picture.setAnchor(anchor);
    }

    return picture;
  }

  private static void addTitle(Map<String, ?> figureMetadata, Slide slide) {
    //add the title to slide
    String title = getTitleText(figureMetadata);
    if (!title.isEmpty()) {
      TextBox pptTitle = slide.addTitle();
      pptTitle.setText(title);
      pptTitle.setAnchor(new Rectangle(28, 22, 737, 36));
      RichTextRun rt = pptTitle.getTextRun().getRichTextRuns()[0];
      rt.setFontSize(16);
      rt.setBold(true);
      rt.setAlignment(TextBox.AlignCenter);
    }
  }

  private static final Pattern TITLE_EXTRACTOR = Pattern.compile("<title[^>]*?>(.*?)</title\\s*>");

  private static String getTitleText(Map<String, ?> figureMetadata) {
    String title = TextUtil.sanitizeWhitespace((String) figureMetadata.get("title"));
    String description = TextUtil.sanitizeWhitespace((String) figureMetadata.get("description"));

    /*
     * The description is an excerpt of article XML. Use quick-and-dirty regexes to get the text of the <title> element
     * with internal markup removed. We expect there never to be another nested pair of <title> tags, so we should be
     * able to get away without context-free parsing.
     *
     * If bugs or shortcomings are found, consider using an XML parser instead.
     */

    // Extract title from description
    Matcher titleElement = TITLE_EXTRACTOR.matcher(description);
    if (!titleElement.find()) {
      return title.isEmpty() ? "" : (title + ".");
    }
    String descriptionTitleText = titleElement.group(1);
    descriptionTitleText = TextUtil.removeMarkup(descriptionTitleText);

    return String.format("%s. %s", title, descriptionTitleText);
  }

  private TextBox buildCitation(Map<String, ?> figureMetadata, URL articleLink, SlideShow slideShow) {
    String pptUrl = articleLink.toString();
    TextBox pptCitationText = new TextBox();

    String citation = getCitationText(figureMetadata);
    pptCitationText.setText(citation + "\n" + pptUrl);
    pptCitationText.setAnchor(new Rectangle(35, 513, 723, 26));

    RichTextRun richTextRun = pptCitationText.getTextRun().getRichTextRuns()[0];
    richTextRun.setFontSize(12);

    String text = pptCitationText.getText();
    Hyperlink link = new Hyperlink();
    link.setAddress(pptUrl);
    link.setTitle("click to visit the article page");
    int linkId = slideShow.addHyperlink(link);
    int startIndex = text.indexOf(pptUrl);
    pptCitationText.setHyperlink(linkId, startIndex, startIndex + pptUrl.length());
    return pptCitationText;
  }

  private String getCitationText(Map<String, ?> figureMetadata) {
    Map<String, ?> parentArticle = getParentArticleMetadata(figureMetadata);
    return Citations.buildCitation(parentArticle);
  }

  private Map<String, ?> getParentArticleMetadata(Map<String, ?> figureMetadata) {
    Map<String, ?> parentArticleSummary = (Map<String, ?>) figureMetadata.get("parentArticle");
    String parentArticleDoi = (String) parentArticleSummary.get("doi");
    try {
      return articleApi.requestObject(ApiAddress.builder("articles").addToken(parentArticleDoi).build(), Map.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static Picture buildLogo(JournalLogoCallback logoCallback, SlideShow slideShow) throws IOException {
    byte[] logoImage;
    try (InputStream logoStream = logoCallback.openLogoStream()) {
      if (logoStream == null) {
        // Assume logoCallback logged a warning. It has more info than we do about how the logo should have been found.
        return null;
      }

      // Dump to memory. This is the only way that SlideShow can consume it (other than a java.io.File object),
      // and anyway we would have to read it twice in order to get the dimensions.
      logoImage = IOUtils.toByteArray(logoStream);
    }

    int logoIdx = slideShow.addPicture(logoImage, Picture.PNG);
    Picture logo = new Picture(logoIdx);
    Dimension dimension = getImageDimension(logoImage);

    final int margin = 5;
    int horizontalPosition = LETTER_SIZE_WIDTH - margin - dimension.width;
    int verticalPosition = LETTER_SIZE_HEIGHT - margin - dimension.height;
    logo.setAnchor(new Rectangle(horizontalPosition, verticalPosition, dimension.width, dimension.height));

    return logo;
  }

  /**
   * get the image dimension
   *
   * @param input
   * @return
   */
  private static Dimension getImageDimension(byte[] input) throws IOException {
    try (ImageInputStream in = ImageIO.createImageInputStream(new ByteArrayInputStream(input))) {
      Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
      if (readers.hasNext()) {
        ImageReader reader = readers.next();
        try {
          reader.setInput(in);
          return new Dimension(reader.getWidth(0), reader.getHeight(0));
        } finally {
          reader.dispose();
        }
      } else {
        throw new RuntimeException("ImageIO.getImageReaders returned an empty iterator");
      }
    }
  }

}
