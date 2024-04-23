package events.boudicca.eventcollector.collectors

import base.boudicca.api.eventcollector.TwoStepEventCollector

class FemaleCoderCollector : TwoStepEventCollector<String>("femalecoder") {
  override fun getAllUnparsedEvents(): List<String>? {
    TODO("Not yet implemented")
  }
}