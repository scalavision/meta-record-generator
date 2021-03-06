scalaVersion := "2.11.8"

libraryDependencies += "org.scalameta" %% "scalameta" % "0.1.0-SNAPSHOT"

name := "metaRecordGenerator"

libraryDependencies += "net.liftweb" %% "lift-mongodb-record" % "3.0-RC2"
/*
libraryDependencies ++= (Seq( 
  "org.scala-lang" % "scala-reflect" % "2.11.8",
  "org.scala-lang" % "scala-compiler"       % "2.11.8",
  "org.scala-lang" % "scala-library"        % "2.11.8"
))
*/

EclipseKeys.withSource := true
