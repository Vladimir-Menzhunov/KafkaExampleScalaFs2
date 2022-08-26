package company.linkedin

import cats.effect.{IO, IOApp}
import company.CompanyConsumerWorker
import domain.{CompanyConsumerWorkerConfig, Resume}

object LinkedInConsumer extends IOApp.Simple {
  def checkWorkExperienceYears(resume: Resume): Boolean =
    resume.workExperienceYears > 5

  override def run: IO[Unit] = {
    val consumerWorker = CompanyConsumerWorker.mk(
      CompanyConsumerWorkerConfig(
        groupId = "linkedin.company",
        topic = "topic.resume",
        company = "linkedin"
      )
    )
    consumerWorker.consume(checkWorkExperienceYears)
  }
}
