package com

import com.typesafe.config.ConfigFactory

import scala.slick.driver.H2Driver.simple._

import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.HikariConfig

import javax.sql.DataSource

package object austindata {
  private val config = ConfigFactory.load("settings.properties")

  lazy val sourceDirectory = config.getString("sourceDirectory")
  lazy val exportDirectory = config.getString("exportDirectory")
  lazy val overlayLength = config.getInt("overlayLength")
  /* DB Properties */
  lazy val dbUrl = config.getString("dbUrl")
  lazy val dbDriver = config.getString("dbDriver")

  val dataSource = {
    val config = new HikariConfig

    config.setDriverClassName(dbDriver)
    config.setJdbcUrl(dbUrl)
    config.setUsername("sa")
    config.setPassword("")
    config.setMaximumPoolSize(20)
    config.setMinimumIdle(10)

    new HikariDataSource(config)
  }

  val database = Database.forDataSource(dataSource)
}