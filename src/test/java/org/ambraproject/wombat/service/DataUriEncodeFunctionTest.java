package org.ambraproject.wombat.service;

import org.ambraproject.wombat.identity.RequestedDoiVersion;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.entity.ContentType;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import net.sf.saxon.s9api.ItemType;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmValue;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DataUriEncodeFunctionTest {
  List<XdmItem> emptyList = new ArrayList<XdmItem>();
  XdmItem trueXdm = new XdmAtomicValue(true);
  XdmItem oneXdm = new XdmAtomicValue(1);
  XdmItem helloXdm = new XdmAtomicValue("hello");


  @InjectMocks
  DataUriEncodeFunction dataUriEncodeFunction;

  @BeforeMethod
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }
  
  @Test
  public void testExtractInt() throws SaxonApiException {
    assertEquals(0, DataUriEncodeFunction.extractInt(trueXdm));
    assertEquals(1, DataUriEncodeFunction.extractInt(oneXdm));
  }

  @Test
  public void testExtractString() throws SaxonApiException {
    assertEquals("hello", DataUriEncodeFunction.extractString(helloXdm));
  }

  @Test
  public void testGetDoiFromArguments() throws SaxonApiException {
    String doiString = "10.9999/1";
    XdmValue doiXdm = new XdmAtomicValue(doiString, ItemType.STRING);
    int ingestionNumber = 1;
    XdmValue ingestionNumberXdm = new XdmAtomicValue(ingestionNumber);

    assertEquals(RequestedDoiVersion.ofIngestion(doiString, ingestionNumber),
                 dataUriEncodeFunction.getDoiFromArguments(new XdmValue[]{helloXdm, doiXdm, ingestionNumberXdm}));
  }

  @Test
  public void testEncodeAsDataUri() throws IOException {
    assertEquals(Optional.empty(), DataUriEncodeFunction.encodeAsDataUrl(null));

    InputStream stubInputStream =  IOUtils.toInputStream("bar", "UTF-8"); // base64: Zm9v
    HttpEntity entity = mock(HttpEntity.class);
    Header header = mock(Header.class);
    HeaderElement element = mock(HeaderElement.class);
    when(entity.getContentType()).thenReturn(header);
    when(header.getElements()).thenReturn(new HeaderElement[]{ element });
    when(element.getName()).thenReturn("image/x-foo");
    when(element.getParameters()).thenReturn(new NameValuePair[]{});
    when(entity.getContent()).thenReturn(stubInputStream);
    
    assertEquals(Optional.of("data:image/x-foo;base64,YmFy"),
                 DataUriEncodeFunction.encodeAsDataUrl(entity));
  }
}
