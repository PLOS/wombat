package org.ambraproject.wombat.service;

import org.testng.annotations.Test;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmItem;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

public class DataUriEncodeFunctionTest {
  List<XdmItem> emptyList = new ArrayList<XdmItem>();
  XdmItem trueXdm = new XdmAtomicValue(true);
  XdmItem oneXdm = new XdmAtomicValue(1);
  XdmItem helloXdm = new XdmAtomicValue("hello");
  
  @Test
  public void testExtractInt() throws SaxonApiException {
    assertEquals(0, DataUriEncodeFunction.extractInt(trueXdm));
    assertEquals(1, DataUriEncodeFunction.extractInt(oneXdm));
  }

  @Test
  public void testExtractString() throws SaxonApiException {
    assertEquals("hello", DataUriEncodeFunction.extractString(helloXdm));
  }
}
