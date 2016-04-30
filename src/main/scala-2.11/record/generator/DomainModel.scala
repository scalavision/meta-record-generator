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

  case class Applicant(
    fnr:Fnr,
    firstName:String,
    middleNames: List[String],
    lastName:String,
    born:Date)

  case class Fnr(fnr:String)

}

object MongoRecordMeta extends MongoRecordMetaGen {
  import DomainModel._

  MRec[Applicant,UUIDPk]

}
