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

import org.ambraproject.wombat.service.remote.CorpusContentApi;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertNull;

@ContextConfiguration(classes = {PeerReviewServiceTest.class})
public class PeerReviewServiceTest extends AbstractTestNGSpringContextTests {

  @Mock
  public CorpusContentApi corpusContentApi;

  @InjectMocks
  private PeerReviewService peerReviewService = new PeerReviewServiceImpl();

  @BeforeMethod
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testAsHtml() throws IOException {
    Map<String, ?> stringMap = new HashMap<>();
    String s = peerReviewService.asHtml(stringMap);
    assertNull(s);
  }

//
//  @Test
//  public void testGetPeerReview() throws IOException {
//    Map<String, String> asset = new HashMap<>();
//    asset.put("doi", "fakeDoi");
//    List<Map<String, String>> assets = new ArrayList<>();
//    assets.add(asset);
//    HashMap<String, List<Map<String, String>>> ingestionMetadata = new HashMap<>();
//    ingestionMetadata.put("assetsLinkedFromManuscript", assets);
//
//    Map<String, String> item = new HashMap<>();
//    item.put("itemType", "reviewLetter");
//    Map<String, Map<String, String>> itemTable = new HashMap<>();
//    itemTable.put("fakeDoi", item);
//
//    Theme theme = mock(Theme.class);
//    HashMap<String, Object> journalAttrs = new HashMap<>();
//    journalAttrs.put("journalKey", "fakeKey");
//    journalAttrs.put("journalName", "fakeName");
//    when(theme.getConfigMap(any())).thenReturn(journalAttrs);
//
//    Site site = new Site("foo", theme, mock(SiteRequestScheme.class), "foo");
//    ArticleMetadata articleMetadata = articleMetadataFactory.newInstance(site,
//        mock(RequestedDoiVersion.class),
//        mock(ArticlePointer.class),
//        ingestionMetadata,
//        itemTable,
//        new HashMap());
//
//    assertNotNull(articleMetadata.getPeerReview());
//  }
//
//  @Test
//  public void testGetPeerReviewReturnsNullWhenNoPeerReviewItems() throws IOException {
//    Map<String, String> asset = new HashMap<>();
//    asset.put("doi", "fakeDoi");
//    List<Map<String, String>> assets = new ArrayList<>();
//    assets.add(asset);
//    HashMap<String, List<Map<String, String>>> ingestionMetadata = new HashMap<>();
//    ingestionMetadata.put("assetsLinkedFromManuscript", assets);
//
//    Map<String, String> item = new HashMap<>();
//    item.put("itemType", "figure");
//    Map<String, Map<String, String>> itemTable = new HashMap<>();
//    itemTable.put("fakeDoi", item);
//
//    Theme theme = mock(Theme.class);
//    HashMap<String, Object> journalAttrs = new HashMap<>();
//    journalAttrs.put("journalKey", "fakeKey");
//    journalAttrs.put("journalName", "fakeName");
//    when(theme.getConfigMap(any())).thenReturn(journalAttrs);
//
//    Site site = new Site("foo", theme, mock(SiteRequestScheme.class), "foo");
//    ArticleMetadata articleMetadata = articleMetadataFactory.newInstance(site,
//        mock(RequestedDoiVersion.class),
//        mock(ArticlePointer.class),
//        ingestionMetadata,
//        itemTable,
//        new HashMap());
//
//    assertNull(articleMetadata.getPeerReview());
//  }
}
