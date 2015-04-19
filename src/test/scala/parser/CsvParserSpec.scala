package parser

import org.parboiled2.ParserInput
import org.scalatest.{FlatSpec, ShouldMatchers}
import scala.util.{Failure, Success}

/**
 * Created by Mark on 3/29/15.
 */

class CsvParserSpec extends FlatSpec with ShouldMatchers {
  val MultilineTabSeparatedFile = "a|b|c\nc|c|c"

  it should "parse multiline strings" in {
    val parser = new CsvParser(ParserInput(MultilineTabSeparatedFile), "|")

    parser.parsed() match {
      case Success(result) =>
        result shouldEqual Vector(Vector("a", "b", "c"), Vector("c", "c", "c"))
      case Failure(e) => fail(e)
    }

  }

  it should "not fail when quotes are inside a field" in {
    // given
    val line1 = """ala,("ma"),kota"""
    val line2 = """ala,asdf"ma"asdf,kota"""

    // when & then
    new CsvParser(ParserInput(line1), ",").file.run().get(0) should contain allOf("ala", """("ma")""", "kota")
    new CsvParser(ParserInput(line2), ",").file.run().get(0) should contain allOf("ala", """asdf"ma"asdf""", "kota")
  }

  it should "parse line with tabular separator" in {
    // given
    val line = "ala\tma\tkota"

    // when
    val csvLines = new CsvParser(ParserInput(line), "\t").file.run().get

    // then
    csvLines should have size 1
    csvLines(0) should contain allOf("ala", "ma", "kota")
  }

  it should "parse line with default separator" in {
    // given
    val line = "ala,ma,kota"

    // when
    val csvLines = new CsvParser(ParserInput(line), ",").file.run().get

    // then
    csvLines should have size 1
    csvLines(0) should contain allOf("ala", "ma", "kota")
  }

  it should "correctly parse lines with escaped fields with delimiters" in {
    val testLine = """"field1","field2","field3 with , before to end","field4""""

    new CsvParser(ParserInput(testLine), ",").parsed() match {
      case Success(result) => println(result)
        result.head.length shouldEqual 4
      case Failure(e) => fail(e)
    }

  }
}
