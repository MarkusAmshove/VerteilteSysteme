# -*- coding: utf-8 -*-
import sqlite3
import pika
import json
import sys
from pika import PlainCredentials


class Datenbank:
    def __init__(self, datenbankName):
        self.datenbank = sqlite3.connect(datenbankName)
        self.datenbank.text_factory = str
        self.datenbank.execute("CREATE TABLE IF NOT EXISTS Staedte ( Staedtename TEXT, Tweets INT)")

    def stadt_existiert(self, staedtename):
        ergebnis = self.datenbank.execute("SELECT COUNT(*) FROM Staedte WHERE Staedtename=?", (staedtename,)).fetchone()
        return ergebnis[0] > 0

    def lege_stadt_an(self, staedtename):
        self.datenbank.execute("INSERT INTO Staedte (Staedtename, Tweets) VALUES (?,?)", (staedtename, 1))
        self.datenbank.commit()

    def update_stadt(self, staedtename):
        self.datenbank.execute("UPDATE Staedte SET Tweets = Tweets + 1 WHERE Staedtename=?", (staedtename,))
        self.datenbank.commit()

    def ermittle_anzahl_tweets(self, staedtename):
        return self.datenbank.execute("SELECT Tweets FROM Staedte WHERE Staedtename=?", (staedtename,)).fetchone()


class StadtFinder:
    def __init__(self, dieDatenbank):
        self.datenbank = dieDatenbank
        self.zugeordneteTweets = 0

    def stadt_gefunden(self, channel, method, properties, body):
        stadt = str(json.loads(body)['name'].encode('utf-8'))
        stadt_existiert = self.datenbank.stadt_existiert(stadt)
        if stadt_existiert:
            self.datenbank.update_stadt(stadt)
        else:
            self.datenbank.lege_stadt_an(stadt)
        self.zugeordneteTweets += 1
        sys.stdout.write("\rZugeordnete Tweets: " + str(self.zugeordneteTweets))
        sys.stdout.flush()
        channel.basic_ack(delivery_tag=method.delivery_tag)


connection = pika.BlockingConnection(
    pika.ConnectionParameters('192.168.0.151', credentials=PlainCredentials("gast", "gast")))

gefundeneStaedteChannel = connection.channel()
gefundeneStaedteChannel.queue_declare("gefundeneStaedte", durable=True)

datenbank = Datenbank("datenbank.db")
stadtFinder = StadtFinder(datenbank)

print("Los gehts")
gefundeneStaedteChannel.basic_consume(stadtFinder.stadt_gefunden,
                                      queue="gefundeneStaedte")
gefundeneStaedteChannel.start_consuming()
