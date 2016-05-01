package record.generator

import java.util.Date

import record.generator.TqlTest.{CleanModel, MongoTemplateData}

import scala.meta.Term

trait MongoRecordMetaGen {

  case class MRec[ClassName, MongoPrimaryKeyType](
    optionalFields: ClassName => List[Any] = (a:ClassName) => Nil,
    lengthFields: ClassName => List[(String,Int)] = (a:ClassName) => Nil
  )

  case class BRec[ClassName]()

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

  case class Person(
    name:String
  )

}

object MongoRecordMeta extends MongoRecordMetaGen {
  import DomainModel._

  MRec[Applicant,UUIDPk](
    optionalFields =
      (a:Applicant) => List(
        a.firstName,
        a.lastName,
        a.born,
        a.fnr
      ),
    lengthFields =
      (a:Applicant) => List(
        (a.firstName, 200),
        (a.lastName, 300)
      )
  )

  BRec[Fnr]
  MRec[Person, ObjectIdPk]()

}

object MongoMetaHelpers {

  val optionalField = Term.Name("optionalFields")
  val lengthField = Term.Name("lengthFields")
  case class FieldLength(name:String, length:Int)

  sealed trait SpecialField
  case class OptionLengthField(name:String, length:Int) extends SpecialField
  case class OptionField(name:String) extends SpecialField
  case class LengthField(length:Int) extends SpecialField

}

object MongoRecordTemplate {
  import MongoMetaHelpers._

  import scala.meta.Term

  private def createParam(param:Term.Param, specialFields:Map[String,SpecialField]):String = {

    specialFields.get(param.name.toString()) match {
      case Some(specialField) =>
        specialField match {
          case OptionField(name) =>
            s"""
               |  object ${param.name} extends ${param.decltpe.get}(this, ${param.decltpe.get}) {
               |    override def optional_? = true
               |  }""".stripMargin
          case LengthField(length) =>
            s"""object ${param.name} extends ${param.decltpe.get}(this, $length)"""

          case OptionLengthField(name, length) =>
            s"""
               |  object ${param.name} extends ${param.decltpe.get}(this, $length) {
               |    override def optional_? = true
               |  }""".stripMargin
        }
      case None =>
        s"""object ${param.name} extends ${param.decltpe.get}(this, ${param.decltpe.get})"""
    }

  }

  def createSource(metaData:MongoTemplateData, cleanModel:CleanModel):String  =
    s"""
       |class ${cleanModel.name.value} private() extends MongoRecord[${cleanModel.name.value}] with ${metaData.pkType}[${cleanModel.name.value}] {
       | def meta = ${cleanModel.name.value}
       | ${cleanModel.params.map(p => createParam(p, metaData.specialFields)).mkString("\n ")}
       |}
       |object ${cleanModel.name.value} extends ${cleanModel.name.value} with MongoMetaRecord[${cleanModel.name.value}]
     """.stripMargin

}