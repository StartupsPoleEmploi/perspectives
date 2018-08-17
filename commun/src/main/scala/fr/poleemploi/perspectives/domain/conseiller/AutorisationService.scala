package fr.poleemploi.perspectives.domain.conseiller

trait AutorisationService {

  def hasRole(conseillerId: ConseillerId, role: RoleConseiller): Boolean

}

class AutorisationServiceDefaut(admins: List[ConseillerId]) extends AutorisationService {

  override def hasRole(conseillerId: ConseillerId,
                       role: RoleConseiller): Boolean = role match {
    case RoleConseiller.ADMIN => admins contains conseillerId
    case _ => false
  }
}