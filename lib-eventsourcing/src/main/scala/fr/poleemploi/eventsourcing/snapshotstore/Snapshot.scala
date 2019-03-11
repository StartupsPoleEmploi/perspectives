package fr.poleemploi.eventsourcing.snapshotstore

case class Snapshot(streamName: String,
                    streamVersion: Int,
                    streamType: String,
                    serializedState: Array[Byte])
