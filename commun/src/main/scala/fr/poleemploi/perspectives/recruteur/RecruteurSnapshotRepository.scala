package fr.poleemploi.perspectives.recruteur

import com.fasterxml.jackson.databind.ObjectMapper
import fr.poleemploi.eventsourcing.SnapshotRepository
import fr.poleemploi.eventsourcing.snapshotstore.SnapshotStore

class RecruteurSnapshotRepository(override val snapshotStore: SnapshotStore,
                                  objectMapper: ObjectMapper) extends SnapshotRepository[Recruteur] {

  override def aggregateType: String = "Recruteur"

  override def gapBetweenSnapshots: Int = 20

  override def deserialize(bytes: Array[Byte]): Recruteur =
    objectMapper.readValue(bytes, classOf[Recruteur])

  override def serialize(aggregate: Recruteur): Array[Byte] =
    objectMapper.writeValueAsBytes(aggregate)

  override def fromStreamName(streamName: String): RecruteurId =
    RecruteurId(streamName)
}
