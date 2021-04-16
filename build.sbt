name := "Zadania"

version := "0.1"

scalaVersion := "2.13.5"


libraryDependencies ++= Seq(
  "com.rabbitmq"   % "amqp-client" % "5.11.0",
  "org.slf4j" % "slf4j-api" % "1.7.29",
  "org.slf4j" % "slf4j-simple" % "1.6.2",
  "org.apache.zookeeper" % "zookeeper" % "3.6.1",
  "org.scalatest" %% "scalatest" % "3.2.5" % Test
)