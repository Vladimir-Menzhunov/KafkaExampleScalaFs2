package mail.kafka.courier

import cats.effect.{ConcurrentEffect, ContextShift, IO}
import domain.{KafkaLogger, ProducerConfig}
import fs2.kafka
import fs2.kafka._
import tethys.{JsonWriter, JsonWriterOps}
import tethys.jackson.jacksonTokenWriterProducer

class Producer[T](
    producerSettings: ProducerSettings[IO, String, String],
    config: ProducerConfig[T]
)(implicit
    concurrent: ConcurrentEffect[IO],
    contextShift: ContextShift[IO],
    jsonWriter: JsonWriter[T]
) extends KafkaLogger {

  def send: IO[Unit] = {
    kafka
      .producerStream(producerSettings)
      .evalMap { producer =>
        val producerRecord: ProducerRecord[String, String] =
          ProducerRecord(config.topic, config.id, config.message.asJson)
        val producerRecords = ProducerRecords.one(producerRecord)
        for {
          sentMessage <- producer.produce(producerRecords)
          gotMessage <- sentMessage
        } yield logger.info(
          s"Посылка: $gotMessage, была отправленна и получена всеми брокерами"
        )
      }
      .compile
      .drain
  }
}

object Producer {
  def mk[T](
      config: ProducerConfig[T]
  )(implicit
      concurrent: ConcurrentEffect[IO],
      contextShift: ContextShift[IO],
      jsonWriter: JsonWriter[T]
  ): Producer[T] = {
    val producerSettings: ProducerSettings[IO, String, String] =
      ProducerSettings[IO, String, String]
        .withBootstrapServers("localhost:9092,localhost:9093")
        .withAcks(Acks.All)
    new Producer(producerSettings, config)
  }
}
