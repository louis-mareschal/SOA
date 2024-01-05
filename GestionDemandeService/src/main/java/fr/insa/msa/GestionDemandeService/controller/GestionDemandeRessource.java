package fr.insa.msa.GestionDemandeService.controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import fr.insa.msa.GestionDemandeService.model.Demande;
import fr.insa.msa.GestionDemandeService.model.Demande.DemandeState;
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
	
	@PutMapping("/{id_user}/valider_demande/{id_demande}")
	public String validerDemande(@PathVariable int id_user, @PathVariable int id_demande) throws SQLException {
		
		User user = restTemplate.getForObject(String.format("http://UserService/users/%d", id_user), User.class);
		if (user == null) {
			return("L'utilisateur n'éxiste pas (normalement impossible puisqu'il faudrait être connecté sur son compte pour ajouter une demande).");
		}
		
		Demande demande = restTemplate.getForObject(String.format("http://DemandeService/demandes/%d", id_demande), Demande.class);
		if (demande == null) {
			return("La demande n'existe pas.");
		}
		
		if (user.getUsertype() != UserType.VALIDEUR) {
			return("Vous n'avez pas l'autorisation pour accepter les demandes.");
		}
		
		if (demande.getEtat() != DemandeState.EN_ATTENTE) {
			return("La demande ne peut pas être validée car elle n'est déjà plus dans l'état \"en attente\".");
		}
		
		Connection connexion = connect_db();
		PreparedStatement preparedStatement = connexion.prepareStatement("UPDATE Demandes SET Etat = ? WHERE Id = ?");
		preparedStatement.setString(1, "VALIDE");
		preparedStatement.setInt(2, id_demande);
		preparedStatement.executeUpdate();

		preparedStatement.close();
        connexion.close();
        
        String result = String.format("La demande d'id %d a bien été validée. Récapitulatif de la demande : \n"
        		+ "Description : %s | Nombre de personne : %d", id_demande, demande.getDescription(), demande.getNb_personne());
        return result;
	}
	
	@PutMapping("/mettre_demande_en_cours/{id_demande}")
	public String startDemande(@PathVariable int id_demande) throws SQLException {
		// Cette fonction n'est appelée que par le microservice InscriptionUserDemande lorsqu'une demande est complète.
		// Aucune vérification n'est nécessaire car tout est déjà fait avant l'appel à cette fonction.
		
		Connection connexion = connect_db();
		PreparedStatement preparedStatement = connexion.prepareStatement("UPDATE Demandes SET Etat = ? WHERE Id = ?");
		preparedStatement.setString(1, "EN_COURS");
		preparedStatement.setInt(2, id_demande);
		preparedStatement.executeUpdate();

		preparedStatement.close();
        connexion.close();
        
        String result = String.format("La demande d'id %d est passée dans l'état \"en cours\"", id_demande);
        return result;
	}
	
	@PutMapping("/{id_user}/refuser_demande/{id_demande}")
	public String refuserDemande(@PathVariable int id_user, @PathVariable int id_demande, @RequestBody String motif_refus) throws SQLException {
		
		System.out.println(motif_refus);
		
		User user = restTemplate.getForObject(String.format("http://UserService/users/%d", id_user), User.class);
		if (user == null) {
			return("L'utilisateur n'éxiste pas (normalement impossible puisqu'il faudrait être connecté sur son compte pour ajouter une demande).");
		}
		
		Demande demande = restTemplate.getForObject(String.format("http://DemandeService/demandes/%d", id_demande), Demande.class);
		if (demande == null) {
			return("La demande n'existe pas.");
		}
		
		if (user.getUsertype() != UserType.VALIDEUR) {
			return("Vous n'avez pas l'autorisation pour refuser les demandes.");
		}
		
		if (demande.getEtat() != DemandeState.EN_ATTENTE) {
			return("La demande ne peut pas être refusée car elle n'est déjà plus dans l'état \"en attente\".");
		}
		
		Connection connexion = connect_db();
		PreparedStatement preparedStatement = connexion.prepareStatement("UPDATE Demandes SET Etat = ?, MotifRefus = ? WHERE Id = ?");
		preparedStatement.setString(1, "REFUSE");
		preparedStatement.setString(2, motif_refus);
		preparedStatement.setInt(3, id_demande);
		preparedStatement.executeUpdate();

		preparedStatement.close();
        connexion.close();
        
        String result = String.format("La demande d'id %d a bien été refusée pour le motif : %s\n Récapitulatif de la demande : \n"
        		+ "Description : %s | Nombre de personne : %d", id_demande, motif_refus, demande.getDescription(), demande.getNb_personne());
        return result;
	}
	
	@PutMapping("/{id_user}/terminer_demande/{id_demande}")
	public String endDemande(@PathVariable int id_user, @PathVariable int id_demande) throws SQLException {
		
		User user = restTemplate.getForObject(String.format("http://UserService/users/%d", id_user), User.class);
		if (user == null) {
			return("L'utilisateur n'éxiste pas (normalement impossible puisqu'il faudrait être connecté sur son compte pour ajouter une demande).");
		}
		
		Demande demande = restTemplate.getForObject(String.format("http://DemandeService/demandes/%d?id_user=%d", id_demande, id_user), Demande.class);
		if (demande == null) {
			return("Vous n'êtes pas à l'origine de cette demande ou elle n'existe pas.");
		}
		
		if (demande.getEtat() != DemandeState.EN_COURS) {
			return("La demande ne peut pas être terminée car elle n'est déjà pas dans l'état \"en cours\".");
		}
		
		Connection connexion = connect_db();
		PreparedStatement preparedStatement = connexion.prepareStatement("UPDATE Demandes SET Etat = ? WHERE Id = ?");
		preparedStatement.setString(1, "REALISE");
		preparedStatement.setInt(2, id_demande);
		preparedStatement.executeUpdate();

		preparedStatement.close();
        connexion.close();
        
        String result = String.format("La demande d'id %d a bien été terminée. \n Récapitulatif de la demande : \n"
        		+ "Description : %s | Nombre de personne : %d", id_demande, demande.getDescription(), demande.getNb_personne());
        return result;
	}
}