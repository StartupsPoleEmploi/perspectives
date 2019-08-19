package fr.poleemploi.perspectives.commun.geo.infra.local

import fr.poleemploi.perspectives.commun.domain._
import fr.poleemploi.perspectives.commun.geo.domain.ReferentielRegion

import scala.concurrent.Future

class ReferentielRegionLocalAdapter extends ReferentielRegion {

  override def regions: Future[List[Region]] = Future.successful(
    List(
      Region(code = CodeRegion("52"), label = "Pays de la Loire"),
      Region(code = CodeRegion("53"), label = "Bretagne"),
      Region(code = CodeRegion("76"), label = "Occitanie")
    )
  )

  override def departements: Future[List[Departement]] = Future.successful(
    List(
      Departement(code = CodeDepartement("72"), label = "Sarthe", codeRegion = CodeRegion("52")),
      Departement(code = CodeDepartement("53"), label = "Mayenne", codeRegion = CodeRegion("52")),
      Departement(code = CodeDepartement("49"), label = "Maine-et-Loire", codeRegion = CodeRegion("52")),
      Departement(code = CodeDepartement("44"), label = "Loire-Atlantique", codeRegion = CodeRegion("52")),
      Departement(code = CodeDepartement("85"), label = "Vendée", codeRegion = CodeRegion("52")),
      Departement(code = CodeDepartement("22"), label = "Côtes-d'Armor", codeRegion = CodeRegion("53")),
      Departement(code = CodeDepartement("29"), label = "Finistère", codeRegion = CodeRegion("53")),
      Departement(code = CodeDepartement("35"), label = "Ille-et-Vilaine", codeRegion = CodeRegion("53")),
      Departement(code = CodeDepartement("56"), label = "Morbihan", codeRegion = CodeRegion("53")),
      Departement(code = CodeDepartement("65"), label = "Hautes-Pyrénées", codeRegion = CodeRegion("76")),
      Departement(code = CodeDepartement("66"), label = "Pyrénées-Orientales", codeRegion = CodeRegion("76")),
      Departement(code = CodeDepartement("81"), label = "Tarn", codeRegion = CodeRegion("76")),
      Departement(code = CodeDepartement("82"), label = "Tarn-et-Garonne", codeRegion = CodeRegion("76"))
    )
  )
}