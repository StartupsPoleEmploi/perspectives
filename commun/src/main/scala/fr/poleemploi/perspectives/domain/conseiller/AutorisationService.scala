package fr.poleemploi.perspectives.domain.conseiller

trait AutorisationService {

  def hasRole(conseillerId: String, role: RoleConseiller): Boolean

}

class AutorisationServiceDefaut(admins: List[String]) extends AutorisationService {

  override def hasRole(conseillerId: String,
                       role: RoleConseiller): Boolean = role match {
    case RoleConseiller.ADMIN => admins contains conseillerId
    case _ => false
  }
}