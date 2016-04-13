package record.generator

import scala.meta.Term.Param
import scala.meta.Type.Arg
import scala.meta.{Term, Type}

/**
  * Created by tomsorlie on 4/9/16.
  */
object SourceGenerator {

  sealed trait MetaParam {
    def createParam: String
  }

  case class BaseParam(name: Param.Name, tpe: Arg) extends MetaParam {
    private val fieldExtractor = s"$tpe(this)"
    def createParam: String =
      s"object $name extends $fieldExtractor"
  }

  case class StringFieldParam(name: Param.Name, tpe: Arg, length: Term) extends MetaParam {
    private val fieldExtractor = s"$tpe(this, ${length})"
    def createParam: String =
      s"object $name extends $fieldExtractor"
  }

  case class RecordInstance(name: Type.Name, params: Seq[MetaParam]) {
    def createRecord: String =
      s"""
         |object $name extends $name with MongoRecord[$name]{
         |  def meta = $name
         |  ${params.map(_.createParam).mkString("\n  ")}
         |}
      """.stripMargin
  }

}
