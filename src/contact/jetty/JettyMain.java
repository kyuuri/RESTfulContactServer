package contact.jetty;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ServerProperties;

/**
 * Main class for running the application and set path of the url
 * which link to the resource.
 * 
 *@author Sarathit Sangtaweep 5510546182
 */
public class JettyMain {
	
	/** 
	 * The default port to listen on. Typically 80 or 8080.  
	 */
	static final int PORT = 8080;

	/**
	 * Create a Jetty server and a context, add Jetty ServletContainer
	 * which dispatches requests to JAX-RS resource objects,
	 * and start the Jetty server.
	 * 
	 * @param args not used
	 * @throws Exception if Jetty server encounters any problem
	 */
	public static void main(String[] args) throws Exception {
		int port = PORT;  // the port the server will listen to for HTTP requests
		Server server = new Server( port );
		
		ServletContextHandler context = new ServletContextHandler( ServletContextHandler.SESSIONS );
		context.setContextPath("/");
		
		ServletHolder holder = new ServletHolder( org.glassfish.jersey.servlet.ServletContainer.class );
		
		holder.setInitParameter(ServerProperties.PROVIDER_PACKAGES, "contact.resource");
		context.addServlet( holder, "/*" );

		server.setHandler( context );
		
		System.out.println("Starting Jetty server on port " + port);
		server.start();
		
		System.out.println("Server started.  Press ENTER to stop it.");
		int ch = System.in.read();
		System.out.println("Stopping server.");
		server.stop();
	}
	
}

