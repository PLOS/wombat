package org.ambraproject.wombat.service;

import org.ambraproject.wombat.service.remote.SoaService;
import org.ambraproject.wombat.util.Citations;
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

public class PowerPointServiceImpl implements PowerPointService {
  private static final Logger log = LoggerFactory.getLogger(PowerPointServiceImpl.class);

  @Autowired
  private SoaService soaService;

  @Override
  public SlideShow createPowerPointFile(Map<String, Object> figureMetadata,
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

  private static Dimension constructLetterSizeDimension() {
    // Letter size = 11 inches by 8.5 inches. One inch = 72 pixels.
    return new Dimension(792, 612);
  }

  private byte[] readFigureImage(Map<String, Object> figureMetadata) throws IOException {
    Map<String, Object> thumbnails = (Map<String, Object>) figureMetadata.get("thumbnails");
    Map<String, Object> medium = (Map<String, Object>) thumbnails.get("medium");
    String file = (String) medium.get("file");
    try (InputStream stream = soaService.requestStream("assetfiles/" + file)) {
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
    int imW = dimension.width;
    int imH = dimension.height;

    //add the image to picture and add picture to shape
    Picture picture = new Picture(index);

    // Image box size 750x432 at xy=21,68

    if (imW > 0 && imH > 0) {
      double pgRatio = 750.0 / 432.0;
      double imRatio = (double) imW / (double) imH;
      if (pgRatio >= imRatio) {
        // horizontal center
        int mw = (int) ((double) imW * 432.0 / (double) imH);
        int mx = 21 + (750 - mw) / 2;

        picture.setAnchor(new Rectangle(mx, 68, mw, 432));
      } else {
        // vertical center
        int mh = (int) ((double) imH * 750.0 / (double) imW);
        int my = 68 + (432 - mh) / 2;

        picture.setAnchor(new Rectangle(21, my, 750, mh));
      }
    }

    return picture;
  }

  private static void addTitle(Map<String, Object> figureMetadata, Slide slide) {
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

  private static String getTitleText(Map<String, Object> figureMetadata) {
    String title = (String) figureMetadata.get("title");
    String description = (String) figureMetadata.get("description");
    // TODO: Deal with markup
    return String.format("%s. %s", title, description);
  }

  private TextBox buildCitation(Map<String, Object> figureMetadata, URL articleLink, SlideShow slideShow) {
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

  private String getCitationText(Map<String, Object> figureMetadata) {
    Map<String, Object> parentArticle = getParentArticleMetadata(figureMetadata);
    return Citations.buildCitation(parentArticle);
  }

  private Map<String, Object> getParentArticleMetadata(Map<String, Object> figureMetadata) {
    Map<String, Object> parentArticleSummary = (Map<String, Object>) figureMetadata.get("parentArticle");
    String parentArticleDoi = (String) parentArticleSummary.get("doi");
    try {
      return soaService.requestObject("articles/" + parentArticleDoi, Map.class);
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
    logo.setAnchor(new Rectangle(792 - 5 - dimension.width, 612 - 5 - dimension.height, dimension.width, dimension.height));
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
