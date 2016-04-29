package record.generator

import scala.meta._
import scala.meta.dialects.Scala211
import java.io.FileInputStream

object TqlTest {

  val path = new java.io.File(".")
  val sourcePath = (path.getAbsolutePath +
    "src/main/scala-2.11/record/generator/DomainModel.scala").
    replace(".src", "src")

  val stream = new FileInputStream(sourcePath)
  val tree: Parsed[Source] = stream.parse[Source]

  case class CleanModel(
    name: Type.Name,
    params: Seq[Seq[Term.Param]]
  )

  case class MongoMapper(
    typeParameters: Seq[Type]
  )

  case class MongoModel(
    primaryId:Type,
    name: Type.Name,
    params:Seq[Seq[Term.Param]]
  )

  def disassembleDomainModel(
    tree: scala.meta.Template
  ): Seq[Any] = tree.children.collect {
    case q"..$mods class $tname (...$paramss)" =>
      CleanModel(tname, paramss)
  }

  def disassembleMongoMapping(
    tree: scala.meta.Template
  ):Seq[Any] = tree.children.collect {
    case q"$tpe [..$tpes] " =>
      MongoMapper(tpes)
  }

  def parseTree(
    tree: Parsed[Source],
    targetModel:String,
    disassembler: Template => Seq[Any]
  ): List[Any] = tree.get.collect {
    case q"..$mods object $name extends $template" =>
      name match {
        case Term.Name(objectName) =>
          if(objectName == targetModel)
            disassembler(template)
      }
  }

  def test(): Unit = {

    val cleanModel = parseTree(
      tree,
      "DomainModel",
      disassembleDomainModel
    )(0).asInstanceOf[List[CleanModel]]

    val mongoMetaModel = parseTree(
      tree,
      "MongoRecordMeta",
      disassembleMongoMapping
    )(1).asInstanceOf[List[MongoMapper]]

    val records = for {
      model <- cleanModel
      mapper <- mongoMetaModel
    } yield {
      s"${model.name} ${mapper.typeParameters(1) } "
    }

    println(records)

  }

}
