import os
import pika
import sys
from pika import PlainCredentials
import subprocess

PROGRAMM_START_QUEUE = "starts"

connection = pika.BlockingConnection(
    pika.ConnectionParameters('192.168.1.1', credentials=PlainCredentials("gast", "gast")))

programmStartChannel = connection.channel()
programmStartChannel.queue_declare(PROGRAMM_START_QUEUE, durable=True)


def finde_start_kommando(programm):
    TWITTER_CONSUMERKEY = os.getenv("TWITTER_CONSUMERKEY", "")
    TWITTER_CONSUMERSECRET = os.getenv("TWITTER_CONSUMERECRET", "")
    TWITTER_ACCESSTOKEN = os.getenv("TWITTER_ACCESSTOKEN", "")
    TWITTER_ACCESSSECRET = os.getenv("2OxJBPvVosEEbKCEwhptkJlLuapPc08qkxnyP9hMSP2Pu", "")
    OWM_API_KEY = os.getenv("OWM_API_KEY", "")

    return {'Wettersammler': 'python /home/pi/scm/VerteilteSysteme/wettersammler/wettersammler.py &',
            'Wetterfinder': 'python /home/pi/scm/VerteilteSysteme/wetterfinder/wetterfinder.py &',
            'Auswertungsprogramm': '/home/pi/scm/VerteilteSysteme/auswertung/build/install/auswerter/bin/auswerter &',
            'Tweetsammler': '/home/pi/scm/VerteilteSysteme/tweetsammler/build/install/tweetsammler/bin/tweetsammler &'
            }[programm]


def starte_programm(self, channel, method, properties, body):
    # programm starten
    try:
        programmpfad = finde_start_kommando(body)
        print("Starte " + str(body))
        print("\t" + programmpfad)
        subprocess.call(['bash', '-c', programmpfad])
        channel.basic_ack(delivery_tag=method.delivery_tag)
    except:
        pass


print("Warte auf zu startende Programme")

programmStartChannel.basic_consume(starte_programm,
                                   queue=PROGRAMM_START_QUEUE)
