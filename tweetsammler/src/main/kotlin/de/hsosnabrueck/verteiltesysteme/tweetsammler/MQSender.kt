package de.hsosnabrueck.verteiltesysteme.tweetsammler

import com.google.gson.Gson
import com.rabbitmq.client.*
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

class MQListener(private val host: String, private val queueName: String) {
    private var channel: Channel
    init {
        val connectionFactory = ConnectionFactory()
        connectionFactory.host = host
        connectionFactory.username = "gast"
        connectionFactory.password = "gast"
        val newConnection = connectionFactory.newConnection()
        channel = newConnection.createChannel()
        channel.queueDeclare(queueName, true, false, false, mutableMapOf())
        channel.basicConsume(queueName, true, NullConsumer())
    }
}

private class NullConsumer : Consumer {
    override fun handleRecoverOk(consumerTag: String?) {
    }

    override fun handleConsumeOk(consumerTag: String?) {
    }

    override fun handleShutdownSignal(consumerTag: String?, sig: ShutdownSignalException?) {
    }

    override fun handleCancel(consumerTag: String?) {
    }

    override fun handleDelivery(consumerTag: String?, envelope: Envelope?, properties: AMQP.BasicProperties?, body: ByteArray?) {
    }

    override fun handleCancelOk(consumerTag: String?) {
    }

}