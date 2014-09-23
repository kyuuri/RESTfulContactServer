package contact.entity;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * 
 * @author Sarathit Sangtaweep 5510546182
 */
@XmlRootElement(name="contactlist")
@XmlAccessorType(XmlAccessType.FIELD)
public class ContactList {
	
	@XmlElement(name="contact")
    private List<Contact> contactList;

	public List<Contact> getContacts() {
		return contactList;
	}

	public void setContacts( List<Contact> contactList ) {
		this.contactList = contactList;
	}

}
