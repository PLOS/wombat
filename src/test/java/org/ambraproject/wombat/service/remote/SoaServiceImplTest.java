package org.ambraproject.wombat.service.remote;

import org.ambraproject.wombat.config.JsonConfiguration;
import org.ambraproject.wombat.util.CacheParams;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.message.BasicHeader;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.net.URL;

import static org.ambraproject.wombat.service.remote.TestUtils.*;
import static org.eclipse.jetty.http.HttpMethods.GET;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Created by jkrzemien on 7/15/14.
 */

public class SoaServiceImplTest {

    @Mock
    private JsonConfiguration jsonConfig;
    @Mock
    private JsonService jsonService;
    @Mock
    private CachedRemoteService<InputStream> cachedRemoteStreamer;
    @Mock
    private CachedRemoteService<Reader> cachedRemoteReader;

    @InjectMocks
    private SoaServiceImpl serviceUnderTest = new SoaServiceImpl();

    public SoaServiceImplTest() {
        initMocks(this);
    }

    @BeforeMethod
    public void setUp() throws IOException {
        // Re initialize all mocks that we are dealing with...
        reset(jsonConfig, jsonService, cachedRemoteStreamer, cachedRemoteReader);

        // Define common behaviour for minimum required mocks for all tests...
        when(jsonConfig.getServer()).thenReturn(new URL(TEST_URL));
    }

    @Test
    public void requestAssetTest() throws IOException {
        final String assetId = "daAssetId";
        final HttpUriRequest expectedURI = buildExpectedURI("assetfiles/" + assetId);
        Header testHeader = new BasicHeader("testName", "testValue");
        expectedURI.setHeader(testHeader);

        when(cachedRemoteStreamer.getResponse(any(HttpGet.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                HttpUriRequest argumentPassedToStreamer = (HttpUriRequest) invocationOnMock.getArguments()[0];
                assertEquals(argumentPassedToStreamer.getMethod(), GET);
                assertEquals(argumentPassedToStreamer.getURI(), expectedURI.getURI());
                assertEquals(argumentPassedToStreamer.getAllHeaders().length, 1);
                assertEquals(argumentPassedToStreamer.getAllHeaders(), expectedURI.getAllHeaders());
                return mock(CloseableHttpResponse.class);
            }
        });

        CloseableHttpResponse response = serviceUnderTest.requestAsset(assetId, testHeader);

        assertNotNull(response);

        InOrder inOrder = inOrder(jsonConfig, cachedRemoteStreamer);
        inOrder.verify(jsonConfig).getServer();
        inOrder.verify(cachedRemoteStreamer).getResponse(any(HttpGet.class));
        verifyNoMoreInteractions(jsonConfig, cachedRemoteStreamer);
        verifyZeroInteractions(jsonService, cachedRemoteReader);
    }

    @Test
    public void requestStreamTest() throws IOException {
        final String responseFromService = "test_string";
        String address = "daAddress";
        final HttpGet expectedAddress = buildExpectedURI(address);

        when(cachedRemoteStreamer.request(any(HttpGet.class))).thenAnswer(new Answer<InputStream>() {
            @Override
            public InputStream answer(InvocationOnMock invocationOnMock) throws Throwable {
                HttpUriRequest argumentPassedToStreamer = (HttpUriRequest) invocationOnMock.getArguments()[0];
                assertEquals(argumentPassedToStreamer.getURI(), expectedAddress.getURI());
                return string2InputStream(responseFromService);
            }
        });

        InputStream response = serviceUnderTest.requestStream(address);

        assertNotNull(response);
        assertEquals(inputStream2String(response), responseFromService);

        InOrder inOrder = inOrder(jsonConfig, cachedRemoteStreamer);
        inOrder.verify(jsonConfig).getServer();
        inOrder.verify(cachedRemoteStreamer).request(any(HttpGet.class));
        verifyNoMoreInteractions(jsonConfig, cachedRemoteStreamer);
        verifyZeroInteractions(jsonService, cachedRemoteReader);
    }

    /*@Test
    public void requestFromContentRepoTest() throws IOException {
        String bucket = "daBucket";
        String key = "daKey";
        String version = "daVersion";

        final HttpGet expectedURI = buildExpectedRepoURI(bucket, key, version);

        when(cachedRemoteStreamer.getResponse(any(HttpGet.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                HttpUriRequest argumentPassedToStreamer = (HttpUriRequest) invocationOnMock.getArguments()[0];
                assertEquals(argumentPassedToStreamer.getMethod(), GET);
                assertEquals(argumentPassedToStreamer.getURI(), expectedURI.getURI());
                assertEquals(argumentPassedToStreamer.getAllHeaders().length, 0);
                return mock(CloseableHttpResponse.class);
            }
        });

        CloseableHttpResponse response = serviceUnderTest.requestFromContentRepo(bucket, key, version);

        assertNotNull(response);

        InOrder inOrder = inOrder(jsonConfig, cachedRemoteStreamer);
        inOrder.verify(jsonConfig).getServer();
        inOrder.verify(cachedRemoteStreamer).getResponse(any(HttpGet.class));
        verifyNoMoreInteractions(jsonConfig, cachedRemoteStreamer);
        verifyZeroInteractions(jsonService, cachedRemoteReader);
    }*/

