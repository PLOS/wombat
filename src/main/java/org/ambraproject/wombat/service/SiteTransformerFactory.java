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

package org.ambraproject.wombat.service;

import com.google.common.io.Closer;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.theme.Theme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
import net.sf.saxon.TransformerFactoryImpl;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.ExtensionFunction;

public class SiteTransformerFactory {
  ExtensionFunction[] extensionFunctions;

  private TransformerFactory newTransformerFactory() {
    // This implementation is required for XSLT features, so just hard-code it here
    // Preferred over TransformerFactory.newInstance because Java system properties can burn in hell
    TransformerFactoryImpl tFactoryImpl = new TransformerFactoryImpl();
    net.sf.saxon.Configuration saxonConfig = tFactoryImpl.getConfiguration();
    Processor processor = (Processor) saxonConfig.getProcessor();
    for (ExtensionFunction ext : extensionFunctions) {
      processor.registerExtensionFunction(ext);
    }
    return tFactoryImpl;
  }

  private final String rootPath;
  private final String templateFilename;
  private final Map<Site, Templates> transformTemplateCache = new ConcurrentHashMap<>();

  public SiteTransformerFactory(String rootPath, String templateFilename, ExtensionFunction ... extensionFunctions) {
    this.rootPath = Objects.requireNonNull(rootPath);
    this.templateFilename = Objects.requireNonNull(templateFilename);
    this.extensionFunctions = extensionFunctions;
  }

  public SiteTransformerFactory(String rootPath, String templateFilename) {
    this(rootPath, templateFilename, new ExtensionFunction[] {});
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
