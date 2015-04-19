
import sources.countries.Countries
import sources.countries.Countries.{CountryParser, Country, Countries}
import slick.driver.PostgresDriver.api._
import sources.genders.Genders
import sources.genders.Genders.GenderParser

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
 * Created by Mark on 3/29/15.
 */
object Main {
  def main(args: Array[String]) {
    val db = Database.forConfig("pgLocalDev")
    try {
      val clist = CountryParser.run
      val glist = GenderParser.run
      val actions = Countries.save(clist, db) >> Genders.save(glist, db)
      val doActions = db.run(actions)
      Await.result(doActions, Duration.Inf)
    } finally db.close
  }

}
