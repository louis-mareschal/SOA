package fr.insa.msa.UserService.controller;

import org.springframework.web.bind.annotation.RestController;

import fr.insa.msa.UserService.model.User;
import fr.insa.msa.UserService.model.User.UserType;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;


@RestController
public class UserRessource {
	
	public Connection connect_db() {
		try {
	        String url = "jdbc:mysql://srv-bdens.insa-toulouse.fr:3306/projet_gei_063?serverTimezone=UTC";
	        String utilisateur = "projet_gei_063";
	        String motDePasse = "Iedo7quo";

	        Connection connexion = DriverManager.getConnection(url, utilisateur, motDePasse);

	        return connexion;

	    } catch ( SQLException e) {
	        e.printStackTrace();
	    }
		return null; 
	}
	
	
	
	
	@PostMapping("/user_sign_up")
	public String addUser(@RequestBody User user) throws SQLException {
		Connection connexion = connect_db();
		Statement statement = connexion.createStatement(); 
		//System.out.println(user.getName()+user.getUsertype());
		//User user = new User(name, surname, age, phone_number, usertype);
		String query = String.format("INSERT INTO Users (Name, Surname, Age, PhoneNumber, UserType) VALUES (\"%s\", \"%s\", %d, \"%s\", \"%s\");", user.getName(), user.getSurname(), user.getAge(), user.getPhone_number(), user.getUsertype());
		//System.out.println(query);
		statement.executeUpdate(query);

		
        statement.close();
        connexion.close();
        
        String result = String.format("Bonjour %s %s et merci pour votre inscription. Récapitulatif de vos informations : \n"
        		+ "Age : %d | Phone Number : %s | User Type : %s.",
        		user.getName(), user.getSurname(), user.getAge(), user.getPhone_number(), user.getUsertype());
        
        return result;
	}
	
	@GetMapping(value="/users/{id}")
	public User printUser(@PathVariable int id) throws SQLException {
		Connection connexion = connect_db();
		Statement statement = connexion.createStatement(); 
		// Exemple de requête de lecture
        ResultSet resultSet = statement.executeQuery(String.format("SELECT * FROM Users WHERE ID = %d", id));
        User user = null;
        
        if (resultSet.next()) {
        	user = new User();
        	//System.out.println("L'utilisateur avec l'id "+ resultSet.getString(1) + " s'appelle " + resultSet.getString(2) + " " + resultSet.getString(3)+ " "+resultSet.getString(6));           
        	user.setName(resultSet.getString("Name"));
        	user.setSurname(resultSet.getString("Surname"));
        	user.setAge(resultSet.getInt("Age"));
        	user.setPhone_number(resultSet.getString("PhoneNumber"));
        	user.setUsertype(UserType.valueOf(resultSet.getString("UserType")));
        }
        
        resultSet.close();
        statement.close();
        connexion.close();
        
        return user;
	}
}
