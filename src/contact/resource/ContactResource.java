package contact.resource;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBElement;

import contact.entity.Contact;
import contact.service.ContactDao;
import contact.service.mem.MemDaoFactory;

/**
 * ContactResource provides RESTful web resources using JAX-RS
 * annotations to map requests to request handling code,
 * and to inject resources into code.
 * This ContactResource can now handle If-Match and If-None-Match
 * by using ETag.
 * 
 * @author Sarathit Sangtaweep 5510546182
 */
@Singleton
@Path("/contacts")
public class ContactResource {
	
	@Context 
	UriInfo uriInfo;
	
	private CacheControl cc;
	
	private ContactDao dao;
	
	public ContactResource(){
		dao = MemDaoFactory.getInstance().getContactDao();
		cc = new CacheControl();
		cc.setMaxAge(86400);
		cc.setPrivate(true);
	}
	
	/**
	 * Get all contacts.
	 * @return all contact(s) in the contact list.
	 */
	public Response getContacts() {
		GenericEntity<List<Contact>> ent = new GenericEntity<List<Contact>>(dao.findAll()){};
		
		EntityTag etag =  new EntityTag(ent.hashCode()+"");
		return Response.ok(ent).cacheControl(cc).tag(etag).build();
	}	

	/**
	 * Get one contact by id.
	 * @param id id of the contact
	 * @return contact with specific id
	 */
	@GET
	@Path("{id}")
	@Produces(MediaType.APPLICATION_XML)
	public Response getContact(@HeaderParam("If-Match") String match, @HeaderParam("If-None-Match") String noneMatch, @PathParam("id") long id) {
		Contact contact = dao.find(id);
		
		if(contact != null){
			EntityTag etag = new EntityTag(contact.hashCode()+"");
			
			if(match != null && noneMatch == null){
				match = match.replace("\"", "");
				
				if(!match.equals(etag.getValue())){
					return Response.notModified().build();
				}
			}
			else if(match == null && noneMatch != null){
				noneMatch = noneMatch.replace("\"", "");
				
				if(noneMatch.equals(etag.getValue())){
					return Response.notModified().build();
				}
			}
			return Response.ok(contact).cacheControl(cc).tag(etag).build();
		}
		return Response.status(Status.NOT_FOUND).build();
	}

	/**
	 * Get contact(s) whose title contains the query string (substring match).
	 * if the query is null it will return all contacts.
	 * @param query String to query
	 * @return contact(s) whose title contains the query string 
	 */
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public Response getContact(@HeaderParam("If-Match") String match, @HeaderParam("If-None-Match") String noneMatch, @QueryParam("title") String query) {
		if(query == null) return getContacts();
		
		List<Contact> list = dao.findByTitle(query);
		GenericEntity<List<Contact>> ent = new GenericEntity<List<Contact>>(list){};
		EntityTag etag = new EntityTag(ent.hashCode()+"");
		
		if(!list.isEmpty()){
		
			if(match != null && noneMatch == null){
				return Response.ok(ent).cacheControl(cc).tag(etag).build();
			}
			else if(match == null && noneMatch != null){
				noneMatch = noneMatch.replace("\"", "");
				
				if(noneMatch.equals(etag.getValue())){
					return Response.notModified().build();
				}
				else{
					return Response.ok(ent).cacheControl(cc).tag(etag).build();
				}
			}
		}
		return Response.status(Status.NOT_FOUND).build();
	}
	

	/**
	 * Create a new contact.
	 * If contact id is omitted or 0, the server will assign a unique ID and return it as the Location header.
	 * @param contact contact
	 * @return URI location
	 */
	@POST
	@Consumes(MediaType.APPLICATION_XML)
	public Response postContact(@HeaderParam("If-Match") String match, @HeaderParam("If-None-Match") String noneMatch, JAXBElement<Contact> contact) {
		Contact c = (Contact)contact.getValue();
		if(dao.find(c.getId()) == null){
			
			boolean success = dao.save(c);
			if(success){
				try {
					
					EntityTag etag = new EntityTag(c.hashCode()+"");
					return Response.created(new URI(uriInfo.getAbsolutePath() + "" + c.getId()))
							.type(MediaType.APPLICATION_XML).entity(c).cacheControl(cc).tag(etag).build();
					
				} catch (URISyntaxException e) {}
			}
			return Response.status(Status.BAD_REQUEST).build();
		}
		else{
			return Response.status(Status.CONFLICT).location(uriInfo.getRequestUri()).entity(c).build();
		}
		
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
	public Response putContact(@HeaderParam("If-Match") String match, @HeaderParam("If-None-Match") String noneMatch, @PathParam("id") long id, JAXBElement<Contact> contact){
		
		Contact c = dao.find(id);
		Contact update = (Contact)contact.getValue();
		boolean success = false;
		
		
		if(c != null){
			c.forceApplyUpdate(update);
			EntityTag etag = new EntityTag(c.hashCode()+"");
			
			if(match != null && noneMatch == null){
				match = match.replace("\"", "");
				
				if(!match.equals(etag.getValue())){
					return Response.status(Status.PRECONDITION_FAILED).build();
				}
			}
			else if(match == null && noneMatch != null){
				noneMatch = noneMatch.replace("\"", "");
				
				if(noneMatch.equals(etag.getValue())){
					return Response.status(Status.PRECONDITION_FAILED).build();
				}
			}
			
			if(id == update.getId()){
				success = dao.update(c);
			}
			if(success){
				return Response.ok(uriInfo.getAbsolutePath()+"").build();
			}
			Response.status(Status.BAD_REQUEST).build();
		}
		return Response.status(Status.NOT_FOUND).build();
	}
	
	/**
	 * Delete a contact with the matching id.
	 * @param id id
	 * @return message for deleted id.
	 */
	@DELETE
	@Path("{id}")
	@Produces(MediaType.APPLICATION_XML)
	public Response deleteContact(@HeaderParam("If-Match") String match, @HeaderParam("If-None-Match") String noneMatch, @PathParam("id") long id){
		Contact c = dao.find(id);
		boolean success = false;
		
		if(c != null){
			EntityTag etag = new EntityTag(c.hashCode()+"");
			
			if(match != null && noneMatch == null){
				match = match.replace("\"", "");
				
				if(!match.equals(etag.getValue())){
					return Response.status(Status.PRECONDITION_FAILED).build();
				}
			}
			else if(match == null && noneMatch != null){
				noneMatch = noneMatch.replace("\"", "");
				
				if(noneMatch.equals(etag.getValue())){
					return Response.status(Status.PRECONDITION_FAILED).build();
				}
			}
			
			success = dao.delete(id);
			if(success){
				return Response.ok().build();
			}
		}
		return Response.status(Status.BAD_REQUEST).build();
	}
}

