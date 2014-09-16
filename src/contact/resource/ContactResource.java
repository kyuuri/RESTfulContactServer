package contact.resource;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBElement;

import contact.entity.Contact;
import contact.service.ContactDao;
import contact.service.DaoFactory;

/**
 * ContactResource provides RESTful web resources using JAX-RS
 * annotations to map requests to request handling code,
 * and to inject resources into code.
 * 
 * @author Sarathit Sangtaweep 5510546182
 */
@Singleton
@Path("/contacts")
public class ContactResource {
	
	private ContactDao dao;
	
	public ContactResource(){
		dao = DaoFactory.getInstance().getContactDao();
	}
	
	/**
	 * Get all contacts.
	 * @return all contact(s) in the contact list.
	 */
	public Response getContacts() {
		GenericEntity<List<Contact>> ent = new GenericEntity<List<Contact>>(dao.findAll()){};
		return Response.ok(ent).build();
	}	

	/**
	 * Get one contact by id.
	 * @param id id of the contact
	 * @return contact with specific id
	 */
	@GET
	@Path("{id}")
	@Produces(MediaType.APPLICATION_XML)
	public Response getContact(@PathParam("id") long id) {
		return Response.ok(dao.find(id)).build();
	}

	/**
	 * Get contact(s) whose title contains the query string (substring match).
	 * if the query is null it will return all contacts.
	 * @param query String to query
	 * @return contact(s) whose title contains the query string 
	 */
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public Response getContact(@QueryParam("q") String query) {
		GenericEntity<List<Contact>> ent = new GenericEntity<List<Contact>>(dao.findAll()){};
		if(query == null) return getContacts();
		
		ent = new GenericEntity<List<Contact>>(dao.findByTitle(query)){};
		return Response.ok(ent).build();
	}
	

	/**
	 * Create a new contact.
	 * If contact id is omitted or 0, the server will assign a unique ID and return it as the Location header.
	 * @param contact contact
	 * @return URI location
	 */
	@POST
	@Consumes(MediaType.APPLICATION_XML)
	public Response putContact(JAXBElement<Contact> contact) {
		Contact c = (Contact)contact.getValue();
		dao.save(c);
		try {
			return Response.created(new URI("localhost:8080/contacts/" + c.getId())).type(MediaType.APPLICATION_XML).entity(contact).build();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Update a contact. Only update the attributes supplied in request body.
	 * @param id id
	 * @param contact contact
	 * @return URI location or no content if the updating contact is null.
	 */
	@PUT
	@Path("{id}")
	@Consumes(MediaType.APPLICATION_XML)
	public Response postContact( @PathParam("id") long id, JAXBElement<Contact> contact){
		Contact c = (Contact)contact.getValue();
		if(dao.update(c)){
			try {
				return Response.created(new URI("localhost:8080/contacts/"+id)).build();
			} catch (URISyntaxException e) {}
		}
		return Response.noContent().build();
	}
	
	/**
	 * Delete a contact with the matching id.
	 * @param id id
	 * @return message for deleted id.
	 */
	@DELETE
	@Path("{id}")
	@Produces(MediaType.APPLICATION_XML)
	public Response deleteContact( @PathParam("id") long id){
		dao.delete(id);
		return Response.ok().entity(id + "deleted.").build();
	}
}

