package contact.service.mem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import contact.entity.Contact;
import contact.entity.ContactList;
import contact.service.ContactDao;

/**
 * Data access object for saving and retrieving contacts.
 * This DAO uses an in-memory list of contacts, which may
 * be lost when the application exits.
 * Use DaoFactory to get an instance of this class, such as:
 * dao = DaoFactory.getInstance().getContactDao()
 * 
 * @author jim editted by Sarathit Sangtaweep 5510546182
 */
public class MemContactDao implements ContactDao {
	private List<Contact> contacts;
	private AtomicLong nextId;
	
	public MemContactDao() {
		contacts = new ArrayList<Contact>();
		nextId = new AtomicLong(1000L);
		this.importFile(MemDaoFactory.FILE_PATH);
		if(contacts.size() == 0)
			createTestContact(1);
	}
	
	/** add a single contact with given id for testing. */
	private void createTestContact(long id) {
		Contact test = new Contact("Test contact", "Joe Experimental", "none@testing.com", "123456789");
		test.setId(id);
		contacts.add(test);
	}

	/** Find a contact by ID in contacts.
	 * @param the id of contact to find
	 * @return the matching contact or null if the id is not found
	 */
	public Contact find(long id) {
		for(Contact c : contacts) 
			if (c.getId() == id) return c;
		return null;
	}

	/**
	 * Find all contacts
	 * @return all contacts
	 */
	public List<Contact> findAll() {
		return java.util.Collections.unmodifiableList(contacts);
	}

	/**
	 * Delete a saved contact.
	 * @param id the id of contact to delete
	 * @return true if contact is deleted, false otherwise.
	 */
	public boolean delete(long id) {
		for(int k=0; k<contacts.size(); k++) {
			if (contacts.get(k).getId() == id) {
				contacts.remove(k);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Save or replace a contact.
	 * If the contact.id is 0 then it is assumed to be a
	 * new (not saved) contact.  In this case a unique id
	 * is assigned to the contact.  
	 * If the contact.id is not zero and the contact already
	 * exists in saved contacts, the old contact is replaced.
	 * @param contact the contact to save or replace.
	 * @return true if saved successfully
	 */
	public boolean save(Contact contact) {
		if (contact.getId() == 0) {
			contact.setId( getUniqueId() );
			return contacts.add(contact);
		}
		// check if this contact is already in persistent storage
		Contact other  = find(contact.getId());
		if (other == contact) return true;
		if ( other != null ) contacts.remove(other);
		return contacts.add(contact);
	}

	/**
	 * Update a Contact.  Only the non-null fields of the
	 * update are applied to the contact.
	 * @param update update info for the contact.
	 * @return true if the update is applied successfully.
	 */
	public boolean update(Contact update) {
		Contact contact = find(update.getId());
		if(contact == null ) return false;
		contact.forceApplyUpdate(update);
		save(contact);
		return true;
	}
	
	/**
	 * Get a unique contact ID.
	 * @return unique id not in persistent storage
	 */
	private synchronized long getUniqueId() {
		long id = nextId.getAndAdd(1L);
		while( id < Long.MAX_VALUE ) {	
			if (find(id) == null) return id;
			id = nextId.getAndAdd(1L);
		}
		return id; // this should never happen
	}
	
	/**
	 * Find the list of the contact which contains the query string.
	 * (title matched with q)
	 * @param q the query string
	 * @return list of the contact which contains the query string.
	 */
	public List<Contact> findByTitle(String q){
		List<Contact> list = new ArrayList<Contact>();
		for(int i = 0 ; i < contacts.size() ; i++){
			if(contacts.get(i).getTitle().contains(q)){
				list.add(contacts.get(i));
			}
		}
		return list;
	}
	
	/**
	 * Import the existing file for getting the saved contacts.
	 * By using JAXB to unmarshal.
	 * @param path path of the importing file.
	 */
	public void importFile(String path){
		JAXBContext context;
		try {
			context = JAXBContext.newInstance(ContactList.class);
			Unmarshaller um = context.createUnmarshaller();
			
			File file = new File(path);
			Object obj = um.unmarshal( file );
			ContactList contactList = (ContactList)obj;
			
			this.contacts = contactList.getContacts();
		} catch (Exception e) {}
	}
	
	/**
	 * Reset all contacts in the list.
	 * Use for testing only (For Unit Test to prevent posted contact)
	 */
	public void resetContact(){
		contacts = new ArrayList<Contact>();
		nextId = new AtomicLong(1000L);
		createTestContact(1);
	}

}
