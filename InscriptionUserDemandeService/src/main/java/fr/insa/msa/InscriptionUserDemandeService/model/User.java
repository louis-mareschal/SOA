package fr.insa.msa.InscriptionUserDemandeService.model;


public class User {	
	 
    // Attributes
 
    private String name;
    private String surname;
    private int age;
    private String phone_number;
    private UserType usertype;
    
    // Constructor
    
    public User() {
    }
    
    // Definition enum
    
    public enum UserType {
        BENEVOLE,
        UTILISATEUR,
        VALIDEUR
    }
  
    // Getters and Setters
    
    public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public String getPhone_number() {
		return phone_number;
	}

	public void setPhone_number(String phone_number) {
		this.phone_number = phone_number;
	}

	public UserType getUsertype() {
		return usertype;
	}

	public void setUsertype(UserType usertype) {
		this.usertype = usertype;
	}

}
