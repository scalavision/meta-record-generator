package record.generator

import java.io.FileInputStream
import scala.meta._
import scala.meta.dialects.Scala211
import MongoMetaHelpers._

object TqlTest {

  val path = new java.io.File(".")
  val sourcePath = (path.getAbsolutePath +
    "src/main/scala-2.11/record/generator/DomainModel.scala").
    replace(".src", "src")

  val stream = new FileInputStream(sourcePath)
  val tree: Parsed[Source] = stream.parse[Source]

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

    val mongoMetaData:Map[String, MongoTemplateData] = mongoMetaModel.map{
      m =>
        val templateData = m.toMongoTemplateData
        templateData.pkType -> templateData
    }.toMap

    val recordSources = for {
      cm <- cleanModel
      metaData <- mongoMetaData.get(cm.name.toString)
    } yield {
      MongoRecordTemplate.createSource(metaData, cm)
    }

    println(recordSources.mkString(""))

  }

  def disassembleDomainModel(
    tree: scala.meta.Template
  ): Seq[Any] = tree.children.collect {
    case q"..$mods class $tname (...$paramss)" =>
      CleanModel(tname, paramss(0))
  }

  def disassembleOptionalFields(tree: Seq[Term.Arg]) : Seq[String] =
    tree.flatMap {
      case Term.Arg.Named(Term.Name("optionalFields"), Term.Function(param)) =>
        param._2.collect {
          case Term.Select(Term.Name(ref), Term.Name(fieldName)) =>
            fieldName
        }
      case _ => Seq.empty
    }


  def disassembleLengthFields(tree: Seq[Term.Arg]) : Seq[FieldLength] = {

    val result:List[Seq[Any]] = tree.flatMap {
      case Term.Arg.Named(Term.Name("lengthFields"), Term.Function(param)) =>
        param._2.collect {
          case Term.Select(Term.Name(ref), Term.Name(fieldName)) =>
            fieldName
          case Lit(length) =>
            length
        }
      case _ => Seq()
    }.sliding(2,2).toList

    result.map {
      s =>
        FieldLength(
          s(0).asInstanceOf[String],
          s(1).asInstanceOf[Int]
        )
    }

  }

  def disassembleMongoMapping(
    tree: scala.meta.Template
  ): Seq[MongoMapper] = tree.children.collect {

    case q"$tpe [..$tpes] (..$aexpr)" =>

      val optionalFields:Seq[String] =
        disassembleOptionalFields(
          aexpr
        )

      val lengthFields:Seq[FieldLength] =
        disassembleLengthFields(
          aexpr
        )

      MongoMapper(tpes, optionalFields, lengthFields)
  }

  def parseTree(
    tree: Parsed[Source],
    targetModel: String,
    disassembler: Template => Seq[Any]
  ): List[Any] = tree.get.collect {
    case q"..$mods object $name extends $template" =>
      name match {
        case Term.Name(objectName) =>
          if (objectName == targetModel)
            disassembler(template)
      }
  }

  case class CleanModel(
    name: Type.Name,
    params: Seq[Term.Param]
  )

  case class MongoMapper(
    typeParameters: Seq[Type],
    optionalParameters: Seq[String],
    lengthFields: Seq[FieldLength]
  ) {
    def toMongoTemplateData:MongoTemplateData = {

      val optionalFieldsMap:Map[String, OptionField] = optionalParameters.map {
        s =>
          s -> OptionField(s)
      }.toMap

      val lengthFieldsMap:Map[String, LengthField] = lengthFields.map {
        fl =>
          fl.name -> LengthField(fl.length)
      }.toMap

      val cleanOptionals: Map[String, OptionField] =
        for {
          (k,v) <- optionalFieldsMap
          if !lengthFieldsMap.contains(k)
        } yield {
          k -> v
        }

      val cleanLengths: Map[String, LengthField] =
        for {
          (k,v) <- lengthFieldsMap
          if !optionalFieldsMap.contains(k)
        } yield {
          k -> v
        }

      val cleanCombos: Map[String, OptionLengthField] =
        for {
          (k,v) <- lengthFieldsMap
          if optionalFieldsMap.contains(k)
        } yield {
          k -> OptionLengthField(
            name = optionalFieldsMap.get(k).get.name,
            length = v.length
          )
        }

      val result : Map[String, SpecialField] =
        cleanOptionals ++ cleanLengths ++ cleanCombos

      MongoTemplateData(
        typeParameters(0).toString,
        result
      )

    }
  }

  case class MongoTemplateData(
    pkType: String,
    specialFields: Map[String, SpecialField]
  )

}
