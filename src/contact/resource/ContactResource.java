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
 * 
 * 
 */
@Singleton
@Path("/contacts")
public class ContactResource {
	
	private ContactDao dao;
	
	public ContactResource(){
		dao = DaoFactory.getInstance().getContactDao();
	}

	@GET
	@Path("{id}")
	@Produces(MediaType.APPLICATION_XML)
	public Response getContact(@PathParam("id") long id) {
		return Response.ok(dao.find(id)).build();
	}

	@GET
	@Produces(MediaType.APPLICATION_XML)
	public Response getContact(@QueryParam("q") String query) {
		GenericEntity<List<Contact>> ent = new GenericEntity<List<Contact>>(dao.findAll()){};
		if(query == null) return Response.ok(ent).build();
		
		ent = new GenericEntity<List<Contact>>(dao.findByTitle(query)){};
		return Response.ok(ent).build();
	}
	

	@POST
	@Consumes(MediaType.APPLICATION_XML)
	public Response putContact(JAXBElement<Contact> contact) {
		Contact c = (Contact)contact.getValue();
		dao.save(c);
		try {
			return Response.created(new URI(c.getId()+"")).type(MediaType.APPLICATION_XML).entity(contact).build();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@PUT
	@Path("{id}")
	@Consumes(MediaType.APPLICATION_XML)
	public Response postContact( @PathParam("id") long id, JAXBElement<Contact> contact){
		Contact c = (Contact)contact.getValue();
		if(dao.update(c)){
			return Response.ok().type(MediaType.APPLICATION_XML).entity(c).build();
		}
		return Response.noContent().build();
	}
	
	@DELETE
	@Path("{id}")
	@Produces(MediaType.APPLICATION_XML)
	public Response deleteContact( @PathParam("id") long id){
		dao.delete(id);
		return Response.ok().entity(id + "deleted.").build();
	}
}

