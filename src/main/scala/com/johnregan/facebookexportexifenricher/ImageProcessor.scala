package com.johnregan.facebookexportexifenricher

import java.io.{BufferedOutputStream, File, FileOutputStream}
import java.text.SimpleDateFormat
import java.util.{Date, TimeZone}

import com.johnregan.facebookexportexifenricher.CommonUtil.getFileTree
import com.typesafe.config.ConfigFactory
import org.apache.commons.imaging.Imaging
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet

object ImageProcessor extends App {
  val config = ConfigFactory.load()

  var inputDir = config.getString("file.dir.input")
  var outputDir = config.getString("file.dir.output")

  timer {
    processDirectory
  }

  private def processDirectory = {
    val inputDirectory = new File(inputDir)
    val files = getFileTree(inputDirectory)
      .withFilter(_.getName.endsWith(".jpg")).foreach(processImage)
  }

  private def processImage(input: File) = {
    val outputSet = getTiffOutputSet(input)
    val filename = input.getName
    addExifData(outputSet, filename)
    val output = new BufferedOutputStream(new FileOutputStream(s"$outputDir/$filename"))
    new ExifRewriter().updateExifMetadataLossless(input, output, outputSet)
  }

  private def addExifData(outputSet: TiffOutputSet, filename: String) = {
    val exifDir = outputSet.getOrCreateExifDirectory()
    exifDir.removeField(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL)
    exifDir.add(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL, getOutputDate(filename))

    FacebookExifCache.getExifData(filename) map { exif =>
      def getGPS(direction: String) = exif.getOrElse(direction, "0").toDouble

      outputSet.setGPSInDegrees(getGPS(Constants.ExifLongitude), getGPS(Constants.ExifLatitude))
    }
  }

  private def getOutputDate(fileName: String): String = {
    val sdf = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
    sdf.setTimeZone(TimeZone.getTimeZone("GMT"))
    sdf.format(FacebookExifCache.getDate(fileName).getOrElse(new Date));
  }

  private def getTiffOutputSet(input: File): TiffOutputSet = {
    Option(Imaging.getMetadata(input))
      .flatMap(imgMeta => Option(imgMeta.asInstanceOf[JpegImageMetadata]))
      .flatMap(jpegMeta => Option(jpegMeta.getExif))
      .fold(new TiffOutputSet())(_.getOutputSet)
  }

  private def timer[R](block: => R): R = {
    val t0 = System.currentTimeMillis()
    val result = block
    val t1 = System.currentTimeMillis()
    println("Elapsed time: " + (t1 - t0) + "ms")
    result
  }
}