package domain

import org.slf4j
import org.slf4j.LoggerFactory

trait KafkaLogger {
  val logger: slf4j.Logger = LoggerFactory.getLogger(this.getClass)
}
