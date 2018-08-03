package fr.poleemploi

import org.slf4j.{Logger, LoggerFactory}

package object eventsourcing {

  lazy val eventSourcingLogger: Logger = LoggerFactory.getLogger("eventsourcing")

  trait StringValue {
    def value: String
  }

  trait IntValue {
    def value: Int
  }
}
