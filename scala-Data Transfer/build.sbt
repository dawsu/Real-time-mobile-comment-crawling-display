ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.11.2"


lazy val root = (project in file("."))
  .settings(
    name := "kafka"
  )
libraryDependencies += "org.apache.spark" %% "spark-core" % "2.4.8"
libraryDependencies += "org.apache.spark" %% "spark-streaming" % "2.4.8"
libraryDependencies += "org.apache.spark" %% "spark-streaming-kafka-0-10" % "2.4.8"
libraryDependencies += "org.apache.spark" %% "spark-sql" % "2.4.8"
libraryDependencies += "mysql" % "mysql-connector-java" % "8.0.28"

