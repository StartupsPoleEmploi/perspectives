package fr.poleemploi.perspectives.candidat

import com.fasterxml.jackson.databind.ObjectMapper
import fr.poleemploi.eventsourcing.SnapshotRepository
import fr.poleemploi.eventsourcing.snapshotstore.SnapshotStore

class CandidatSnapshotRepository(override val snapshotStore: SnapshotStore,
                                 objectMapper: ObjectMapper) extends SnapshotRepository[Candidat] {

  override def aggregateType: String = "Candidat"

  override def gapBetweenSnapshots: Int = 20

  override def deserialize(bytes: Array[Byte]): Candidat =
    objectMapper.readValue(bytes, classOf[Candidat])

  override def serialize(aggregate: Candidat): Array[Byte] =
    objectMapper.writeValueAsBytes(aggregate)

  override def fromStreamName(streamName: String): CandidatId =
    CandidatId(streamName)
}