    @Test
    public void requestCachedObjectTest() throws IOException {
        final Integer returnedObject = new Integer(13);
        final CacheParams cacheKey = CacheParams.create("daKey");
        final HttpGet expectedAddress = buildExpectedURI("something");

        when(jsonService.requestCachedObject(cachedRemoteReader, cacheKey, expectedAddress.getURI(), returnedObject.getClass())).thenAnswer(new Answer<Integer>() {
            @Override
            public Integer answer(InvocationOnMock invocationOnMock) throws Throwable {
                CachedRemoteService<? extends Reader> passedRemoteService = (CachedRemoteService<? extends Reader>) invocationOnMock.getArguments()[0];
                CacheParams passedCacheKey = (CacheParams) invocationOnMock.getArguments()[1];
                URI passedAddress = (URI) invocationOnMock.getArguments()[2];
                Class<?> passedResponseClass = (Class<?>) invocationOnMock.getArguments()[3];
                assertEquals(passedRemoteService, cachedRemoteReader);
                assertEquals(passedCacheKey, cacheKey);
                assertEquals(passedAddress, expectedAddress.getURI());
                assertEquals(passedResponseClass, returnedObject.getClass());
                return returnedObject;
            }
        });

        Object response = serviceUnderTest.requestCachedObject(cacheKey, "something", returnedObject.getClass());

        assertNotNull(response);
        assertEquals(response, returnedObject);
        assertEquals(response.getClass(), returnedObject.getClass());

        InOrder inOrder = inOrder(jsonConfig, jsonService);
        inOrder.verify(jsonConfig).getServer();
        inOrder.verify(jsonService).requestCachedObject(cachedRemoteReader, cacheKey, expectedAddress.getURI(), returnedObject.getClass());
        verifyNoMoreInteractions(jsonConfig, jsonService);
        verifyZeroInteractions(cachedRemoteStreamer, cachedRemoteReader);
    }

    @Test
    public void requestObjectTest() throws IOException {
        final Integer returnedObject = new Integer(13);
        final String cachePreffix = "obj:";
        final String address = "something";
        final CacheParams cacheKey = CacheParams.create(cachePreffix + address);
        final HttpGet expectedAddress = buildExpectedURI(address);

        when(jsonService.requestCachedObject(cachedRemoteReader, cacheKey, expectedAddress.getURI(), returnedObject.getClass())).thenAnswer(new Answer<Integer>() {
            @Override
            public Integer answer(InvocationOnMock invocationOnMock) throws Throwable {
                CachedRemoteService<? extends Reader> passedRemoteService = (CachedRemoteService<? extends Reader>) invocationOnMock.getArguments()[0];
                CacheParams passedCacheKey = (CacheParams) invocationOnMock.getArguments()[1];
                URI passedAddress = (URI) invocationOnMock.getArguments()[2];
                Class<?> passedResponseClass = (Class<?>) invocationOnMock.getArguments()[3];
                assertEquals(passedRemoteService, cachedRemoteReader);
                assertEquals(passedCacheKey, cacheKey);
                assertEquals(passedAddress, expectedAddress.getURI());
                assertEquals(passedResponseClass, returnedObject.getClass());
                return returnedObject;
            }
        });

        Object response = serviceUnderTest.requestObject(address, returnedObject.getClass());

        assertNotNull(response);
        assertEquals(response, returnedObject);
        assertEquals(response.getClass(), returnedObject.getClass());

        InOrder inOrder = inOrder(jsonConfig, jsonService);
        inOrder.verify(jsonConfig).getServer();
        inOrder.verify(jsonService).requestCachedObject(cachedRemoteReader, cacheKey, expectedAddress.getURI(), returnedObject.getClass());
        verifyNoMoreInteractions(jsonConfig, jsonService);
        verifyZeroInteractions(cachedRemoteStreamer, cachedRemoteReader);
    }

    @Test
    public void postObjectTest() throws IOException {
        final String address = "something";
        final HttpGet expectedAddress = buildExpectedURI(address);
        final String serializedJSON = "{ 'what_a': 'nice_json!' }";

        when(jsonService.serialize(any())).thenReturn(serializedJSON);
        when(cachedRemoteReader.getResponse(any(HttpPost.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                HttpPost post = (HttpPost) invocationOnMock.getArguments()[0];
                assertEquals(post.getURI(), expectedAddress.getURI());
                assertEquals(inputStream2String(post.getEntity().getContent()), serializedJSON);
                assertEquals(post.getEntity().getContentType().getValue(), "text/plain; charset=ISO-8859-1");
                return mock(CloseableHttpResponse.class);
            }
        });

        serviceUnderTest.postObject(address, new Object());

        InOrder inOrder = inOrder(jsonService, cachedRemoteReader);
        inOrder.verify(jsonService).serialize(any(Object.class));
        inOrder.verify(cachedRemoteReader).getResponse(any(HttpPost.class));
        verifyNoMoreInteractions(jsonService, cachedRemoteReader);
        verifyZeroInteractions(cachedRemoteStreamer);
    }

