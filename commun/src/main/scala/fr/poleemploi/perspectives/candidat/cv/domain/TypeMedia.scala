package fr.poleemploi.perspectives.candidat.cv.domain

import fr.poleemploi.eventsourcing.StringValueObject

case class TypeMedia(value: String) extends StringValueObject

object TypeMedia {

  val PDF = TypeMedia("application/pdf")
  val DOC = TypeMedia("application/msword")
  val DOCX = TypeMedia("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
  val ODT = TypeMedia("application/vnd.oasis.opendocument.text")
  val JPEG = TypeMedia("image/jpeg")

  private val typesMediasCVParValeur: Map[String, TypeMedia] = Map(
    PDF.value -> PDF,
    DOC.value -> DOC,
    DOCX.value -> DOCX,
    ODT.value -> ODT,
    JPEG.value -> JPEG
  )

  val typesMediasCV: List[String] = typesMediasCVParValeur.keys.toList

  val extensionsCV: List[String] = typesMediasCVParValeur.values.map(getExtensionFichier).toList

  def typeMediaCV(value: String): Option[TypeMedia] = typesMediasCVParValeur.get(value)

  def getExtensionFichier(typeMedia: TypeMedia): String = typeMedia match {
    case PDF => "pdf"
    case DOC => "doc"
    case DOCX => "docx"
    case ODT => "odt"
    case JPEG => "jpeg"
    case _ => ""
  }
}
