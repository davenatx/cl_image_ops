package com.austindata

import com.austindata.DBImageRecord._
import com.typesafe.scalalogging.LazyLogging
import java.nio.file.Files
import java.nio.file.Paths.get
import java.sql.Date
import java.util.Calendar
import java.util.regex.Pattern
import scala.util.{ Try, Success, Failure }
import scala.language.implicitConversions

object ImageExport extends LazyLogging {

  // Implicit conversion to convert string to path
  implicit def toPath(filename: String) = get(filename)

  // Used to track the number of files copied
  var fileCount = 0

  val calendar = Calendar.getInstance

  // Pattern used to split file name on period
  val pattern = Pattern.quote(".")

  /**
   * Export a list of Image Records
   */
  def processOverlayedImages(records: List[ImageRecord]) {
    records map (record => {
      val exportPath = createExportPath(record)
      processDocument(fileName2DocumentNumber(record.fileName), exportPath)
    })
    logger.info("Exported {} files", fileCount.toString)
  }

  /**
   * Build the output path and create the directory if necessary
   */
  private def createExportPath(record: ImageRecord): String = {

    val path = exportDirectory + "/" + date2Year(record.fileDate)

    // If export directory does not exists, create it
    Files.exists(path) match {
      case true => logger.trace("Export Directory alread exists: {}", path)
      case false => {
        Files.createDirectory(path)
        logger.trace("Export Directory created: {}", path)
      }
    }
    path
  }

  /**
   * Convert java.sql.Date to year as String
   */
  private def date2Year(date: Date): String = {
    calendar.setTime(date)
    calendar.get(Calendar.YEAR).toString
  }

  /**
   * Retrieve doucment number from file name
   */
  private def fileName2DocumentNumber(fileName: String): String = {
    fileName.split(pattern)(0)
  }

  /**
   * Export all files that represent this document number
   */
  private def processDocument(documentNumber: String, exportPath: String) {
    DBImageRecord.documentRecords(documentNumber) map (record => {
      val sourceFile = record.filePath + "/" + record.fileName
      val exportFile = exportPath + "/" + record.fileName

      // Do not attempt to copy file that already exists in export locatoin
      Files.exists(exportFile) match {
        case true => logger.trace("Export File already exists: ", exportFile)
        case false => {
          Try(Files.copy(sourceFile, exportFile)) match {
            case Failure(thrown) => {
              logger.warn("Failed to export file: {}, to directory: {}, with Exception: {}", record.fileName, exportPath, thrown.getMessage)
            }
            case Success(s) => {
              logger.info("Sucessfully exported file: {}, to directory: {}", record.fileName, exportPath)
              fileCount = fileCount + 1
            }
          }
        }
      }
    })
  }
}

/**
 * Export images
 */
object ExportImages extends App {
  /* Retrieve overlayed images from database and export them */
  ImageExport.processOverlayedImages(DBImageRecord.overlayedImages)
}

