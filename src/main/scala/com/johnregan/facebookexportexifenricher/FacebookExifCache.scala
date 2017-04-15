package com.johnregan.facebookexportexifenricher

import java.io.File
import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalDateTime, LocalTime, ZoneOffset}
import java.util.Date

import com.johnregan.facebookexportexifenricher.CommonUtil.getFileTree
import com.typesafe.config.ConfigFactory
import org.jsoup.Jsoup

object FacebookExifCache {
  val exportFolder = ConfigFactory.load().getString("file.dir.input") + "/photos"

  private val exifCache = initExifCache()
  private val dateCache = initDateCache()

  def getExifData(fileName: String): Option[Map[String, String]] = exifCache.get(fileName)

  def getDate(fileName: String): Option[Date] = dateCache.get(fileName)

  private def initExifCache(): Map[String, Map[String, String]] = {
    val inputDirectory = new File(exportFolder)
    import scala.collection.JavaConverters._

    getFileTree(inputDirectory)
      .withFilter(_.getName.endsWith(".htm")).map(f => Jsoup.parse(f, null))
      .map(_.getElementsByClass("block")).flatMap(_.asScala)
      .map(el => (el.children().attr("src").split("/").last,
        el.children().select("table").select("tr").select("th,td").asScala.toList
          .map(_.text).grouped(2).toList.map(entry => entry(0) -> entry(1)).toMap)).toMap
      .filter(exifEntry => exifEntry._2.contains(Constants.ExifLatitude)
        && exifEntry._2.contains(Constants.ExifLongitude))
  }

  private def initDateCache(): Map[String, Date] = {
    val inputDirectory = new File(exportFolder)
    import scala.collection.JavaConverters._

    getFileTree(inputDirectory)
      .withFilter(_.getName.endsWith(".htm")).map(f => Jsoup.parse(f, null))
      .map(_.getElementsByClass("block")).flatMap(_.asScala)
      .map(el => (el.children().attr("src").split("/").last,
        el.children().select("div[class=meta]").first().text()))
      .map((entry) => (entry._1, convertRawStringToDate(entry._2))).toMap
  }

  private def convertRawStringToDate(input: String): Date = {
    val toParse = input.split(",").last.split("at").head.trim
    val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy");
    val epoch = LocalDateTime.of(LocalDate.parse(toParse, formatter), LocalTime.MIN).toEpochSecond(ZoneOffset.UTC)
    new Date(epoch * 1000)
  }
}