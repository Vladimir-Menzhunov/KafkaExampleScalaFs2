package company.google

import cats.effect.{IO, IOApp, Sync}
import company.CompanyConsumerWorker
import domain.{CompanyConsumerWorkerConfig, KafkaLogger, Resume}

object GoogleConsumer extends IOApp.Simple with KafkaLogger {
  def checkWorkExperienceYears(resume: Resume): Boolean =
    resume.workExperienceYears > 2

  override def run: IO[Unit] = {
    val consumerWorker = CompanyConsumerWorker.mk(
      CompanyConsumerWorkerConfig(
        groupId = "google.company",
        topic = "topic.resume",
        company = "google"
      )
    )
    consumerWorker.consume(checkWorkExperienceYears)
  }
}
