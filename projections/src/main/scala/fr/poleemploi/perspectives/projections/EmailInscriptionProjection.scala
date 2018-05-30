package fr.poleemploi.perspectives.projections

import fr.poleemploi.eventsourcing.{AppendedEvent, EventHandler}

class EmailInscriptionProjection extends EventHandler {

  override def handle(appendedEvent: AppendedEvent): Unit = {
    print("SEND EMAIL " + appendedEvent.eventType)
  }
}
