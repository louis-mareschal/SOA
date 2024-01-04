package fr.insa.msa.GestionDemandeService.model;

public class Demande {
	// Attributes
	private String description ;
	private int nb_personne ;
	private DemandeType type ;
	private DemandeState etat ;
	private String motif_refus ;	
	
	// Constructors
	
	public Demande(){
		this.etat = DemandeState.EN_ATTENTE;
	}
	
	// Definition enums
	
	public enum DemandeType {
        AIDE,
        OFFRE
    }
	
	public enum DemandeState {
        EN_ATTENTE,
        VALIDE,
        REFUSE,
        EN_COURS,
        REALISE
    }
	
	// Getters and setters
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public DemandeState getEtat() {
		return etat;
	}

	public void setEtat(DemandeState etat) {
		this.etat = etat;
	}
	
	public String getMotif_refus() {
		return motif_refus;
	}

	public void setMotif_refus(String motif_refus) {
		this.motif_refus = motif_refus;
	}

	public int getNb_personne() {
		return nb_personne;
	}

	public void setNb_personne(int nb_personne) {
		this.nb_personne = nb_personne;
	}

	public DemandeType getType() {
		return type;
	}

	public void setType(DemandeType type) {
		this.type = type;
	}

}
