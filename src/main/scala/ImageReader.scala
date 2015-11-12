package com.austindata

import java.io.File
import com.dmp.image.TIFFImage
import DBImageRecord._
import com.typesafe.scalalogging.LazyLogging
import java.sql.Date
import java.util.regex.Pattern
import scala.util.{ Try, Success, Failure }

object ImageReader extends LazyLogging {

  /**
   * Recursively read TIFF files from the image repository
   *
   */
  def traverse(dir: File, func: File => Unit): Unit = {
    dir.listFiles.sorted foreach { (f) =>
      {
        if (f.isDirectory) {
          traverse(f, func)
        } else {
          func(f)
        }
      }
    }
  }

  /**
   * Read the TIFF and create the database record
   */
  def processTIFF(file: File): Unit = {
    logger.info("Processing File: " + file.getName + ", Dir: " + file.getParent)
    /* These are all single page TIFF files so headOption is OK to use becuase there is only one page */
    TIFFImage.fromFile(file).headOption map (img => {
      val imageRecord = ImageRecord(
        file.getName,
        file.getParent,
        dateFromPath(file.getParent),
        img.compression.getOrElse(0),
        img.imageWidth.getOrElse(0),
        img.imageLength.getOrElse(0),
        img.xResolution.getOrElse(0),
        img.yResolution.getOrElse(0),
        false
      )

      // Log warning message if DB exception occurs
      Try(insert(imageRecord)) match {
        case Failure(thrown) => {
          logger.warn("Failed to insert: " + imageRecord + "; Exception: " + thrown)
        }
        case Success(s) => {
          logger.debug("Inserted: " + imageRecord)
        }
      }

    })
  }

  // RegEx Pattern to split on file separator
  val pattern = Pattern.quote(System.getProperty("file.separator"))

  /**
   * Create Date using the image repository path
   */
  private def dateFromPath(path: String): Date = {
    val arr = path.split(pattern)
    Date.valueOf(arr(4) + "-" + arr(5) + "-" + arr(6))
  }

  /* Partially applied function */
  val readAndInsertFunc = traverse(_: File, processTIFF)

}

/**
 * Populate the database with TIFF informatoin from the image repository
 */
object PopulateDatabase extends App {
  //dropTables
  createTables
  val rootDir = new File(sourceDirectory)
  ImageReader.readAndInsertFunc(rootDir)
}

