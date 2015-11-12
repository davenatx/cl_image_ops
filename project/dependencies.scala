import sbt._
import sbt.Keys._

object Version {
  val scalaLogging = "3.1.0"
  val logback      = "1.1.3"
  val config       = "1.3.0"
  val dmpImageOps  = "0.1"
  val slick        = "2.1.0"
  val h2           = "1.4.189"
  val hakariCP     = "2.3.9"
  val specs2       = "3.6.4"
}

object Library {
  val scalaLogging = "com.typesafe.scala-logging"   %% "scala-logging" 	 % Version.scalaLogging
  val logback      = "ch.qos.logback"               %  "logback-classic" % Version.logback
  val config       = "com.typesafe"                 %  "config"          % Version.config
  val dmpImageOps  = "com.dmp"                      %% "dmp_image_ops"   % Version.dmpImageOps
  val slick        = "com.typesafe.slick"           %% "slick"           % Version.slick
  val h2           = "com.h2database"               %  "h2"              % Version.h2
  val hakariCP     = "com.zaxxer"                   %  "HikariCP-java6"  % Version.hakariCP
  val specs2       = "org.specs2"                   %% "specs2-core"     % Version.specs2
}

object Dependencies {
  import Library._
  
  val clImageOps = List(
    scalaLogging,
	  logback,
	  config,
	  dmpImageOps,
    slick,
    h2,
    hakariCP,
    specs2
	)
}