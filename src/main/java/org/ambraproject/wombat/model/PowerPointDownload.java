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

package org.ambraproject.wombat.model;

import com.google.common.io.ByteSource;
import org.ambraproject.wombat.util.Citations;
import org.ambraproject.wombat.util.TextUtil;
import org.apache.poi.hslf.model.Hyperlink;
import org.apache.poi.hslf.model.Picture;
import org.apache.poi.hslf.model.Slide;
import org.apache.poi.hslf.model.TextBox;
import org.apache.poi.hslf.usermodel.RichTextRun;
import org.apache.poi.hslf.usermodel.SlideShow;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PowerPointDownload {
  private static final Logger log = LogManager.getLogger(PowerPointDownload.class);

  /**
   * Indicate that a journal's theme does not provide a logo file. In this case, it is best for the callback to log a
   * warning that describes the file missing from the theme, because the invoker won't know anything about the theme
   * backing this callback object.
   *
   * @return the input stream, or {@code null} if no logo is available for the journal
   * @throws IOException
   */
  public static class JournalHasNoLogoException extends RuntimeException {
    public JournalHasNoLogoException(String message) {
      super(message);
    }
  }

  private final Map<String, ?> parentArticleMetadata;
  private final List<Map<String, ?>> parentArticleAuthors;
  private final URL downloadLink;
  private final String figureTitle;
  private final String figureDescription;
  private final ByteSource figureImageSource;
  private final ByteSource journalLogoSource;

  public PowerPointDownload(Map<String, ?> parentArticleMetadata, List<Map<String, ?>> parentArticleAuthors,
                            URL downloadLink,
                            String figureTitle, String figureDescription,
                            ByteSource figureImageSource, ByteSource journalLogoSource) {
    this.parentArticleMetadata = Collections.unmodifiableMap(parentArticleMetadata);
    this.parentArticleAuthors = Collections.unmodifiableList(parentArticleAuthors);
    this.downloadLink = Objects.requireNonNull(downloadLink);
    this.figureTitle = TextUtil.sanitizeWhitespace(figureTitle);
    this.figureDescription = TextUtil.sanitizeWhitespace(figureDescription);
    this.figureImageSource = Objects.requireNonNull(figureImageSource);
    this.journalLogoSource = Objects.requireNonNull(journalLogoSource);
  }

  /**
   * Create a Microsoft PowerPoint slide show object that presents a figure.
   *
   * @return the slide show object
   * @throws IOException
   */
  public SlideShow createPowerPointFile()
      throws IOException {
    //make the new slide
    SlideShow slideShow = new SlideShow();
    slideShow.setPageSize(constructLetterSizeDimension());
    Slide slide = slideShow.createSlide();

    //set the picture box on particular location
    byte[] image = figureImageSource.read();
    Picture picture = setPictureBox(image, slideShow);
    slide.addShape(picture);

    addTitle(slide);

    TextBox pptCitationText = buildCitation(slideShow);
    slide.addShape(pptCitationText);

    Picture logo = buildLogo(slideShow);
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

  private void addTitle(Slide slide) {
    //add the title to slide
    String title = getTitleText();
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

  private String getTitleText() {
    /*
     * The description is an excerpt of article XML. Use quick-and-dirty regexes to get the text of the <title> element
     * with internal markup removed. We expect there never to be another nested pair of <title> tags, so we should be
     * able to get away without context-free parsing.
     *
     * If bugs or shortcomings are found, consider using an XML parser instead.
     */

    // Extract title from description
    Matcher titleElement = TITLE_EXTRACTOR.matcher(figureDescription);
    if (!titleElement.find()) {
      return figureTitle.isEmpty() ? "" : (figureTitle + ".");
    }
    String descriptionTitleText = titleElement.group(1);
    descriptionTitleText = TextUtil.removeMarkup(descriptionTitleText);

    return String.format("%s. %s", figureTitle, descriptionTitleText);
  }

  private TextBox buildCitation(SlideShow slideShow) {
    String pptUrl = downloadLink.toString();
    TextBox pptCitationText = new TextBox();

    String citation = Citations.buildCitation(parentArticleMetadata, parentArticleAuthors);
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

  private Picture buildLogo(SlideShow slideShow) throws IOException {
    byte[] logoImage;
    try {
      // Dump to memory. This is the only way that SlideShow can consume it (other than a java.io.File object),
      // and anyway we would have to read it twice in order to get the dimensions.
      logoImage = journalLogoSource.read();
    } catch (JournalHasNoLogoException e) {
      log.warn(e.getMessage());
      return null;
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
