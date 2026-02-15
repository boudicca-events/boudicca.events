package base.boudicca.api.eventcollector

import base.boudicca.api.eventcollector.config.EventCollectorBaseConfig

// another layer of abstraction to set the EventCollectorBaseConfig as default config type
abstract class SimpleTwoStepEventCollector<T> : TwoStepEventCollector<T, EventCollectorBaseConfig>(EventCollectorBaseConfig::class)
