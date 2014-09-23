package contact.service.mem;

import java.io.File;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import contact.entity.Contact;
import contact.entity.ContactList;
import contact.service.ContactDao;
import contact.service.DaoFactory;

/**
 * MemDaoFactory is a factory for getting instances of entity DAO object
 * that use memory-based persistence, which isn't really persistence at all!
 * 
 * @see contact.service.DaoFactory
 * @version 2014.09.19
 * @author jim editted by Sarathit Sangtaweep 5510546182
 */
public class MemDaoFactory extends DaoFactory {
	//for my computer using C: - access denied ,so I changed to D:
	public static final String FILE_PATH = "D:/ContactServerPersistence.xml";
	
	// singleton instance of this factory
	private static MemDaoFactory factory;
	private ContactDao daoInstance;
	
	private MemDaoFactory() {
		daoInstance = new MemContactDao();
	}

	public static MemDaoFactory getInstance() {
		if (factory == null) factory = new MemDaoFactory();
		return factory;
	}

	@Override
	public ContactDao getContactDao() {
		return daoInstance;
	}

	@Override
	public void shutdown() {
		List<Contact> contactList = daoInstance.findAll();
		ContactList export = new ContactList();
		export.setContacts(contactList);
		
		try{
			JAXBContext context = JAXBContext.newInstance(ContactList.class);
			File file = new File(FILE_PATH);
			Marshaller marshaller = context.createMarshaller();
			marshaller.marshal(export, file);
		} catch(JAXBException e){
			e.printStackTrace();
		}
	}
}
