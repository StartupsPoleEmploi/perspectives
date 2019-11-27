package fr.poleemploi.perspectives.commun

import java.text.Normalizer

object StringUtils {

  def unaccent(src: String): String =
    Normalizer.normalize(src, Normalizer.Form.NFD)
      .replaceAll("[^\\p{ASCII}]", "")

}
