
import sources.countries.Countries
import sources.countries.Countries.{CountryParser, Country, Countries}
import slick.driver.PostgresDriver.api._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
 * Created by Mark on 3/29/15.
 */
object Main {
  def main(args: Array[String]) {
    val db = Database.forConfig("pgLocalDev")
    val clist = CountryParser.run
    Countries.save(clist, db)
  }

}
