package authentification.model

import fr.poleemploi.perspectives.domain.recruteur.RecruteurId

case class RecruteurAuthentifie(recruteurId: RecruteurId,
                                nom: String,
                                prenom: String)
