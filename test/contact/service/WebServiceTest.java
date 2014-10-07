package contact.service;

import static org.junit.Assert.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.core.Response.Status;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import contact.jetty.JettyMain;
import contact.service.mem.MemContactDao;
import contact.service.mem.MemDaoFactory;

/**
 * JUnit Test class for testing ContactResource
 * There are 2 test for each operation which are GET, POST, PUT and DELETE method.
 * For each two, one is success and another one is fail.
 * 
 * @author Sarathit Sangtaweep 5510546182
 */
public class WebServiceTest {

	 private static String serviceUrl;
	 private static HttpClient client;
	 private static ContactDao dao;
	 
	 /**
	  * Do before the class, to prepare the server
	  * and start the client.
	  * @throws Exception
	  */
	 @BeforeClass
	 public static void doFirst() throws Exception {
		 // Start the Jetty server. 
		 serviceUrl = JettyMain.startServer( 8080 );
		 //System.out.println(serviceUrl);
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
	  * Do after class, for shutting down the server
	  * and stop the client.
	  */
	 @AfterClass
	 public static void doLast( ) {
		 // stop the Jetty server after the last test
		 JettyMain.stopServer();
	 }
	 
	 
	 /**
	  * Test Success GET.
	  * @throws InterruptedException
	  * @throws ExecutionException
	  * @throws TimeoutException
	  */
	 @Test
	 public void testGetPass() throws InterruptedException, ExecutionException, TimeoutException {
		 ContentResponse res = client.GET(serviceUrl+"contacts/1");
		 assertEquals("The response should be 200 OK", Status.OK.getStatusCode(), res.getStatus());
		 assertTrue("Have body content", !res.getContentAsString().isEmpty());
	 }
	 
	 /**
	  * Test Fail GET.
	  * @throws InterruptedException
	  * @throws ExecutionException
	  * @throws TimeoutException
	  */
	 @Test
	 public void testGetFail() throws InterruptedException, ExecutionException, TimeoutException {
		 ContentResponse res = client.GET(serviceUrl+"contacts/999");
		 assertEquals("The response should be 404 Not Found", Status.NOT_FOUND.getStatusCode(), res.getStatus());
	 }

	 /**
	  * Test success POST.
	  * @throws InterruptedException
	  * @throws ExecutionException
	  * @throws TimeoutException
	  */
	 @Test
	 public void testPostPass() throws InterruptedException, ExecutionException, TimeoutException {
		 resetContact();
		 StringContentProvider content = new StringContentProvider("<contact id=\"555\">" +
					"<title>Test Title</title>" +
					"<name>Full Name</name>" +
					"<email>emai@email</email>" +
					"<phoneNumber>555555555</phoneNumber>"+
					"</contact>");
		 Request request = client.newRequest(serviceUrl+"contacts");
		 request.method(HttpMethod.POST);
		 request.content(content, "application/xml");
		 ContentResponse res = request.send();
		
		 assertEquals("POST complete ,should response 201 Created", Status.CREATED.getStatusCode(), res.getStatus());
		 res = client.GET(serviceUrl+"contacts/555");
		 assertTrue("Check by using GET ,request posted id.", !res.getContentAsString().isEmpty() );
	 }
	 
	 /**
	  * Test Fail Post.
	  * @throws InterruptedException
	  * @throws TimeoutException
	  * @throws ExecutionException
	  */
	 @Test
	 public void testPostFail() throws InterruptedException, TimeoutException, ExecutionException {
		 //first post
		 resetContact();
		 StringContentProvider content = new StringContentProvider("<contact id=\"555\">" +
					"<title>Test Title</title>" +
					"<name>Full Name</name>" +
					"<email>emai@email</email>" +
					"<phoneNumber>555555555</phoneNumber>"+
					"</contact>");
		 Request request = client.newRequest(serviceUrl+"contacts");
		 request.method(HttpMethod.POST);
		 request.content(content, "application/xml");
		 ContentResponse res = request.send();
		 
		 content = new StringContentProvider("<contact id=\"555\">" +
					"<title>Test Title</title>" +
					"<name>Full Name</name>" +
					"<email>emai@email</email>" +
					"<phoneNumber>555555555</phoneNumber>"+
					"</contact>");
		 request = client.newRequest(serviceUrl+"contacts");
		 request.method(HttpMethod.POST);
		 request.content(content, "application/xml");
		 res = request.send();
		 
		 assertEquals("Should response CONFLICT because the id is already exist", Status.CONFLICT.getStatusCode(), res.getStatus());
	 }

	 /**
	  * Test success PUT
	  * @throws InterruptedException
	  * @throws TimeoutException
	  * @throws ExecutionException
	  */
	 @Test
	 public void testPutPass() throws InterruptedException, TimeoutException, ExecutionException {
		 StringContentProvider content = new StringContentProvider("<contact id=\"555\">" +
					"<title>NEW Title</title>" +
					"<name>NEW Full Name</name>" +
					"<email>NEW emai@email</email>" +
					"<phoneNumber>7777777777</phoneNumber>"+
					"</contact>");
		 Request request = client.newRequest(serviceUrl+"contacts/555");
		 request.method(HttpMethod.PUT);
		 request.content(content, "application/xml");
		 ContentResponse res = request.send();
		 
		 assertEquals("PUT Success should response 200 OK", Status.OK.getStatusCode(), res.getStatus());
	 }
	 
	 /**
	  * Test Fail PUT
	  * @throws InterruptedException
	  * @throws TimeoutException
	  * @throws ExecutionException
	  */
	 @Test
	 public void testPutFail() throws InterruptedException, TimeoutException, ExecutionException {
		//first post
		 resetContact();
		 StringContentProvider content = new StringContentProvider("<contact id=\"555\">" +
					"<title>Test Title</title>" +
					"<name>Full Name</name>" +
					"<email>emai@email</email>" +
					"<phoneNumber>555555555</phoneNumber>"+
					"</contact>");
		 Request request = client.newRequest(serviceUrl+"contacts");
		 request.method(HttpMethod.POST);
		 request.content(content, "application/xml");
		 ContentResponse res = request.send();
		 
		 content = new StringContentProvider("<contact id=\"555\">" +
					"<title>NEW Title</title>" +
					"<name>NEW Full Name</name>" +
					"<email>NEW emai@email</email>" +
					"<phoneNumber>7777777777</phoneNumber>"+
					"</contact>");
		 request = client.newRequest(serviceUrl+"contacts/557");
		 request.method(HttpMethod.PUT);
		 request.content(content, "application/xml");
		 res = request.send();
		 
		 assertEquals("PUT Fail should response 404 Not Found", Status.NOT_FOUND.getStatusCode(), res.getStatus());
	 }
	 
	 /**
	  * Test success DELETE
	  * @throws InterruptedException
	  * @throws ExecutionException
	  * @throws TimeoutException
	  */
	 @Test
	 public void testDeletePass() throws InterruptedException, ExecutionException, TimeoutException {
		//first post
		 resetContact();
		 StringContentProvider content = new StringContentProvider("<contact id=\"555\">" +
					"<title>Test Title</title>" +
					"<name>Full Name</name>" +
					"<email>emai@email</email>" +
					"<phoneNumber>555555555</phoneNumber>"+
					"</contact>");
		 Request request = client.newRequest(serviceUrl+"contacts");
		 request.method(HttpMethod.POST);
		 request.content(content, "application/xml");
		 ContentResponse res = request.send();
		 
		 request = client.newRequest(serviceUrl+"contacts/555");
		 request.method(HttpMethod.DELETE);
		 res = request.send();
		 
		 assertEquals("DELETE success should response 200 OK", Status.OK.getStatusCode(), res.getStatus());
		 res = client.GET(serviceUrl+"contacts/555");

		 res = client.GET(serviceUrl + "contacts/555");
		 assertEquals("Can not get the deleted resource should response 404 Not Found", Status.NOT_FOUND.getStatusCode(), res.getStatus());		
	 }
	 
	 /**
	  * Test fail DELETE
	  * @throws InterruptedException
	  * @throws TimeoutException
	  * @throws ExecutionException
	  */
	 @Test
	 public void testDeleteFail() throws InterruptedException, TimeoutException, ExecutionException {
		 Request request = client.newRequest(serviceUrl+"contacts/999");
		 request.method(HttpMethod.DELETE);
		 ContentResponse res = request.send();
		 
		 assertEquals("Contact does not exist should response 400 Bad Request", Status.BAD_REQUEST.getStatusCode(), res.getStatus());
	 }


}
