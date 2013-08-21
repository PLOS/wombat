package org.ambraproject.wombat.controller;

import com.google.common.io.Closer;
import org.ambraproject.wombat.config.SiteSet;
import org.ambraproject.wombat.config.Theme;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Controller
public class StaticFileController {

  @Autowired
  private SiteSet siteSet;

  @RequestMapping("/{site}/static/**")
  public void serveStaticContent(HttpServletRequest request, HttpServletResponse response,
                                 HttpSession session, @PathVariable("site") String site)
      throws IOException {
    Theme theme = siteSet.getSite(site).getTheme();

    // Kludge to get "static/**"
    String servletPath = request.getServletPath();
    String filePath = servletPath.substring(site.length() + 2);
    response.setContentType(session.getServletContext().getMimeType(servletPath));

    Closer closer = Closer.create();
    try {
      InputStream inputStream = theme.getStaticResource(filePath);
      if (inputStream == null) {
        // TODO: Forward to user-friendly 404 page
        response.setStatus(HttpStatus.NOT_FOUND.value());

        // Just for debugging
        closer.register(response.getOutputStream()).write("Not found!".getBytes());
      } else {
        closer.register(inputStream);
        OutputStream outputStream = closer.register(response.getOutputStream());
        IOUtils.copy(inputStream, outputStream);
      }
    } catch (Throwable t) {
      throw closer.rethrow(t);
    } finally {
      closer.close();
    }
  }

}
