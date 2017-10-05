package de.hsosnabrueck.verteiltesysteme.tweetsammler

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody
import twitter4j.*
import twitter4j.conf.ConfigurationBuilder

class Argumente(parser: ArgParser) {
    val hashTags by parser.adding("--tags", "-t", help = "Hashtags", transform = {
        "#$this"
    })

    val consumerKey by parser.storing("Consumer Key","-ck")
    val consumerSecret by parser.storing("Consumer Secret","-cs")
    val accessToken by parser.storing("Accesstoken", "-at")
    val accessSecret by parser.storing("Accesssecret", "-as")
}

fun main(args: Array<String>) {
    mainBody("Tweetsammler") {
        Argumente(ArgParser(args)).run {

            if(hashTags.isNotEmpty()) {
                println("Filtere nach Hashtags:")
                println(hashTags.joinToString("\n"))
            }

            val configurationBuilder = ConfigurationBuilder()

            /*configurationBuilder.setOAuthConsumerKey(System.getenv("TWITTER_CONSUMERKEY"))
            configurationBuilder.setOAuthConsumerSecret(System.getenv("TWITTER_CONSUMERSECRET"))
            configurationBuilder.setOAuthAccessToken(System.getenv("TWITTER_ACCESSTOKEN"))
            configurationBuilder.setOAuthAccessTokenSecret(System.getenv("TWITTER_ACCESSSECRET"))*/

            configurationBuilder.setOAuthConsumerKey(consumerKey)
            configurationBuilder.setOAuthConsumerSecret(consumerSecret)
            configurationBuilder.setOAuthAccessToken(accessToken)
            configurationBuilder.setOAuthAccessTokenSecret(accessSecret)



            val twitterStreamFactory = TwitterStreamFactory(configurationBuilder.build())
            val twitterStream = twitterStreamFactory.instance

            MQListener("192.168.1.1", "tweetSammler")

            val mqSender = MQSender("192.168.1.1", "rawGeoLocations")
            StreamListenerHelper.addStatusListener(twitterStream, Listener(mqSender))

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


class Listener(private val mqSender: MQSender) : StatusListener {
    private var gesendet = 0

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
        mqSender.neueLocation(status.geoLocation)
        gesendet++
        print("\rGesendet: $gesendet")
    }

    override fun onScrubGeo(userId: Long, upToStatusId: Long) {
    }

}

