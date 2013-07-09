package org.ambraproject.wombat.service;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.io.Closer;
import org.ambraproject.wombat.config.Theme;
import org.springframework.beans.factory.annotation.Autowired;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

public class ArticleTransformServiceImpl implements ArticleTransformService {

  private static final String TEMPLATE_ROOT_PATH = "static/xform/";
  private static final String TRANSFORM_TEMPLATE_PATH = TEMPLATE_ROOT_PATH + "article-transform.xsl";

  @Autowired
  private ImmutableMap<String, Theme> themesForJournals;

  private static TransformerFactory newTransformerFactory() {
    // This implementation is required for XSLT features, so just hard-code it here
    // Preferred over TransformerFactory.newInstance because Java system properties can burn in hell
    return new net.sf.saxon.TransformerFactoryImpl();
  }


  private static class ThemeUriResolver implements URIResolver, Closeable {

    /*
     * Any stream opened while providing a Source gets stored here,
     * then is closed when the outer ThemeUriResolver object is closed.
     */
    private final Closer closer = Closer.create();

    private final Theme theme;

    private ThemeUriResolver(Theme theme) {
      this.theme = Preconditions.checkNotNull(theme);
    }

    @Override
    public Source resolve(String href, String base) throws TransformerException {
      InputStream resourceStream;
      try {
        resourceStream = closer.register(theme.getStaticResource(TEMPLATE_ROOT_PATH + href));
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


  private final Map<String, Transformer> cache = Maps.newHashMap();

  /**
   * Access the transformer for a journal. Either builds it or retrieves it from a cache.
   *
   * @param journal the journal key
   * @return the transformer
   * @throws IOException
   */
  private Transformer getTransformer(String journal) throws IOException {
    /*
     * Use a simple, hashtable-based cache. This assumes that the number of journals (and the size of the transfomers)
     * will never be so large that evicting a cached transformer makes sense.
     *
     * This prevents the application from picking up any dynamic changes to the transform in a theme (such as an *.xsl
     * file on disk being overwritten at runtime).
     */
    Transformer transformer = cache.get(journal);
    if (transformer == null) {
      transformer = buildTransformer(journal);
      cache.put(journal, transformer);
    }
    return transformer;
  }

  /**
   * Build a new transformer for a journal.
   *
   * @param journal the journal key
   * @return the transformer
   * @throws IOException
   */
  private Transformer buildTransformer(String journal) throws IOException {
    Theme theme = themesForJournals.get(journal);
    if (theme == null) {
      throw new UnmatchedJournalException(journal);
    }
    TransformerFactory factory = newTransformerFactory();

    Closer closer = Closer.create();
    try {
      ThemeUriResolver resolver = closer.register(new ThemeUriResolver(theme));
      factory.setURIResolver(resolver);

      InputStream transformFile = closer.register(theme.getStaticResource(TRANSFORM_TEMPLATE_PATH));
      return factory.newTransformer(new StreamSource(transformFile));
    } catch (Throwable t) {
      throw closer.rethrow(t);
    } finally {
      closer.close();
    }
  }

  @Override
  public void transform(String journalKey, InputStream xml, OutputStream html)
      throws IOException, TransformerException {
    Preconditions.checkNotNull(journalKey);
    Preconditions.checkNotNull(xml);
    Preconditions.checkNotNull(html);

    Transformer transformer = getTransformer(journalKey);
    transformer.transform(new StreamSource(xml), new StreamResult(html));
  }

}
