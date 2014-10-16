package org.ambraproject.wombat.controller;

import com.google.common.net.HttpHeaders;
import com.google.common.net.MediaType;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.theme.Theme;
import org.ambraproject.wombat.service.PowerPointService;
import org.apache.poi.hslf.usermodel.SlideShow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

/**
 * Endpoint for downloading PowerPoint slides of figures.
 */
@Controller
public class PowerPointController {
  private static final Logger log = LoggerFactory.getLogger(PowerPointController.class);

  private static final String LOGO_PATH = "resource/img/logo.png";

  @Autowired
  private PowerPointService powerPointService;

  @RequestMapping({"/article/figure/powerpoint", "/{site}/article/figure/powerpoint"})
  public void download(HttpServletRequest request, HttpServletResponse response,
                       @SiteParam Site site,
                       @RequestParam(value = "id", required = true) String figureId)
      throws IOException {

    final Theme theme = site.getTheme();
    PowerPointService.JournalLogoCallback logoCallback = new PowerPointService.JournalLogoCallback() {
      @Override
      public InputStream openLogoStream() throws IOException {
        InputStream stream = theme.getStaticResource(LOGO_PATH);
        if (stream == null) {
          log.warn("Logo file not found at {} for theme: {}", LOGO_PATH, theme.getKey());
        }
        return stream;
      }
    };

    StringBuffer requestUrl = request.getRequestURL();
    URL articleLink = new URL(requestUrl.toString()); // TODO: Point at correct link address

    SlideShow powerPointFile = powerPointService.createPowerPointFile(figureId, articleLink, logoCallback);
    response.setContentType(MediaType.MICROSOFT_POWERPOINT.toString());
    response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + getDownloadFilename(figureId));
    try (OutputStream outputStream = response.getOutputStream()) {
      powerPointFile.write(outputStream);
    }
  }

  private static String getDownloadFilename(String figureId) {
    int slashIndex = figureId.lastIndexOf('/');
    String name = figureId.substring(slashIndex + 1);
    return name + ".ppt";
  }

}