    @Test
    public void requestReaderTest() throws IOException {
        final String responseFromService = "test_string";
        String address = "daAddress";
        final HttpGet expectedAddress = buildExpectedURI(address);

        when(cachedRemoteReader.request(any(HttpGet.class))).thenAnswer(new Answer<Reader>() {
            @Override
            public Reader answer(InvocationOnMock invocationOnMock) throws Throwable {
                HttpUriRequest argumentPassedToReader = (HttpUriRequest) invocationOnMock.getArguments()[0];
                assertEquals(argumentPassedToReader.getURI(), expectedAddress.getURI());
                return new StringReader(responseFromService);
            }
        });

        Reader response = serviceUnderTest.requestReader(address);

        assertNotNull(response);
        assertEquals(reader2String(response), responseFromService);

        InOrder inOrder = inOrder(jsonConfig, cachedRemoteReader);
        inOrder.verify(jsonConfig).getServer();
        inOrder.verify(cachedRemoteReader).request(any(HttpGet.class));
        verifyNoMoreInteractions(jsonConfig, cachedRemoteReader);
        verifyZeroInteractions(jsonService, cachedRemoteStreamer);
    }

    @Test
    public void requestCachedReaderTest() throws IOException {

        final String responseFromService = "test_string";
        String address = "daAddress";
        final HttpGet expectedAddress = buildExpectedURI(address);
        final CacheParams cacheKey = CacheParams.create("daKey");

        final CacheDeserializer<Reader, String> callback = new CacheDeserializer<Reader, String>() {
            @Override
            public String read(Reader stream) throws IOException {
                return null;
            }
        };

        when(cachedRemoteReader.requestCached(eq(cacheKey), any(HttpGet.class), eq(callback))).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocationOnMock) throws Throwable {
                CacheParams passedCacheKey = (CacheParams) invocationOnMock.getArguments()[0];
                HttpUriRequest passedTarget = (HttpUriRequest) invocationOnMock.getArguments()[1];
                CacheDeserializer<Reader, String> passedCallback = (CacheDeserializer<Reader, String>) invocationOnMock.getArguments()[2];
                assertEquals(passedCacheKey, cacheKey);
                assertEquals(passedTarget.getURI(), expectedAddress.getURI());
                assertEquals(passedCallback, callback);
                return responseFromService;
            }
        });

        String response = serviceUnderTest.requestCachedReader(cacheKey, address, callback);

        assertNotNull(response);
        assertEquals(response, responseFromService);

        InOrder inOrder = inOrder(jsonConfig, cachedRemoteReader);
        inOrder.verify(jsonConfig).getServer();
        inOrder.verify(cachedRemoteReader).requestCached(eq(cacheKey), any(HttpGet.class), eq(callback));
        verifyNoMoreInteractions(jsonConfig, cachedRemoteReader);
        verifyZeroInteractions(jsonService, cachedRemoteStreamer);
    }

    @Test
    public void requestCachedStreamTest() throws IOException {

        final String responseFromService = "test_string";
        String address = "daAddress";
        final HttpGet expectedAddress = buildExpectedURI(address);
        final CacheParams cacheKey = CacheParams.create("daKey");

        final CacheDeserializer<InputStream, String> callback = mock(CacheDeserializer.class);

        when(cachedRemoteStreamer.requestCached(eq(cacheKey), any(HttpGet.class), eq(callback))).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocationOnMock) throws Throwable {
                CacheParams passedCacheKey = (CacheParams) invocationOnMock.getArguments()[0];
                HttpUriRequest passedTarget = (HttpUriRequest) invocationOnMock.getArguments()[1];
                CacheDeserializer<Reader, String> passedCallback = (CacheDeserializer<Reader, String>) invocationOnMock.getArguments()[2];
                assertEquals(passedCacheKey, cacheKey);
                assertEquals(passedTarget.getURI(), expectedAddress.getURI());
                assertEquals(passedCallback, callback);
                return responseFromService;
            }
        });

        String response = serviceUnderTest.requestCachedStream(cacheKey, address, callback);

        assertNotNull(response);
        assertEquals(response, responseFromService);

        InOrder inOrder = inOrder(jsonConfig, cachedRemoteStreamer);
        inOrder.verify(jsonConfig).getServer();
        inOrder.verify(cachedRemoteStreamer).requestCached(eq(cacheKey), any(HttpGet.class), eq(callback));
        verifyNoMoreInteractions(jsonConfig, cachedRemoteStreamer);
        verifyZeroInteractions(jsonService, cachedRemoteReader);
    }

}
