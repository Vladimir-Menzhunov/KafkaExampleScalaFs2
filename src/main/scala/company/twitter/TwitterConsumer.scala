package company.twitter

import cats.effect.{IO, IOApp}
import company.CompanyConsumerWorker
import domain.{CompanyConsumerWorkerConfig, Resume}

object TwitterConsumer extends IOApp.Simple {
  def checkWorkExperienceYears(resume: Resume): Boolean =
    resume.workExperienceYears > 4

  override def run: IO[Unit] = {
    val consumerWorker = CompanyConsumerWorker.mk(
      CompanyConsumerWorkerConfig(
        groupId = "twitter.company",
        topic = "topic.resume",
        company = "twitter"
      )
    )
    consumerWorker.consume(checkWorkExperienceYears)
  }
}
