package company

import cats.effect.{ContextShift, IO, Sync, Timer}
import domain._
import fs2.kafka
import fs2.kafka.{AutoOffsetReset, ConsumerSettings}
import mail.kafka.courier.Producer
import tethys._
import tethys.jackson.jacksonTokenIteratorProducer

class CompanyConsumerWorker(
    consumerSettings: ConsumerSettings[IO, Unit, String],
    config: CompanyConsumerWorkerConfig
)(implicit contextShift: ContextShift[IO], timer: Timer[IO])
    extends KafkaLogger {
  def consume(fun: Resume => Boolean): IO[Unit] =
    kafka
      .consumerStream(consumerSettings)
      .evalTap(_.subscribeTo(config.topic))
      .flatMap(_.stream)
      .evalMap(message =>
        message.record.value.jsonAs[Resume] match {
          case Left(_) =>
            logger.error("Не смог прочитать сообщение :(")
            Sync[IO].unit

          case Right(resume) =>
            val isPositive = fun(resume)

            val message = Answer(
              isPositive = isPositive,
              message =
                if (isPositive)
                  "Мы готовы позвать вас на собеседование"
                else
                  "К сожалению, мы не можем вас пригласить на собеседование",
              company = config.company
            )
            val producer = Producer.mk[Answer](
              ProducerConfig(
                resume.keyPartition,
                topic = "topic.answer",
                message
              )
            )
            for {
              _ <- producer.send
            } yield ()
        }
      )
      .compile
      .drain
}

object CompanyConsumerWorker {
  def mk(config: CompanyConsumerWorkerConfig)(implicit
      contextShift: ContextShift[IO],
      timer: Timer[IO]
  ): CompanyConsumerWorker = {
    val consumerSettings: ConsumerSettings[IO, Unit, String] =
      ConsumerSettings[IO, Unit, String]
        .withAutoOffsetReset(AutoOffsetReset.Earliest)
        .withEnableAutoCommit(true)
        .withBootstrapServers("localhost:9092, localhost:9093")
        .withGroupId(config.groupId)

    new CompanyConsumerWorker(consumerSettings, config)

  }
}
