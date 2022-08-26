package programer.pasha

import cats.effect.concurrent.{MVar, Ref}
import cats.effect.{Concurrent, ExitCode, IO, IOApp, Resource, Sync}
import domain._
import mail.kafka.courier.Producer
import programer.pasha.manager.ConsumerManager

import scala.collection.mutable

object PashaAction extends IOApp.Simple with KafkaLogger {
  val table: mutable.Set[Answer] = mutable.Set.empty
  val keyPartition = "programmer.pasha"

  override def run: IO[Unit] = {
    val resume =
      Resume(keyPartition, "Паша, просто гениален!", workExperienceYears = 3)
    val producer = Producer.mk(
      ProducerConfig[Resume](
        keyPartition,
        "topic.resume",
        resume
      )
    )

    val consumerManager = ConsumerManager.mk(
      ConsumerConfig(
        "topic.answer",
        keyPartition
      )
    )

    for {
      _ <- producer.send
      initializationMarker <- MVar.empty[IO, Unit]
      counter <- Ref.of[IO, Int](0)
      _ <- Concurrent[IO]
        .background(
          consumerManager.consume(counter, initializationMarker)
        )
        .allocated
      _ <- Sync[IO].pure(logger.info("[Паша]: Дела сделаны, я пошел отдыхать!"))
      _ <- initializationMarker.take
    } yield {
      val (rightBunchOnTable, leftBunchOnTable) = table.partition(_.isPositive)
      logger.info(s"Отрицательные ответы: $leftBunchOnTable")
      logger.info(s"Положительные ответы: $rightBunchOnTable")
      table.clear()
    }
  }
}
