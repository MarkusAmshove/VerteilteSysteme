# -*- coding: utf-8 -*-
import sqlite3
import pika
import json
import sys
from pika import PlainCredentials


class Datenbank:
    def __init__(self, datenbankname):
        self.datenbank = sqlite3.connect(datenbankname)
        self.datenbank.text_factory = str
        self.datenbank.execute("CREATE TABLE IF NOT EXISTS Wetter ( Status TEXT, Tweets INT)")

    def wetter_existiert(self, status):
        ergebnis = self.datenbank.execute("SELECT COUNT(*) FROM Wetter WHERE Status=?", (status,)).fetchone()
        return ergebnis[0] > 0

    def lege_wetter_an(self, status):
        self.datenbank.execute("INSERT INTO Wetter (Status, Tweets) VALUES (?,?)", (status, 1))
        self.datenbank.commit()

    def update_wetter(self, status):
        self.datenbank.execute("UPDATE Wetter SET Tweets = Tweets + 1 WHERE Status=?", (status,))
        self.datenbank.commit()

    def ermittle_anzahl_tweets(self, status):
        return self.datenbank.execute("SELECT Tweets FROM Wetter WHERE Status=?", (status,)).fetchone()[0]

    def alle_stati(self):
        return self.datenbank.execute("SELECT Status, Tweets FROM Wetter").fetchall()


class WetterStatistik(object):
    def __init__(self, status, tweets):
        self.status = status
        self.tweets = tweets


class StadtFinder:
    def __init__(self, diedatenbank, statistikchannel):
        self.datenbank = diedatenbank
        self.zugeordneteTweets = 0
        self.statistikchannel = statistikchannel

    def stadt_gefunden(self, channel, method, properties, body):
        status = str(json.loads(body)['status'])
        if len(status) == 0:
            return
        wetter_existiert = self.datenbank.wetter_existiert(status)
        if wetter_existiert:
            self.datenbank.update_wetter(status)
        else:
            self.datenbank.lege_wetter_an(status)
        self.zugeordneteTweets += 1
        sys.stdout.write("\rZugeordnete Tweets: " + str(self.zugeordneteTweets))
        sys.stdout.flush()
        channel.basic_ack(delivery_tag=method.delivery_tag)

        tweets_zu_wetter = self.datenbank.ermittle_anzahl_tweets(status)
        wetterstatistik = WetterStatistik(status, tweets_zu_wetter)
        self.statistikchannel.basic_publish(exchange='',
                                            routing_key="wetterStatistik",
                                            body=json.dumps(wetterstatistik.__dict__))


connection = pika.BlockingConnection(
    pika.ConnectionParameters('192.168.1.1', credentials=PlainCredentials("gast", "gast")))

gefundeneStaedteChannel = connection.channel()
gefundeneStaedteChannel.queue_declare("gefundenesWetter", durable=True)

wetterStatistikChannel = connection.channel()
wetterStatistikChannel.queue_declare("wetterStatistik", durable=True)

datenbank = Datenbank("datenbank.db")

for row in datenbank.alle_stati():
    if len(row[0]) == 0:
        continue
    print(row[0])
    wetterstatistik = WetterStatistik(row[0], row[1])
    wetterStatistikChannel.basic_publish(exchange='',
                                         routing_key="wetterStatistik",
                                         body=json.dumps(wetterstatistik.__dict__))

stadtFinder = StadtFinder(datenbank, wetterStatistikChannel)

print("Los gehts")
gefundeneStaedteChannel.basic_consume(stadtFinder.stadt_gefunden,
                                      queue="gefundenesWetter")
gefundeneStaedteChannel.start_consuming()
