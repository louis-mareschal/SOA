package fr.insa.msa.GestionDemandeService.controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import fr.insa.msa.GestionDemandeService.model.Demande;
import fr.insa.msa.GestionDemandeService.model.Demande.demandeType;
import fr.insa.msa.GestionDemandeService.model.User;
import fr.insa.msa.GestionDemandeService.model.User.UserType;


@RestController
public class GestionDemandeRessource {
	
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
	
	@Autowired
	private RestTemplate restTemplate;
	
	@PostMapping("/{id_user}/add_demande")
	public String addDemande(@PathVariable int id_user, @RequestBody Demande demande) throws SQLException {
		
		User user = restTemplate.getForObject(String.format("http://UserService/users/%d", id_user), User.class);
		if (user == null) {
			return("L'utilisateur n'éxiste pas (normalement impossible puisqu'il faudrait être connecté sur son compte pour ajouter une demande).");
		}
		if (user.getUsertype() == UserType.BENEVOLE) {
			demande.setType(demandeType.OFFRE);
			demande.setNb_personne(1);
		}else if (user.getUsertype() == UserType.UTILISATEUR) {
			demande.setType(demandeType.AIDE);
			if (demande.getNb_personne() < 1) {
				return("Votre demande n'est pas valide car le nombre de personne doit être supérieur à 0.");
			}
		}
		
		Connection connexion = connect_db();
		Statement statement = connexion.createStatement(); 
		String query = String.format("INSERT INTO Demandes (Description, NbPersonne, Type, Etat, MotifRefus) VALUES (\"%s\", %d, \"%s\", \"%s\", null);", demande.getDescription(), demande.getNb_personne(), demande.getType(), demande.getEtat());
		statement.executeUpdate(query);

        statement.close();
        connexion.close();
        
        String result = String.format("Votre demande de type %s a bien été ajouté et elle est en cours de traitement par l'équipe administrative. Récapitulatif de la demande : \n"
        		+ "Description : %s | Nombre de personne : %d", demande.getType(), demande.getDescription(), demande.getNb_personne());
        return result;
	}
	
	
	
	/*@GetMapping(value="/demande")
	public void getOneUser() {
		
		
		restTemplate.getForObject("http://UserService/users/1", User.class);
		
	}*/
	
	@GetMapping(value="/test")
	public void printDemande() throws SQLException {
		Connection connexion = connect_db();
		Statement statement = connexion.createStatement(); 
		// Exemple de requête de lecture
        ResultSet resultSet = statement.executeQuery("SELECT * FROM Demandes");

        while (resultSet.next()) {
        	System.out.println("La demande avec l'id "+ resultSet.getString(1) + " a pour description " + resultSet.getString(2));           
        }
        resultSet.close();
        statement.close();
        connexion.close();
	}

	
	
}