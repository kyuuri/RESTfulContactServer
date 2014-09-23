package contact.entity;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Wrapper class for wrapping List.
 * A root for many contacts
 * for JAXB to do the marshalling and unmarshalling.
 * 
 * @author Sarathit Sangtaweep 5510546182
 */
@XmlRootElement(name="contactlist")
@XmlAccessorType(XmlAccessType.FIELD)
public class ContactList {
	
	/**List of the contacts*/
	@XmlElement(name="contact")
    private List<Contact> contactList;

	/**
	 * Get contact list.
	 * @return contact list.
	 */
	public List<Contact> getContacts() {
		return contactList;
	}

	/**
	 * Set the contact list.
	 * @param contactList contact list to be set.
	 */
	public void setContacts( List<Contact> contactList ) {
		this.contactList = contactList;
	}

}
