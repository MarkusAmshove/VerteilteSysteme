package de.hsosnabrueck.verteiltesysteme

import com.google.gson.Gson
import com.rabbitmq.client.*

class MQEmpfaenger(private val host: String, private val queueName: String, private val wetterCallback: (WetterAenderung) -> Unit) {
    private var channel: Channel

    init {
        val connectionFactory = ConnectionFactory()
        connectionFactory.host = host
        connectionFactory.username = "gast"
        connectionFactory.password = "gast"
        val newConnection = connectionFactory.newConnection()
        channel = newConnection.createChannel()
        channel.queueDeclare(queueName, true, false, false, mutableMapOf())
        channel.basicConsume(queueName, MQKonsument(channel, wetterCallback))
    }
}

class MQKonsument(channel: Channel, private val wetterCallback: (WetterAenderung) -> Unit) : DefaultConsumer(channel) {

    override fun handleDelivery(consumerTag: String, envelope: Envelope, properties: AMQP.BasicProperties, body: ByteArray) {
        val wetterAenderung = Gson().fromJson<WetterAenderung>(body.toString(), WetterAenderung::class.java)
        wetterCallback(wetterAenderung)
    }
}

data class WetterAenderung(val status: String, val tweets: Int)