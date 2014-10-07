package contact.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.core.Response.Status;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import contact.jetty.JettyMain;
import contact.service.mem.MemDaoFactory;
import contact.service.mem.MemContactDao;

/**
 * JUnit Test class for testing ContactResource with ETag
 * Test for each operation which are GET, POST, PUT and DELETE method.
 * Test that for each operation really support ETag, If-Match and If-None-Match.
 * 
 * @author Sarathit Sangtaweep 5510546182
 */
public class ETagTest {

	private static String serviceUrl;
	private static HttpClient client;
	private static ContactDao dao;

	/**
	 * Do before the class, to prepare the server and start the client.
	 * 
	 * @throws Exception
	 */
	@BeforeClass
	public static void doFirst() throws Exception {
		// Start the Jetty server.
		serviceUrl = JettyMain.startServer(8080);
		client = new HttpClient();
		client.start();
		dao = MemDaoFactory.getInstance().getContactDao();
		resetContact();
	}
	 
	/**
	 * Reset the contact list in dao.
	 */
	public static void resetContact(){
		((MemContactDao) dao).resetContact();
	}

	/**
	 * Do after class, for shutting down the server and stop the client.
	 */
	@AfterClass
	public static void doLast() {
		// stop the Jetty server after the last test
		JettyMain.stopServer();
	}

	/**
	 * Test Success GET.
	 * Should response with ETag header
	 * 
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 */
	@Test
	public void testGet() throws InterruptedException, ExecutionException, TimeoutException {
		ContentResponse res = client.GET(serviceUrl + "contacts/1");
		assertEquals("The response should be 200 OK", Status.OK.getStatusCode(), res.getStatus());
		assertTrue("Have body content", !res.getContentAsString().isEmpty());
		assertTrue("Should have ETag", res.getHeaders().containsKey("ETag"));
	}

	/**
	 * Test success POST.
	 * Should response with ETag header
	 * 
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 */
	@Test
	public void testPost() throws InterruptedException, ExecutionException, TimeoutException {
		resetContact();
		StringContentProvider content = new StringContentProvider(
				"<contact id=\"555\">" + "<title>Test Title</title>"
						+ "<name>Full Name</name>"
						+ "<email>emai@email</email>"
						+ "<phoneNumber>555555555</phoneNumber>" + "</contact>");
		Request request = client.newRequest(serviceUrl + "contacts");
		request.method(HttpMethod.POST);
		request.content(content, "application/xml");
		ContentResponse res = request.send();

		assertEquals("POST complete ,should response 201 Created", Status.CREATED.getStatusCode(), res.getStatus());
		assertTrue("POST response should have ETag", res.getHeaders().containsKey("ETag"));
	}

	/**
	 * Test GET with If-Match and If-None-Match.
	 * IF-Match should response 200 OK
	 * IF-None-Match should response 304 Not Modified.
	 * 
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 */
	@Test
	public void testGetWithETag() throws InterruptedException, ExecutionException, TimeoutException {
		ContentResponse res = client.GET(serviceUrl + "contacts/1");
		String etag = res.getHeaders().get(HttpHeader.ETAG).replace("\"", "");
		
		Request request = client.newRequest(serviceUrl + "contacts/1");
		request.method(HttpMethod.GET);
		request.header(HttpHeader.IF_NONE_MATCH, etag);
		request.accept("application/xml");
		res = request.send();
		assertEquals("Should response 304 Not Modified", Status.NOT_MODIFIED.getStatusCode(), res.getStatus());
		
		request.header(HttpHeader.IF_MATCH, etag);
		res = request.send();
		assertEquals("Should response 200 OK", Status.OK.getStatusCode(), res.getStatus());
		
		boolean isEmpty = (res.getContentAsString().isEmpty());
		assertTrue("Content should not be empty.", !isEmpty);
	}
	
	/**
	 * Test PUT with If-Match and If-None-Match.
	 * IF-Match should response 200 OK.
	 * IF-None-Match should response 412 Precondition Failed.
	 * 
	 * @throws InterruptedException
	 * @throws TimeoutException
	 * @throws ExecutionException
	 */
	@Test
	public void testPutWithETag() throws InterruptedException, TimeoutException, ExecutionException {
		//first post
		resetContact();
		StringContentProvider content = new StringContentProvider(
				"<contact id=\"555\">" + "<title>Test Title</title>"
						+ "<name>Full Name</name>"
						+ "<email>emai@email</email>"
						+ "<phoneNumber>555555555</phoneNumber>" + "</contact>");
		Request request = client.newRequest(serviceUrl + "contacts");
		request.method(HttpMethod.POST);
		request.content(content, "application/xml");
		ContentResponse res = request.send();
		
		res = client.GET(serviceUrl + "contacts/555");
		String etag = res.getHeaders().get(HttpHeader.ETAG).replace("\"", "");
		content = new StringContentProvider(
				"<contact id=\"555\">" + "<title>NEW Title</title>"
						+ "<name>NEW Full Name</name>"
						+ "<email>NEW emai@email</email>"
						+ "<phoneNumber>7777777777</phoneNumber>"
						+ "</contact>");
		
		request = client.newRequest(serviceUrl + "contacts/555");
		request.method(HttpMethod.PUT);
		request.header(HttpHeader.IF_NONE_MATCH, etag);
		request.content(content, "application/xml");
		res = request.send();
		assertEquals("PUT not success should response 412 Precondition Failed", Status.PRECONDITION_FAILED.getStatusCode(), res.getStatus());
		
		request.header(HttpHeader.IF_MATCH, etag);
		res = request.send();
		assertEquals("PUT Success response 200 OK", Status.OK.getStatusCode(), res.getStatus());
	}

	/**
	 * Test DELETE with If-Match and If-None-Match.
	 * IF-Match should response 204 No Content.
	 * IF-None-Match should response 412 Precondition Failed.
	 * 
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 */
	@Test
	public void testDeleteWithETag() throws InterruptedException, ExecutionException, TimeoutException {
		//first post
		resetContact();
		StringContentProvider content = new StringContentProvider(
				"<contact id=\"555\">" + "<title>Test Title</title>"
						+ "<name>Full Name</name>"
						+ "<email>emai@email</email>"
						+ "<phoneNumber>555555555</phoneNumber>" + "</contact>");
		Request request = client.newRequest(serviceUrl + "contacts");
		request.method(HttpMethod.POST);
		request.content(content, "application/xml");
		ContentResponse res = request.send();
		
		res = client.GET(serviceUrl + "contacts/555");
		String etag = res.getHeaders().get(HttpHeader.ETAG).replace("\"", "");
		
		request = client.newRequest(serviceUrl + "contacts/555");
		request.method(HttpMethod.DELETE);
		request.header(HttpHeader.IF_NONE_MATCH, etag);	
		res = request.send();
		assertEquals("DELETE not success should response 412 Precondition Failed", Status.PRECONDITION_FAILED.getStatusCode(), res.getStatus());
		
		request.header(HttpHeader.IF_MATCH, etag);
		res = request.send();
		assertEquals("DELETE success should response 204 No Content", Status.NO_CONTENT.getStatusCode(), res.getStatus());		
		
		res = client.GET(serviceUrl + "contacts/555");
		assertEquals("Can not get the deleted resource should response 404 Not Found", Status.NOT_FOUND.getStatusCode(), res.getStatus());		
	}

}
