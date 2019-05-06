package fr.poleemploi.perspectives.recruteur

import com.fasterxml.jackson.databind.ObjectMapper
import fr.poleemploi.eventsourcing.SnapshotRepository
import fr.poleemploi.eventsourcing.snapshotstore.SnapshotStore

class RecruteurSnapshotRepository(override val snapshotStore: SnapshotStore,
                                  objectMapper: ObjectMapper) extends SnapshotRepository[Recruteur] {

  override def aggregateType: String = "Recruteur"

  override def deserialize(id: RecruteurId, version: Int, state: Array[Byte]): Recruteur =
    Recruteur(
      id = id,
      version = version,
      state = objectMapper.readValue(state, classOf[RecruteurContext])
    )

  override def serialize(recruteur: Recruteur): Array[Byte] =
    objectMapper.writeValueAsBytes(recruteur.state)

  override def fromStreamName(streamName: String): RecruteurId =
    RecruteurId(streamName)
}
