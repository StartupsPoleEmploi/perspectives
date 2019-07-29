package conf

import java.nio.file.Paths

import fr.poleemploi.perspectives.commun.domain.Email
import fr.poleemploi.perspectives.commun.infra.file.ImportFileAdapterConfig
import fr.poleemploi.perspectives.emailing.infra.ws.MailjetWSAdapterConfig
import fr.poleemploi.perspectives.infra.BuildInfo
import play.api.Configuration

class BatchsConfig(configuration: Configuration) {

  val useMailjet: Boolean = configuration.getOptional[Boolean]("useMailjet").getOrElse(true)
  val usePEConnect: Boolean = configuration.getOptional[Boolean]("usePEConnect").getOrElse(true)
  val useImportHabiletesMRS: Boolean = configuration.getOptional[Boolean]("useImportHabiletesMRS").getOrElse(true)

  val version: String = BuildInfo.version

  val mailjetWSAdapterConfig: MailjetWSAdapterConfig = MailjetWSAdapterConfig(
    urlApi = configuration.get[String]("mailjet.urlApi"),
    senderAdress = Email(configuration.get[String]("mailjet.sender")),
    apiKeyPublic = configuration.get[String]("mailjet.apiKey.public"),
    apiKeyPrivate = configuration.get[String]("mailjet.apiKey.private")
  )
  val mailjetTesteurs: List[Email] =
    configuration.getOptional[Seq[String]]("mailjet.testeurs").map(_.map(Email(_)).toList).getOrElse(Nil)

  val importPoleEmploiFileConfig: ImportFileAdapterConfig = ImportFileAdapterConfig(
    importDirectory = Paths.get(configuration.get[String]("exportPoleEmploi.directory")),
    archiveDirectory = Paths.get(configuration.get[String]("exportPoleEmploi.archiveDirectory"))
  )
}

