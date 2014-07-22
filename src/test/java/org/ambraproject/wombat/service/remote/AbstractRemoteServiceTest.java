package org.ambraproject.wombat.service.remote;

import org.ambraproject.wombat.service.EntityNotFoundException;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.testng.annotations.Test;

import java.io.InputStream;

import static org.ambraproject.wombat.service.remote.TestUtils.buildExpectedURI;
import static org.apache.http.HttpStatus.*;
import static org.testng.Assert.*;

/**
 * Created by jkrzemien on 7/15/14.
 */

public class AbstractRemoteServiceTest {

    private MockHttpClientConnectionManager mockHttpManager = new MockHttpClientConnectionManager();

    private AbstractRemoteService<InputStream> classUnderTest = new StreamService(mockHttpManager);

    @Test
    public void requestTest() throws Exception {
        String daCoolText = "These aren't the droids you are looking for...";
        mockHttpManager.setResponse(SC_OK, daCoolText);

        InputStream response = classUnderTest.request(buildExpectedURI("something"));

        assertNotNull(response);
        assertTrue(response.available() > 0);
        String responseText = IOUtils.toString(response);
        assertEquals(daCoolText, responseText);
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "No response")
    public void requestNullResponseTest() throws Exception {
        mockHttpManager.setResponse(SC_OK, null);
        classUnderTest.request(buildExpectedURI("something"));
    }

    @Test(expectedExceptions = ServiceRequestException.class, expectedExceptionsMessageRegExp = "Request to \"/something\" failed \\(500\\): ")
    public void invalidRequestTest() throws Exception {
        mockHttpManager.setResponse(SC_INTERNAL_SERVER_ERROR, "");
        classUnderTest.request(buildExpectedURI("something"));
    }

    @Test(expectedExceptions = EntityNotFoundException.class, expectedExceptionsMessageRegExp = "Entity /something not found")
    public void notFoundRequestTest() throws Exception {
        mockHttpManager.setResponse(SC_NOT_FOUND, "");
        classUnderTest.request(buildExpectedURI("something"));
    }

    @Test(expectedExceptions = EntityNotFoundException.class, expectedExceptionsMessageRegExp = "Entity /something\\?param=woot\\! not found")
    public void requestWithQueryTest() throws Exception {
        mockHttpManager.setResponse(SC_NOT_FOUND, "");
        classUnderTest.request(buildExpectedURI("something?param=woot!"));
    }

    @Test
    public void getResponseTest() throws Exception {
        String daCoolText = "These aren't the droids you are looking for...";
        mockHttpManager.setResponse(SC_OK, daCoolText);

        CloseableHttpResponse response = classUnderTest.getResponse(buildExpectedURI("something?param=woot!"));

        assertNotNull(response);
        String responseText = IOUtils.toString(response.getEntity().getContent());
        assertEquals(daCoolText, responseText);
        assertEquals(SC_OK, response.getStatusLine().getStatusCode());
    }
}
