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

package org.ambraproject.wombat.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@ContextConfiguration
public class PeerReviewControllerTest extends ControllerTest {
  @Autowired
  ArticleMetadata articleMetadata;

  @Autowired
  ArticleMetadata.Factory articleMetadataFactory;
  
  @BeforeMethod
  private void setup() throws IOException {
    when(articleMetadata.validateVisibility(anyString())).thenReturn(articleMetadata);
    when(articleMetadata.populate(any(), any())).thenReturn(articleMetadata);
    when(articleMetadata.fillAmendments(any())).thenReturn(articleMetadata);

    doReturn(articleMetadata).when(articleMetadataFactory).newInstance(any(), any(), any(), any(), any(), any());
  }
    
  @Test(expectedExceptions = NotFoundException.class)
  public void testPeerReviewNotFound(){
    PeerReviewController controller = new PeerReviewController();
    Map<String, Object> map = new HashMap<String,Object>();

    controller.throwIfPeerReviewNotFound(map);
  }

  @Test
  public void testPeerReviewFound(){
    PeerReviewController controller = new PeerReviewController();
    Map<String, Object> map = new HashMap<String,Object>();

    map.put("peerReview", "foo");
    controller.throwIfPeerReviewNotFound(map);
  }
}
