package de.hsosnabrueck.verteiltesysteme.tweetsammler

import com.google.gson.Gson
import com.rabbitmq.client.Channel
import com.rabbitmq.client.ConnectionFactory
import twitter4j.GeoLocation

class MQSender(private val host: String, private val queueName: String) {

    private var channel: Channel

    init {
        val connectionFactory = ConnectionFactory()
        connectionFactory.host = host
        connectionFactory.username = "gast"
        connectionFactory.password = "gast"
        val newConnection = connectionFactory.newConnection()
        channel = newConnection.createChannel()
        channel.queueDeclare(queueName, true, false, false, mutableMapOf())
    }

    fun neueLocation(geoLocation: GeoLocation) {
        val message = Gson().toJson(geoLocation)
        channel.basicPublish("", queueName, null, message.toByteArray())
    }
}