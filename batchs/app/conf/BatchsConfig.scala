package conf

import java.nio.file.Paths

import fr.poleemploi.perspectives.authentification.infra.autologin.AutologinConfig
import fr.poleemploi.perspectives.commun.domain.Email
import fr.poleemploi.perspectives.commun.infra.elasticsearch.EsConfig
import fr.poleemploi.perspectives.commun.infra.file.ImportFileAdapterConfig
import fr.poleemploi.perspectives.emailing.infra.ws.MailjetWSAdapterConfig
import fr.poleemploi.perspectives.infra.BuildInfo
import play.api.Configuration

class BatchsConfig(configuration: Configuration) {

  val useMailjet: Boolean = configuration.getOptional[Boolean]("useMailjet").getOrElse(true)
  val usePEConnect: Boolean = configuration.getOptional[Boolean]("usePEConnect").getOrElse(true)
  val useImportHabiletesMRS: Boolean = configuration.getOptional[Boolean]("useImportHabiletesMRS").getOrElse(true)

  val baseUrl: String = configuration.get[String]("baseUrl")

  val version: String = BuildInfo.version

  val mailjetWSAdapterConfig: MailjetWSAdapterConfig = MailjetWSAdapterConfig(
    urlApi = configuration.get[String]("mailjet.urlApi"),
    senderAdress = Email(configuration.get[String]("mailjet.sender")),
    apiKeyPublic = configuration.get[String]("mailjet.apiKey.public"),
    apiKeyPrivate = configuration.get[String]("mailjet.apiKey.private")
  )

  val importPoleEmploiFileConfig: ImportFileAdapterConfig = ImportFileAdapterConfig(
    importDirectory = Paths.get(configuration.get[String]("exportPoleEmploi.directory")),
    archiveDirectory = Paths.get(configuration.get[String]("exportPoleEmploi.archiveDirectory"))
  )

  val esConfig: EsConfig = EsConfig(
    host = configuration.get[String]("elasticsearch.host"),
    port = configuration.get[Int]("elasticsearch.port")
  )

  val autologinConfig: AutologinConfig = AutologinConfig(
    secretKey = configuration.get[String]("autologin.secretKey"),
    issuer = configuration.get[String]("autologin.issuer"),
    expirationInSeconds = configuration.get[Long]("autologin.expirationInSeconds")
  )
}

