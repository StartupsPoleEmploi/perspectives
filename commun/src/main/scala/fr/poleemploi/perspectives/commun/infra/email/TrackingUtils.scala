package fr.poleemploi.perspectives.commun.infra.email

object TrackingUtils {

  def buildTrackingGA(utmCampaign: String, utmSource: String, utmMedium: String, utmContent: String): String =
    s"utm_campaign=$utmCampaign&utm_source=$utmSource&utm_medium=$utmMedium&utm_content=$utmContent"

}
