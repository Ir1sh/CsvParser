package sources.genders

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
object Genders {

  case class Gender(name: String, id: Option[Int] = None)

  class Genders(tag: Tag) extends Table[Gender](tag, "Gender") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name")

    def * = (name, id.?) <>(Gender.tupled, Gender.unapply)
  }

  val genders = TableQuery[Genders]

  def save(gt: List[String], db: Database): DBIO[Unit] = {
    val createIfNotExists = {
      DBIO.seq(
        MTable.getTables(genders.baseTableRow.tableName).map(result => if (result.isEmpty) db.run(genders.schema.create))
      )
    }
    val insertAction: DBIO[Option[Int]] = genders ++= gt.map { g => Gender(g) }
    val insertAndPrintAction: DBIO[Unit] = insertAction.map { gendersInsertResult =>
      // Print the number of rows inserted
      gendersInsertResult foreach { numRows =>
        println(s"Inserted $numRows rows into the Genders table")
      }
    }

    createIfNotExists >> insertAndPrintAction
  }

  object GenderParser {
    val stream: InputStream = getClass.getResourceAsStream("/Gender.csv")
    val genders = Source.fromInputStream(stream).mkString

    /**
     * Runs the parser on CountryCode.csv
     * @return
     */
    def run: List[(String)] = {
      val p = new CsvParser(ParserInput(genders), ",").file.run()
      println(p)
      val countries: List[(String)] = p match {
        //we need to drop the first row which is just column names from the csv that is why we end with .tail
        case Success(x) => x.asInstanceOf[Vector[Vector[String]]].toList.map { case Vector(nm) => nm }.tail
        case Failure(e) => List[(String)]()
      }
      countries
    }
  }

}

