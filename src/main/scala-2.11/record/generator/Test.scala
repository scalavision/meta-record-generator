package record.generator

import java.io.FileInputStream
import scala.meta._
import scala.meta.dialects.Scala211

object Test {

  def main(args: Array[String]): Unit = {
    val path = new java.io.File(".")
    val sourcePath = (path.getAbsolutePath + "src/main/scala-2.11/record/generator/Model.scala").replace(".src", "src")
    val stream = new FileInputStream(sourcePath)
    val tree: Parsed[Source] = stream.parse[Source]
    println(ParseTransformer.parseTree(tree).map(_.createRecord).mkString)
  }

}