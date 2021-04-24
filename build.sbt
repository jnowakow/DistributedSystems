name := "Zadania"

version := "0.1"

scalaVersion := "2.13.5"

val AkkaVersion = "2.6.14"
val SlickVersion = "3.3.3"

libraryDependencies ++= Seq(
  "com.rabbitmq"   % "amqp-client" % "5.11.0",
  "org.slf4j" % "slf4j-api" % "1.7.29",
  "org.slf4j" % "slf4j-simple" % "1.6.2",
  "org.apache.zookeeper" % "zookeeper" % "3.6.1",
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.slick" %% "slick" % SlickVersion,
  "org.xerial" % "sqlite-jdbc" % "3.34.0",
  "org.scalatest" %% "scalatest" % "3.2.5" % Test,
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion % Test,
)