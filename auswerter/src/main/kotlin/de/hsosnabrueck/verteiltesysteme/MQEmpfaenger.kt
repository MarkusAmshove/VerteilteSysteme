package de.hsosnabrueck.verteiltesysteme

import com.google.gson.Gson
import com.rabbitmq.client.*
import javafx.application.Platform
import java.nio.charset.Charset

class MQEmpfaenger(private val host: String, private val queueName: String, private val wetterCallback: (WetterAenderung) -> Unit) {
    private var channel: Channel

    init {
        val connectionFactory = ConnectionFactory()
        connectionFactory.host = host
        connectionFactory.username = "gast"
        connectionFactory.password = "gast"
        val newConnection = connectionFactory.newConnection()
        channel = newConnection.createChannel()
        channel.queueDeclare(queueName, true, false, false, null)
        channel.basicConsume(queueName, MQKonsument(channel, wetterCallback))
    }
}

class MQKonsument(channel: Channel, private val wetterCallback: (WetterAenderung) -> Unit) : DefaultConsumer(channel) {

    override fun handleDelivery(consumerTag: String, envelope: Envelope, properties: AMQP.BasicProperties, body: ByteArray) {
        try {
            val json = body.toString(Charset.forName("UTF-8"))
            val wetterAenderung = Gson().fromJson<WetterAenderung>(json, WetterAenderung::class.java)
            Platform.runLater {
                wetterCallback(wetterAenderung)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

data class WetterAenderung(var status: String = "", var tweets: Int = 0)