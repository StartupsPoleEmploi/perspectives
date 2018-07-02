package authentification

trait RoleService {
  def hasRole(authenticatedCandidat: AuthenticatedCandidat, role: Role): Boolean
}

class SimpleRoleService(admins: List[String]) extends RoleService {

  override def hasRole(authenticatedCandidat: AuthenticatedCandidat,
                       role: Role): Boolean = role match {
    case Role.ADMIN => admins contains authenticatedCandidat.candidatId
    case _ => false
  }
}
