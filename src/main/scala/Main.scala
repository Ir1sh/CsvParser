import java.io.InputStream

import slick.backend.DatabaseConfig
import scala.concurrent.{Future, Await}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

import org.parboiled2.ParserInput
import parser.CsvParser
import slick.driver.PostgresDriver.api._
import slick.lifted.{ProvenShape, ForeignKeyQuery}

import scala.io.Source
import scala.util.{Failure, Success, Try}

/**
 * Created by Mark on 3/29/15.
 */
object Main {
  private lazy val stream: InputStream = getClass.getResourceAsStream("CountryCode.csv")
  private lazy val countryCodes = Source.fromInputStream(stream).mkString

  //definition of countries table
  case class Country(name: String, isoCode: String, id: Option[Int] = None)

  class Countries(tag: Tag) extends Table[Country](tag, "Countries") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    // This is the primary key column
    def name = column[String]("name")

    def isoCode = column[String]("isoCode")

    // Every table needs a * projection with the same type as the tabe's type parameter
    def * = (name, isoCode, id.?) <>(Country.tupled, Country.unapply)
  }

  val countries = TableQuery[Countries]

  def main(args: Array[String]) {
    val db = Database.forConfig("pgLocalDev")
    try {
      val countries = TableQuery[Countries]
      val irl = Country("Ireland", "ei")
      val eng = Country("England", "en")
      val sc = Country("Scotland", "sc")
      val wl = Country("Wales", "wl")

        // Insert some coffees (using JDBC's batch insert feature)
        val insertAction: DBIO[Option[Int]] = countries ++= Seq(irl, eng)

        val insertAndPrintAction: DBIO[Unit] = insertAction.map { countriesInsertResult =>
          // Print the number of rows inserted
          countriesInsertResult foreach { numRows =>
            println(s"Inserted $numRows rows into the Countries table")
          }
        }
        /*val allCountriesAction: DBIO[Seq[(Int, String, String)]] =
          TableQuery[Countries].result*/

        val combinedAction: DBIO[Unit] =
          insertAndPrintAction

        val combinedFuture: Future[Unit] =
          db.run(combinedAction)

        combinedFuture.map { allSuppliers =>
          println(allSuppliers)
        }


      Await.result(combinedFuture, Duration.Inf)
    } finally db.close()
    val p = new CsvParser(ParserInput(countryCodes), ",").file.run()
    val countries: List[(String, String)] = p match {
      case Success(x) => x.asInstanceOf[Vector[Vector[String]]].toList.map { case Vector(iso, nm) => Tuple2(iso, nm) }
      case Failure(e) => List[(String, String)]()
    }
    countries.map { x => Console.println(x._1 + ", " + x._2); x }
  }

}
