package org.ambraproject.wombat.controller;

import com.gargoylesoftware.htmlunit.WebClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.htmlunit.MockMvcWebConnection;
import org.springframework.web.context.WebApplicationContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.io.IOException;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

/**
 * Created by jkrzemien on 7/16/14.
 */

@WebAppConfiguration
public class ControllerTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private WebApplicationContext wac;

    protected MockMvc mockMvc;
    protected WebClient webClient;

    @BeforeMethod
    public void setUp() throws IOException {
        this.mockMvc = webAppContextSetup(wac)
                .alwaysDo(print())
                .build();

        /**
         * In order to test the "V" part of MVC a little deeper, here we inject a minimalistic user-like web browser
         * to be the one rendering the view.
         * This way, we can interact with the HTML generated in an "object oriented way"...kind of...
         */
        this.webClient = new WebClient();
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.setWebConnection(new MockMvcWebConnection(mockMvc));
    }

    @AfterMethod(alwaysRun = true)
    public void cleanup() {
        this.webClient.closeAllWindows();
    }
}

