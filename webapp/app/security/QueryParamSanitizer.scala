package security

import play.twirl.api.HtmlFormat

object QueryParamSanitizer {

  def sanitize(queryParam: String): String =
    HtmlFormat.escape(queryParam).toString()

  def sanitize(queryParam: Option[String]): Option[String] =
    queryParam.map(sanitize)

}
