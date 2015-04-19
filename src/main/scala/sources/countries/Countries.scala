package sources.countries


import java.io.InputStream
import org.parboiled2.ParserInput
import parser.CsvParser
import slick.driver.PostgresDriver.api._
import slick.jdbc.meta.MTable
import scala.concurrent.duration.Duration
import scala.concurrent.{Future, Await}
import scala.io.Source
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by Mark on 4/19/15.
 */
object Countries {

  //definition of countries table
  case class Country(name: String, isoCode: String, id: Option[Int] = None)

  class Countries(tag: Tag) extends Table[Country](tag, "Countries") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    // This is the primary key column
    def name = column[String]("name")

    def isoCode = column[String]("isoCode")

    // Every table needs a * projection with the same type as the table's type parameter
    def * = (name, isoCode, id.?) <> (Country.tupled, Country.unapply)
  }

  val countries = TableQuery[Countries]

  def save(ct: List[(String, String)], db: Database): Unit = {

    try {
      val createIfNotExists = {
        println("gonna try and print")
        DBIO.seq(
          MTable.getTables(countries.baseTableRow.tableName).map(result => if(result.isEmpty) db.run(countries.schema.create))
        )
      }

      val setUpFuture: Future[Unit] = db.run(createIfNotExists)
      val f = setUpFuture.flatMap { _ =>
        val insertAction: DBIO[Option[Int]] = countries ++= ct.map { c => Country(c._2, c._1)}
        val insertAndPrintAction: DBIO[Unit] = insertAction.map { countriesInsertResult =>
          // Print the number of rows inserted
          countriesInsertResult foreach { numRows =>
            println(s"Inserted $numRows rows into the Countries table")
          }
        }
        db.run(insertAndPrintAction)
      }
      Await.result(f, Duration.Inf)
    } finally db.close()
  }

  object CountryParser {
    val stream: InputStream = getClass.getResourceAsStream("/CountryCode.csv")
    println("got stream")
    val countryCodes = Source.fromInputStream(stream).mkString
    println("got string")
    def run: List[(String, String)] = {
      val p = new CsvParser(ParserInput(countryCodes), ",").file.run()
      val countries: List[(String, String)] = p match {
          //we need to drop the first row which is just column names from the csv that is why we end with .tail
        case Success(x) => x.asInstanceOf[Vector[Vector[String]]].toList.map { case Vector(iso, nm) => Tuple2(iso, nm) }.tail
        case Failure(e) => List[(String, String)]()
      }
      countries
    }
  }
}

