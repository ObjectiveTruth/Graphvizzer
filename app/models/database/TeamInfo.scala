package models.database

import slick.driver.H2Driver.api._
import slick.lifted.{ProvenShape, ForeignKeyQuery}

class TeamInfo(tag: Tag) extends Table[(String, String, String, String, String)](tag, "TEAM_INFO") {
    def userId: Rep[String] = column[String]("USER_ID", O.PrimaryKey)
    def teamId: Rep[String] = column[String]("TEAM_ID")
    def teamName: Rep[String] = column[String]("TEAM_NAME")
    def scope: Rep[String] = column[String]("SCOPE")
    def accessToken: Rep[String] = column[String]("ACCESS_TOKEN")

    def * : ProvenShape[(String, String, String, String, String)] = (userId, teamId, teamName, scope, accessToken)
}
