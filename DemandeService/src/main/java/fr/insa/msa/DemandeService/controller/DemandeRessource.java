package fr.insa.msa.DemandeService.controller;

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

import fr.insa.msa.DemandeService.model.Demande;
import fr.insa.msa.DemandeService.model.Demande.DemandeState;
import fr.insa.msa.DemandeService.model.Demande.DemandeType;
import fr.insa.msa.DemandeService.model.User;
import fr.insa.msa.DemandeService.model.User.UserType;


@RestController
public class DemandeRessource {
	
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
			demande.setType(DemandeType.OFFRE);
			demande.setNb_personne(1);
		}else if (user.getUsertype() == UserType.UTILISATEUR) {
			demande.setType(DemandeType.AIDE);
			if (demande.getNb_personne() < 1) {
				return("Votre demande n'est pas valide car le nombre de personne doit être supérieur à 0.");
			}
		}
		
		Connection connexion = connect_db();
		Statement statement = connexion.createStatement(); 
		String query = String.format("INSERT INTO Demandes (UserId, Description, NbPersonne, Type, Etat, MotifRefus) VALUES (%d, \"%s\", %d, \"%s\", \"%s\", null);",
				id_user, demande.getDescription(), demande.getNb_personne(), demande.getType(), demande.getEtat());
		statement.executeUpdate(query);

        statement.close();
        connexion.close();
        
        String result = String.format("Votre demande de type %s a bien été ajouté et elle est en cours de traitement par l'équipe administrative. Récapitulatif de la demande : \n"
        		+ "Description : %s | Nombre de personne : %d", demande.getType(), demande.getDescription(), demande.getNb_personne());
        return result;
	}
	
	@GetMapping("/demandes/{id}")
	public Demande getDemande(@PathVariable int id) throws SQLException {
		Connection connexion = connect_db();
		Statement statement = connexion.createStatement(); 
        ResultSet resultSet = statement.executeQuery(String.format("SELECT * FROM Demandes WHERE ID = %d", id));
        Demande demande = null;
        
        if (resultSet.next()) {
        	demande = new Demande();
        	demande.setDescription(resultSet.getString("Description"));
        	demande.setNb_personne(resultSet.getInt("NbPersonne"));
        	demande.setType(DemandeType.valueOf(resultSet.getString("Type")));
        	demande.setEtat(DemandeState.valueOf(resultSet.getString("Etat")));
        	demande.setMotif_refus(resultSet.getString("MotifRefus"));
        }
        
        resultSet.close();
        statement.close();
        connexion.close();
        
        return demande;
	}
	
	@GetMapping("/{id_user}/liste_demandes")
	public String printDemandes(@PathVariable int id_user) throws SQLException {
		
		User user = restTemplate.getForObject(String.format("http://UserService/users/%d", id_user), User.class);
		String query;
		if (user == null) {
			return("L'utilisateur n'éxiste pas (normalement impossible puisqu'il faudrait être connecté sur son compte pour ajouter une demande).");
		}
		if (user.getUsertype() == UserType.BENEVOLE) {
			query = "SELECT * FROM Demandes WHERE TYPE = \"AIDE\"";
		}else if (user.getUsertype() == UserType.UTILISATEUR) {
			query = "SELECT * FROM Demandes WHERE TYPE = \"OFFRE\"";
		}else { // VALIDEUR (on retourne toutes les demandes)
			query = "SELECT * FROM Demandes"; 
		}
		
		Connection connexion = connect_db();
		Statement statement = connexion.createStatement();
        ResultSet resultSet = statement.executeQuery(query);
        String result = "Liste demandes : \n";
        while (resultSet.next()) {
        	result += String.format("Id : %d | Nombre de personne : %d | Type : \"%s\" | Etat : %s | Description : \"%s\"\n",
        			resultSet.getInt("Id"), resultSet.getInt("NbPersonne"), resultSet.getString("Type"),
        			resultSet.getString("Etat"), resultSet.getString("Description"));           
        }
        resultSet.close();
        statement.close();
        connexion.close();
        
        return result;
	}

	
	
}