package controllers.candidat

import java.nio.file.Path

import fr.poleemploi.perspectives.candidat.cv.domain.TypeMedia
import play.api.libs.Files
import play.api.mvc.MultipartFormData

case class CVForm(nomFichier: String,
                  path: Path,
                  typeMedia: TypeMedia)

object CVForm {

  val maxLength: Long = 5L * 1024 * 1024

  def bindFromMultipart(request: MultipartFormData[Files.TemporaryFile]): Either[String, CVForm] = {
    val fichierCV = request.file("cv")

    fichierCV.map(f => {
      f.contentType.flatMap(TypeMedia.typeMediaCV).map(t =>
        Right(CVForm(
          nomFichier = f.filename,
          typeMedia = t,
          path = f.ref.path
        ))
      ).getOrElse(Left("Le type de fichier n'est pas valide"))
    }).getOrElse(Left("Aucun fichier reçu"))
  }
}
