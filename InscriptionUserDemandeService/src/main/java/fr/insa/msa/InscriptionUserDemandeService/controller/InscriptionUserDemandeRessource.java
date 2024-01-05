package fr.insa.msa.InscriptionUserDemandeService.controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import fr.insa.msa.InscriptionUserDemandeService.model.Demande;
import fr.insa.msa.InscriptionUserDemandeService.model.Demande.DemandeState;
import fr.insa.msa.InscriptionUserDemandeService.model.Demande.DemandeType;
import fr.insa.msa.InscriptionUserDemandeService.model.User;
import fr.insa.msa.InscriptionUserDemandeService.model.User.UserType;


@RestController
public class InscriptionUserDemandeRessource {
	
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
	
	@PostMapping("/{id_user}/inscription_demande/{id_demande}")
	public String inscription_demande(@PathVariable int id_user, @PathVariable int id_demande) throws SQLException {
		
		User user = restTemplate.getForObject(String.format("http://UserService/users/%d", id_user), User.class);
		if (user == null) {
			return("L'utilisateur n'existe pas (normalement impossible puisqu'il faudrait être connecté sur son compte pour ajouter une demande).");
		}
		Demande demande = restTemplate.getForObject(String.format("http://DemandeService/demandes/%d", id_demande), Demande.class);
		if (demande == null) {
			return("La demande n'existe pas.");
		}
		
		if (user.getUsertype() == UserType.BENEVOLE && demande.getType() != DemandeType.AIDE) {
			return("Vous êtes un bénévole, vous pouvez vous inscrire que sur les demandes de type \"Aide\"");
		}else if (user.getUsertype() == UserType.UTILISATEUR && demande.getType() != DemandeType.OFFRE) {
			return("Vous êtes un utilisateur, vous pouvez vous inscrire que sur les demandes de type \"Offre\"");
		}
		if (demande.getEtat() != DemandeState.VALIDE) {
			return("La demande n'est pas de l'état \"validée\" donc il n'est pas possible de s'inscrire.");
		}
		
		Connection connexion = connect_db();
		Statement statement_1 = connexion.createStatement(); 
		String query_1 = String.format("SELECT * FROM DemandesUsers WHERE DemandeId = %d AND UserId = %d", id_demande, id_user);
		ResultSet resultSet_1 = statement_1.executeQuery(query_1);
		
		Vector<Integer> userIds = new Vector<>();
		
		while (resultSet_1.next()) {
			userIds.add(resultSet_1.getInt("UserId"));
		}
		
		resultSet_1.close();
		statement_1.close();
		
		
		if (userIds.contains(id_user)) {
			return("Vous êtes déjà inscrit pour cette demande.");
		}
		if (userIds.size() == demande.getNb_personne()) {
			return("Il n'y a plus de place pour cette demande.");
		}
		
		
		Statement statement_2 = connexion.createStatement(); 
		String query_2 = String.format("INSERT INTO DemandesUsers (DemandeId, UserId) VALUES (%d, %d);", id_demande, id_user);
		statement_2.executeUpdate(query_2);
		userIds.add(id_user);
		
		statement_2.close();
        connexion.close();
        
        String result = String.format("Vous avez bien été inscrit à la demande n°%d. ", id_demande);
        
        if (userIds.size() == demande.getNb_personne()) {
    		result += restTemplate.getForObject(String.format("http://GestionDemandeService/mettre_demande_en_cours/%d", id_demande), String.class);
    		// Appel au micro serice de messagerie pour envoyer un message à tous les utilisateurs concernés par cette demande ainsi qu'au créateur de la demande.
    		
		}else {
			result += "Un message vous sera envoyé quand elle passera en statut \"en cours\".";
		}
        result += String.format("\nRécapitulatif de la demande : \n Description : %s | Nombre de personne : %d",
        		demande.getDescription(), demande.getNb_personne());
	
        return result;
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