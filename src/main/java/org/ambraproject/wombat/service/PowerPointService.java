package org.ambraproject.wombat.service;

import org.apache.poi.hslf.usermodel.SlideShow;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

public interface PowerPointService {

  /**
   * Represents access to the logo image for a particular journal.
   */
  public static interface JournalLogoCallback {
    /**
     * Open a stream to the logo image file for the journal.
     * <p/>
     * Return {@code null} if the journal's theme does not provide a logo file. In this case, it is best for the
     * callback to log a warning that describes the file missing from the theme, because the invoker won't know anything
     * about the theme backing this callback object.
     *
     * @return the input stream, or {@code null} if no logo is available for the journal
     * @throws IOException
     */
    public abstract InputStream openLogoStream() throws IOException;
  }

  /**
   * Create a Microsoft PowerPoint slide show object that presents a figure.
   *
   * @param figureMetadata the metadata of the figure to feature in the slide show
   * @param articleLink    an absolute path to the figure's article
   * @param logoCallback   provides the journal logo for the figure
   * @return the slide show object
   * @throws IOException
   */
  public abstract SlideShow createPowerPointFile(Map<String, ?> figureMetadata,
                                                 URL articleLink,
                                                 JournalLogoCallback logoCallback)
      throws IOException;

}
