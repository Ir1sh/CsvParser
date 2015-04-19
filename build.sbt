name := "CsvParser"

version := "1.0"

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  "com.typesafe" % "config" % "1.2.1",
  "org.parboiled" %% "parboiled" % "2.1.0",
  "org.scalatest" % "scalatest_2.11" % "2.2.0" % "test",
  "com.typesafe.slick" %% "slick" % "3.0.0-RC3",
  "org.slf4j" % "slf4j-nop" % "1.6.4",
  "org.postgresql" % "postgresql" % "9.3-1102-jdbc4"
)
    