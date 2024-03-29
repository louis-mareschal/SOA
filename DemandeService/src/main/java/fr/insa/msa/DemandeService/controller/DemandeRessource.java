package fr.insa.msa.DemandeService.controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import fr.insa.msa.DemandeService.model.Demande;
import fr.insa.msa.DemandeService.model.Demande.DemandeState;
import fr.insa.msa.DemandeService.model.Demande.DemandeType;
import fr.insa.msa.DemandeService.model.User;
import fr.insa.msa.DemandeService.model.User.UserType;


@RestController
public class DemandeRessource {
	
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
	
	@Autowired
	private RestTemplate restTemplate;
	
	// Pour ajouter une demande (en simulant une connexion préalable de l'utilisateur)
	@PostMapping("/{id_user}/add_demande")
	public String addDemande(@PathVariable int id_user, @RequestBody Demande demande) throws SQLException {
		
		User user = restTemplate.getForObject(String.format("http://UserService/users/%d", id_user), User.class);
		if (user == null) {
			return("L'utilisateur n'éxiste pas (normalement impossible puisqu'il faudrait être connecté sur son compte pour ajouter une demande).");
		}
		
		
		if (user.getUsertype() == UserType.BENEVOLE) { // Un bénévole ajoute une demande de type offre 
			demande.setType(DemandeType.OFFRE);
			demande.setNb_personne(1);
		}else if (user.getUsertype() == UserType.UTILISATEUR) { // Un utilisateur ajoute une demande de type aide 
			demande.setType(DemandeType.AIDE);
			if (demande.getNb_personne() < 1) { // Comme on attend un nombre de personne pour une demande d'aide, il faut un nombre >= 1
				return("Votre demande n'est pas valide car le nombre de personne doit être supérieur à 0.");
			}
		}
		
		Connection connexion = connect_db();
		PreparedStatement preparedStatement = connexion.prepareStatement("INSERT INTO Demandes (UserId, Description, NbPersonne, Type, Etat, MotifRefus) VALUES (?, ?, ?, ?, ?, ?)");
		preparedStatement.setInt(1, id_user);
		preparedStatement.setString(2, demande.getDescription());
		preparedStatement.setInt(3, demande.getNb_personne());
		preparedStatement.setString(4, demande.getType().name());
		preparedStatement.setString(5, demande.getEtat().name());
		preparedStatement.setString(6, null);
		preparedStatement.executeUpdate();

		preparedStatement.close();
        connexion.close();
        
        String result = String.format("Votre demande de type %s a bien été ajouté et elle est en cours de traitement par l'équipe administrative. Récapitulatif de la demande : \n"
        		+ "Description : %s | Nombre de personne : %d", demande.getType(), demande.getDescription(), demande.getNb_personne());
        return result;
	}
	
	// Pour récuperer une demande (avec en option l'id de son créateur à préciser, utilise pour déclarer un demande résalisée dans GestionDemande)
	@GetMapping("/demandes/{id_demande}")
	public Demande getDemande(@PathVariable int id_demande, @RequestParam(required=false) Integer id_user) throws SQLException {
		
		Connection connexion = connect_db();
		PreparedStatement preparedStatement;
		
		if (id_user != null) { 
			preparedStatement = connexion.prepareStatement("SELECT * FROM Demandes WHERE ID = ? AND UserId = ?");
			preparedStatement.setInt(1, id_demande);
			preparedStatement.setInt(2, id_user);
		}else { // Si on ne précise pas d'id_user, on récupère simplement la demande
			preparedStatement = connexion.prepareStatement("SELECT * FROM Demandes WHERE ID = ?");
			preparedStatement.setInt(1, id_demande);
		}
		
        ResultSet resultSet = preparedStatement.executeQuery();
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
        preparedStatement.close();
        connexion.close();
        
        return demande;
	}
	
	

	
	
}