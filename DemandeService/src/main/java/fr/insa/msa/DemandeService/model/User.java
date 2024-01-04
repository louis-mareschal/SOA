package fr.insa.msa.DemandeService.model;


public class User {	
    public enum UserType {
        BENEVOLE,
        UTILISATEUR,
        VALIDEUR
    }
   
    private String name;
    private String surname;
    private int age;
    private String phone_number;
    private UserType usertype;
    
    public User() {
    }
    
	/*public User(String _name, String _surname, int _age, String _phone_number, UserType _usertype){
        this.name = _name;
        this.surname = _surname;
        this.age = _age;
        this.phone_number = _phone_number;
        this.usertype = _usertype;
        this.id = User.NEXT_ID;
    }*/
  

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
