package record.generator

import java.util.Date

trait MongoRecordMetaGen {
  case class MRec[ClassName, MongoPrimaryKeyType]()
  type StringPk
  type ObjectIdPk
  type UUIDPk
  type IntPk
  type LongPk
}

object DomainModel {

  case class Student(
    firstName:String,
    middleNames: List[String],
    lastName:String,
    born:Date)

}

object MongoRecordMeta extends MongoRecordMetaGen {
  import DomainModel._

  MRec[Student,StringPk]

}
