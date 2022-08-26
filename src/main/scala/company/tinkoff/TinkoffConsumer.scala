package company.tinkoff

import cats.effect.{IO, IOApp}
import company.CompanyConsumerWorker
import domain.{CompanyConsumerWorkerConfig, Resume}

object TinkoffConsumer extends IOApp.Simple {
  def checkWorkExperienceYears(resume: Resume): Boolean =
    resume.workExperienceYears >= 3

  override def run: IO[Unit] = {
    val consumerWorker = CompanyConsumerWorker.mk(
      CompanyConsumerWorkerConfig(
        groupId = "tinkoff.company",
        topic = "topic.resume",
        company = "Tinkoff"
      )
    )
    consumerWorker.consume(checkWorkExperienceYears)
  }
}
