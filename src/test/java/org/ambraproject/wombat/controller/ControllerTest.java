package org.ambraproject.wombat.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import org.testng.annotations.BeforeMethod;

import java.io.IOException;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@WebAppConfiguration
public class ControllerTest extends AbstractTestNGSpringContextTests {

  @Autowired
  private WebApplicationContext wac;

  protected MockMvc mockMvc;

  @BeforeMethod
  public void setUp() throws IOException {
    this.mockMvc = webAppContextSetup(wac)
        .alwaysDo(print())
        .build();
  }
}
