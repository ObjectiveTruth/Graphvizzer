package common

import com.typesafe.config.ConfigFactory

object constants {
    object IMGUR {
        val UPLOAD_IMAGE_ENDPOINT = "https://api.imgur.com/3/image"
        val CLIENT_ID = "34b1e110bd71758"
    }

    object SLACK {
        val OAUTH_ENDPOINT = "https://slack.com/api/oauth.access"
        val CLIENT_ID = ConfigFactory.load().getString("SLACK_CLIENT_ID")
        val APP_SECRET = ConfigFactory.load().getString("SLACK_APP_SECRET")
    }
}
