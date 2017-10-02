package de.hsosnabrueck.verteiltesysteme.tweetsammler

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody
import twitter4j.*
import twitter4j.conf.ConfigurationBuilder

fun main(args: Array<String>) {
    mainBody("Tweetsammler") {
        Argumente(ArgParser(args)).run {

            if(hashTags.isNotEmpty()) {
                println("Filtere nach Hashtags:")
                println(hashTags.joinToString("\n"))
            }


            val configurationBuilder = ConfigurationBuilder()

            configurationBuilder.setOAuthConsumerKey(System.getenv("TWITTER_CONSUMERKEY"))
            configurationBuilder.setOAuthConsumerSecret(System.getenv("TWITTER_CONSUMERSECRET"))
            configurationBuilder.setOAuthAccessToken(System.getenv("TWITTER_ACCESSTOKEN"))
            configurationBuilder.setOAuthAccessTokenSecret(System.getenv("TWITTER_ACCESSSECRET"))


            val twitterStreamFactory = TwitterStreamFactory(configurationBuilder.build())
            val twitterStream = twitterStreamFactory.instance

            StreamListenerHelper.addStatusListener(twitterStream, Listener())

            val filterQuery = FilterQuery()
            if (hashTags.isNotEmpty()) {
                filterQuery.track(*hashTags.toTypedArray())
            }
            else {
                filterQuery.locations(*ganzeWelt)
            }

            twitterStream.filter(filterQuery)
        }
    }
}

val ganzeWelt = arrayOf(doubleArrayOf(-180.0, -90.0), doubleArrayOf(180.0, 90.0))


class Listener : StatusListener {
    override fun onTrackLimitationNotice(numberOfLimitedStatuses: Int) {
    }

    override fun onStallWarning(warning: StallWarning?) {
    }

    override fun onException(ex: Exception) {
        ex.printStackTrace()
    }

    override fun onDeletionNotice(statusDeletionNotice: StatusDeletionNotice?) {
    }

    override fun onStatus(status: Status) {
        if(status.geoLocation==null) return
        println("(${status.createdAt})[${status.geoLocation.latitude}:${status.geoLocation.longitude}] @${status.user.screenName}")
    }

    override fun onScrubGeo(userId: Long, upToStatusId: Long) {
    }

}

class Argumente(parser: ArgParser) {
    val hashTags by parser.adding("--tags", "-t", help = "Hashtags", transform = {
        "#$this"
    })
}