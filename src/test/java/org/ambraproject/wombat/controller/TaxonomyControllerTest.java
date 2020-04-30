/*
 * Copyright (c) 2019 Public Library of Science
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.ambraproject.wombat.config.theme.Theme;
import org.ambraproject.wombat.model.TaxonomyGraph;
import org.ambraproject.wombat.model.TaxonomyGraph.CategoryView;
import org.ambraproject.wombat.service.BrowseTaxonomyService;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration
public class TaxonomyControllerTest extends ControllerTest {
  @Configuration
  static class ContextConfiguration {
    @Bean
    public TaxonomyController taxonomyController() {
      return new TaxonomyController();
    }
  }

  @Autowired
  TaxonomyController taxonomyController;
  
  @Autowired
  Theme theme;

  @Autowired
  BrowseTaxonomyService browseTaxonomyService;

  @Before
  public void setup() throws IOException {
    Map<String, Object> taxonomyBrowserConfig = new HashMap<String, Object>();
    taxonomyBrowserConfig.put("hasTaxonomyBrowser", true);
    when(theme.getConfigMap("taxonomyBrowser")).thenReturn(taxonomyBrowserConfig);

    TaxonomyGraph taxonomyGraph = mock(TaxonomyGraph.class);
    when(taxonomyGraph.getRootCategoryViews()).thenReturn(new ArrayList<CategoryView>());

    Map<String, Long> articleCounts = mock(Map.class);
    when(articleCounts.get("ROOT")).thenReturn(0L);

    when(browseTaxonomyService.parseCategories(any(), any())).thenReturn(taxonomyGraph);
    when(browseTaxonomyService.getCounts(any(), any(), any())).thenReturn(articleCounts);
  }

  @Test
  public void testSmokeTest() throws Exception {
    mockMvc.perform(get("/taxonomy/")).andExpect(handler().handlerType(TaxonomyController.class))
        .andExpect(handler().methodName("read")).andExpect(status().is(HttpStatus.SC_OK)).andReturn();
  }
}
