package fr.poleemploi.perspectives.recruteur

import fr.poleemploi.perspectives.recruteur.commentaire.domain.{CommentaireListeCandidats, CommentaireService, ContexteRecherche}
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{AsyncWordSpec, BeforeAndAfter, MustMatchers, Succeeded}

import scala.concurrent.Future

class CommenterListeCandidatsSpec extends AsyncWordSpec
  with MustMatchers with MockitoSugar with BeforeAndAfter {

  val recruteurBuilder = new RecruteurBuilder

  var commentaireService: CommentaireService = _
  var contexteRecherche: ContexteRecherche = _
  var commande: CommenterListeCandidatsCommand = _

  before {
    commentaireService = mock[CommentaireService]

    contexteRecherche = mock[ContexteRecherche]
    commande = CommenterListeCandidatsCommand(
      id = recruteurBuilder.recruteurId,
      contexteRecherche = contexteRecherche,
      commentaire = "top cool"
    )
  }

  "commenterListeCandidats" should {
    "renvoyer une erreur lorsque le recruteur n'est pas inscrit" in {
      // Given
      val recruteur = recruteurBuilder.build

      // When & Then
      recoverToExceptionIf[IllegalArgumentException] {
        recruteur.commenterListeCandidats(commande, commentaireService)
      }.map(ex =>
        ex.getMessage mustBe s"Le recruteur ${commande.id.value} dans l'état Nouveau ne peut pas gérer la commande ${commande.getClass.getSimpleName}"
      )
    }
    "renvoyer une erreur lorsque le recruteur n'a pas le profil complet" in {
      // Given
      val recruteur = recruteurBuilder.avecInscription().build

      // When & Then
      recoverToExceptionIf[IllegalArgumentException] {
        recruteur.commenterListeCandidats(commande, commentaireService)
      }.map(ex =>
        ex.getMessage mustBe s"Le recruteur ${commande.id.value} dans l'état Inscrit ne peut pas gérer la commande ${commande.getClass.getSimpleName}"
      )
    }
    "renvoyer une erreur lorsque le service échoue" in {
      val recruteur = recruteurBuilder
        .avecInscription()
        .avecProfil()
        .build
      when(commentaireService.commenterListeCandidats(ArgumentMatchers.any[CommentaireListeCandidats]())) thenReturn Future.failed(new RuntimeException("erreur de service"))

      // When & Then
      recoverToExceptionIf[RuntimeException] {
        recruteur.commenterListeCandidats(commande, commentaireService)
      }.map(ex =>
        ex.getMessage mustBe "erreur de service"
      )
    }
    "ne pas générer d'événement lorsque l'appel au service réussit" in {
      val recruteur = recruteurBuilder
        .avecInscription()
        .avecProfil()
        .build
      when(commentaireService.commenterListeCandidats(ArgumentMatchers.any[CommentaireListeCandidats]())) thenReturn Future.successful(())

      // When & Then
      recruteur.commenterListeCandidats(commande, commentaireService)
        .map( _ => Succeeded)
    }
  }

}
