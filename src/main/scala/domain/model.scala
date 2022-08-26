package domain

import tethys.derivation.semiauto.{jsonReader, jsonWriter}
import tethys.{JsonReader, JsonWriter}

case class ProducerConfig[T](id: String, topic: String, message: T)(implicit
    messageWriter: JsonWriter[T]
)

case class Resume(
    keyPartition: String,
    otherInfo: String,
    workExperienceYears: Int
)
object Resume {
  implicit val resumeWriter: JsonWriter[Resume] = jsonWriter[Resume]
  implicit val resumeReader: JsonReader[Resume] = jsonReader[Resume]
}

case class ConsumerConfig(topic: String, key: String)

case class Answer(isPositive: Boolean, message: String, company: String)
object Answer {
  implicit val answerWriter: JsonWriter[Answer] = jsonWriter[Answer]
  implicit val answerReader: JsonReader[Answer] = jsonReader[Answer]
}

case class CompanyConsumerWorkerConfig(
    groupId: String,
    topic: String,
    company: String
)
