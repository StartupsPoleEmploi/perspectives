package fr.poleemploi.perspectives.commun.infra.csv

import akka.NotUsed
import akka.stream.alpakka.csv.scaladsl.CsvFormatting
import akka.stream.scaladsl.{Concat, Source}
import akka.util.ByteString

/**
  * Generates a source of bytestring representing CSV data, with a header and a body
  * @tparam T type of object generated to CSV
  */
trait CsvGenerator[T] {

  /**
    * List of CSV header names
    * @return
    */
  def csvHeader: List[String]

  /**
    * Method applied to every data object in the source to transform it into string list
    * Size of the returning list should be the same as csvHeader list
    * @param dto input object in source
    * @return list of values to be set in CSV columns, or None if object should not be present in export
    */
  def dtoToCsv(dto: T): Option[List[String]]

  def generate(source: Source[T, NotUsed]): Source[ByteString, NotUsed] = {
    val dataSource = source
      .map(dtoToCsv)
      .filter(_.isDefined)
      .map(_.get)
    val headerSource = Source.single(csvHeader)

    Source
      .combine(headerSource, dataSource)(Concat[List[String]])
      .via(CsvFormatting.format())
  }

}
