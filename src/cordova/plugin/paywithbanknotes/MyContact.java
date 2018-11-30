package cordova.plugin.paywithbanknotes;

public class MyContact {
	
	public String name;
	public String lastName;
	public long id;
	public String iban;

	public MyContact(long id, String name, String lastName) {
		this.id = id;
		this.name = name;
		this.lastName = lastName;
	}

	public MyContact(String name, String lastName) {
		this.name = name;
		this.lastName = lastName;
	}

}
