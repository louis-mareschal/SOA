package fr.insa.msa.UserService.controller;

import org.springframework.web.bind.annotation.RestController;

import fr.insa.msa.UserService.model.User;
import fr.insa.msa.UserService.model.User.UserType;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;


@RestController
public class UserRessource {
	
	// Pour se connecter à la base de données
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
	
	// Pour inscrire un utilisateur 
	@PostMapping("/user_sign_up")
	public String addUser(@RequestBody User user) throws SQLException {
		
		Connection connexion = connect_db();
		//Statement statement = connexion.createStatement(); 
		PreparedStatement preparedStatement = connexion.prepareStatement("INSERT INTO Users (Name, Surname, Age, PhoneNumber, UserType) VALUES (?, ?, ?, ?, ?");
		preparedStatement.setString(1, user.getName());
		preparedStatement.setString(2, user.getSurname());
		preparedStatement.setInt(3, user.getAge());
		preparedStatement.setString(4, user.getPhone_number());
		preparedStatement.setString(5, user.getUsertype().name());
		//String query = String.format("INSERT INTO Users (Name, Surname, Age, PhoneNumber, UserType) VALUES (\"%s\", \"%s\", %d, \"%s\", \"%s\");",
		//		user.getName(), user.getSurname(), user.getAge(), user.getPhone_number(), user.getUsertype());
		//statement.executeUpdate(query);
		preparedStatement.executeUpdate();
        //statement.close();
        connexion.close();
        
        String result = String.format("Bonjour %s %s et merci pour votre inscription. Récapitulatif de vos informations : \n"
        		+ "Age : %d | Phone Number : %s | User Type : %s.",
        		user.getName(), user.getSurname(), user.getAge(), user.getPhone_number(), user.getUsertype());
        
        return result;
	}
	
	// Pour récuperer un utilisateur
	@GetMapping(value="/users/{id}")
	public User printUser(@PathVariable int id) throws SQLException {
		
		Connection connexion = connect_db();
		//Statement statement = connexion.createStatement(); 
        //ResultSet resultSet = statement.executeQuery(String.format("SELECT * FROM Users WHERE ID = %d", id));
		PreparedStatement preparedStatement = connexion.prepareStatement("SELECT * FROM Users WHERE ID = ?");
		preparedStatement.setInt(1, id);
		ResultSet resultSet = preparedStatement.executeQuery();
		User user = null;
        
        if (resultSet.next()) {
        	user = new User();
        	user.setName(resultSet.getString("Name"));
        	user.setSurname(resultSet.getString("Surname"));
        	user.setAge(resultSet.getInt("Age"));
        	user.setPhone_number(resultSet.getString("PhoneNumber"));
        	user.setUsertype(UserType.valueOf(resultSet.getString("UserType")));
        }
        
        resultSet.close();
        preparedStatement.close();
        //statement.close();
        connexion.close();
        
        return user;
	}
}
