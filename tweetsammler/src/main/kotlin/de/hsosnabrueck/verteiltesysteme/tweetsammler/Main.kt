package de.hsosnabrueck.verteiltesysteme.tweetsammler

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody

fun main(args: Array<String>) {
    mainBody("Tweetsammler") {
        Argumente(ArgParser(args)).run {
            println(hashTags.joinToString("\n"))
        }
    }
}


class Argumente(parser: ArgParser) {
    val hashTags by parser.adding("--tags", "-t", help = "Hashtags", transform = {
        "#$this"
    })
}