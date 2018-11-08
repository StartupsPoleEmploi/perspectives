package fr.poleemploi.perspectives.candidat.mrs

import org.slf4j.{Logger, LoggerFactory}

package object infra {

  lazy val importMrsCandidatLogger: Logger = LoggerFactory.getLogger("importMrsCandidat")
  lazy val importHabiletesMRSLogger: Logger = LoggerFactory.getLogger("importHabiletesMrs")
}
