package fr.poleemploi.perspectives.domain.conseiller

class AutorisationService(admins: List[ConseillerId]) {

  def hasRole(conseillerId: ConseillerId,
              role: RoleConseiller): Boolean = role match {
    case RoleConseiller.ADMIN => admins contains conseillerId
    case _ => false
  }

}