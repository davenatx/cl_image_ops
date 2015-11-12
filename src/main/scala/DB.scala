package com.austindata

import scala.slick.driver.H2Driver.simple._
import scala.slick.jdbc.{ GetResult, StaticQuery => Q }

import java.sql.Date

object DBImageRecord {

  /**
   * Domain Object representing an Image Record
   */
  case class ImageRecord(fileName: String, filePath: String, fileDate: Date, compression: Int, imageWidth: Long, imageLength: Long, xResolution: Long, yResolution: Long, isOverlayed: Boolean, id: Option[Int] = None)

  /**
   * Slick Table object
   */
  class ImageRecords(tag: Tag) extends Table[ImageRecord](tag, "IMAGE_RECORDS") {
    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
    def fileName = column[String]("FNAME")
    def filePath = column[String]("FPATH")
    def fileDate = column[Date]("FILEDATE")
    def compression = column[Int]("COMPRESSION")
    def imageWidth = column[Long]("IMG_WIDTH")
    def imageLength = column[Long]("IMG_LENGTH")
    def xResolution = column[Long]("X_RESOLUTION")
    def yResolution = column[Long]("Y_RESOLUTION")
    def isOverlayed = column[Boolean]("IS_OVERLAYED", O.Default(false))
    def * = (fileName, filePath, fileDate, compression, imageWidth, imageLength, xResolution, yResolution, isOverlayed, id.?) <> (ImageRecord.tupled, ImageRecord.unapply)
    def fileNameIndex = index("IDX_FNAME", fileName, unique = true)
    def fileDateIndex = index("IDX_FDATE", fileDate, unique = false)
    def imageWidthIndex = index("IDX_IMG_WIDTH", imageWidth, unique = false)
    def imageLengthIndex = index("IDX_IMG_LENGTH", imageLength, unique = false)
    def isOverlayedIndex = index("IDX_IS_OVERLAY", isOverlayed, unique = false)
  }

  /**
   * The query interface for the IndexRecords table
   */
  val imageRecords: TableQuery[ImageRecords] = TableQuery[ImageRecords]

  /**
   * Create the tables
   */
  def createTables {
    database withSession { implicit session =>
      imageRecords.ddl.create
    }
  }

  /**
   * Drop the tables
   */
  def dropTables {
    database withSession { implicit session =>
      imageRecords.ddl.drop
    }
  }

  /**
   * Insert one IndexRecord into IndexRecords table
   */
  def insert(record: ImageRecord) {
    database withSession { implicit session =>
      imageRecords += (record)
    }
  }

  /**
   * Batch Insert a sequence of IndexRecord into IndexRecords
   */
  def insert(records: Seq[ImageRecord]) {
    database withSession { implicit session =>
      imageRecords ++= records
    }
  }

  // Implicit conversion to map ResultSet to ImageRecord
  implicit val getImageRecordResult = GetResult(r => ImageRecord(r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<))

  /**
   * Query to retrieve cropped images where the DPI is 300 or 72
   */
  def croppedImages: List[ImageRecord] = {
    database withSession { implicit session =>
      Q.queryNA[ImageRecord]("""
      SELECT *
      FROM IMAGE_RECORDS
      WHERE IMG_LENGTH < IMG_WIDTH
      AND (X_RESOLUTION = 300 OR X_RESOLUTION=72)
      AND IS_OVERLAYED = false
      ORDER BY FILEDATE
      """).list
    }
  }

  /**
   * Query to retrieve images where IS_OVERLAYED is true
   */
  def overlayedImages: List[ImageRecord] = {
    database withSession { implicit session =>
      Q.queryNA[ImageRecord]("""
      SELECT *
      FROM IMAGE_RECORDS
      WHERE IS_OVERLAYED = true
      """).list
    }
  }

  /**
   * Query to retireve records by document number
   *
   * This method expects a document number without a page extension
   */
  def documentRecords(documentNumber: String): List[ImageRecord] = {
    database withSession { implicit session =>

      val documentQuery = Q.query[String, ImageRecord]("""
        SELECT *
        FROM IMAGE_RECORDS
        WHERE FNAME LIKE ?
        """)
      documentQuery(documentNumber + "%").list
    }
  }

  /**
   * Query to update isOverlay
   */
  def updateIsOverlayed(imageRecord: ImageRecord, isOverlay: Boolean): Int = {
    database withSession { implicit session =>
      // Update Query
      val updateOverlay = Q.update[(Boolean, Int)]("""
        UPDATE IMAGE_RECORDS
        SET IS_OVERLAYED = ?
        WHERE ID = ?
        """)
      // Perform update and return number of roms updates
      updateOverlay(isOverlay, imageRecord.id.getOrElse(-1)).first
    }
  }
}