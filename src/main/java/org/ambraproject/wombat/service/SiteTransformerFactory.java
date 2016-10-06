package org.ambraproject.wombat.service;

import com.google.common.io.Closer;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.theme.Theme;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.Templates;
import javax.xml.transform.stream.StreamSource;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class SiteTransformerFactory {

  private static TransformerFactory newTransformerFactory() {
    // This implementation is required for XSLT features, so just hard-code it here
    // Preferred over TransformerFactory.newInstance because Java system properties can burn in hell
    return new net.sf.saxon.TransformerFactoryImpl();
  }

  private final String rootPath;
  private final String templateFilename;
  private final Map<Site, Templates> transformTemplateCache = new ConcurrentHashMap<>();

  public SiteTransformerFactory(String rootPath, String templateFilename) {
    this.rootPath = Objects.requireNonNull(rootPath);
    this.templateFilename = Objects.requireNonNull(templateFilename);
  }

  public Transformer build(Site site) {
    Templates templates = transformTemplateCache.computeIfAbsent(site, this::createTemplate);
    try {
      return templates.newTransformer();
    } catch (TransformerConfigurationException e){
      throw new RuntimeException(e);
    }
  }

  private Templates createTemplate(Site site) {
    TransformerFactory factory = newTransformerFactory();
    Theme theme = site.getTheme();
    try (ThemeUriResolver resolver = new ThemeUriResolver(theme);
         InputStream transformFile = theme.getStaticResource(rootPath + templateFilename)) {
      factory.setURIResolver(resolver);
      return factory.newTemplates(new StreamSource(transformFile));
    } catch (TransformerConfigurationException | IOException e) {
      throw new RuntimeException(e);
    }
  }

  private class ThemeUriResolver implements URIResolver, Closeable {

    /*
     * Any stream opened while providing a Source gets stored here,
     * then is closed when the outer ThemeUriResolver object is closed.
     */
    private final Closer closer = Closer.create();

    private final Theme theme;

    private ThemeUriResolver(Theme theme) {
      this.theme = Objects.requireNonNull(theme);
    }

    @Override
    public Source resolve(String href, String base) throws TransformerException {
      InputStream resourceStream;
      try {
        resourceStream = closer.register(theme.getStaticResource(rootPath + href));
      } catch (IOException e) {
        throw new TransformerException(e);
      }
      return new StreamSource(resourceStream);
    }

    @Override
    public void close() throws IOException {
      closer.close();
    }
  }

}
