package programer.pasha.manager

import cats.effect.concurrent.{MVar2, Ref}
import cats.effect.{ConcurrentEffect, ContextShift, IO, IOApp, Sync, Timer}
import domain.{Answer, ConsumerConfig, KafkaLogger}
import fs2.kafka.{AutoOffsetReset, ConsumerSettings, consumerStream}
import programer.pasha.PashaAction.table
import tethys._
import tethys.jackson.jacksonTokenIteratorProducer

class ConsumerManager(
    consumerSettings: ConsumerSettings[IO, String, String],
    consumerConfig: ConsumerConfig
)(implicit contextShift: ContextShift[IO], timer: Timer[IO])
    extends KafkaLogger {
  def consume(
      counter: Ref[IO, Int],
      initializationMarker: MVar2[IO, Unit]
  ): IO[Unit] = {
    consumerStream(consumerSettings)
      .evalTap(_.subscribeTo(consumerConfig.topic))
      .flatMap(_.stream)
      .evalMap(message =>
        (message.record.key, message.record.value.jsonAs[Answer]) match {
          case (_, Left(_)) =>
            logger.error("Не смог разобрать ответ (")
            Sync[IO].unit
          case (key, Right(answer)) if key == consumerConfig.key =>
            logger.info(
              s"Было получено сообщение от компании: ${answer.company}"
            )
            table += answer
            for {
              _ <- counter.modify(c => (c + 1, c + 1))
              count <- counter.get
              _ <-
                if (count == 4) {
                  message.offset.commit.flatMap(_ =>
                    initializationMarker.put(()).as(())
                  )
                } else
                  Sync[IO].unit
            } yield ()

          case _ =>
            Sync[IO].unit
        }
      )
      .compile
      .drain
  }
}

object ConsumerManager {
  def mk(consumerConfig: ConsumerConfig)(implicit
      timer: Timer[IO],
      contextShift: ContextShift[IO]
  ): ConsumerManager = {
    val consumerSettings: ConsumerSettings[IO, String, String] =
      ConsumerSettings[IO, String, String]
        .withAutoOffsetReset(AutoOffsetReset.Earliest)
        .withEnableAutoCommit(true)
        .withBootstrapServers("localhost:9092, localhost:9093")
        .withGroupId("ConsumerManager")

    new ConsumerManager(consumerSettings, consumerConfig)
  }
}
