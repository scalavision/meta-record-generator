package record.generator

import scala.meta._
import scala.meta.Term
import record.generator.SourceGenerator._

/**
  * Created by tomsorlie on 4/9/16.
  */
object ParseTransformer {

  private def createParam(caseClassParam: Term.Param): MetaParam = {

    caseClassParam.decltpe.get match {
      case targ"StringField" =>
        StringFieldParam(
          caseClassParam.name,
          caseClassParam.decltpe.get,
          caseClassParam.default.get
        )
      case _ => BaseParam(caseClassParam.name, caseClassParam.decltpe.get)
    }

  }

  def parseTree(tree: Parsed[Source]): Seq[RecordInstance] = tree.get.collect {
    case q"$mods class $name (..$params)" =>
      val caseClassParams = params map {
        case caseClassParam: Term.Param =>
          createParam(caseClassParam)
      }

      RecordInstance(name, caseClassParams)

  }
}

