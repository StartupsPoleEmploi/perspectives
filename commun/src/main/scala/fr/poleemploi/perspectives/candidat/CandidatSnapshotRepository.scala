package fr.poleemploi.perspectives.candidat

import com.fasterxml.jackson.databind.ObjectMapper
import fr.poleemploi.eventsourcing.SnapshotRepository
import fr.poleemploi.eventsourcing.snapshotstore.SnapshotStore

class CandidatSnapshotRepository(override val snapshotStore: SnapshotStore,
                                 objectMapper: ObjectMapper) extends SnapshotRepository[Candidat] {

  override def aggregateType: String = "Candidat"

  override def deserialize(id: CandidatId, version: Int, state: Array[Byte]): Candidat =
    Candidat(
      id = id,
      version = version,
      state = objectMapper.readValue(state, classOf[CandidatContext])
    )

  override def serialize(candidat: Candidat): Array[Byte] =
    objectMapper.writeValueAsBytes(candidat.state)

  override def fromStreamName(streamName: String): CandidatId =
    CandidatId(streamName)
}
