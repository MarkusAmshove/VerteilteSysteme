import subprocess
import os
import json
import time
import pika
import json
from pika import PlainCredentials


def ermittle_consumer_pro_queue(command):
    proc = subprocess.Popen(command,shell=True,stdout=subprocess.PIPE)
    script_response = proc.stdout.read().split('\n')
    resp=json.loads(script_response[8])
    return resp


def cls():
    os.system('cls' if os.name=='nt' else 'clear')


def ermittle_programm_name(consumername):
    return {
        'gefundenesWetter': 'Wettersammler',
        'rawGeoLocations': 'Wetterfinder',
        'wetterStatistik': 'Auswertungsprogramm',
        'tweetSammler': 'Tweetsammler',
        'starts': 'Startschlange'
    }[consumername]



if __name__ == '__main__':
    PROGRAMM_QUEUE = "starts"
    connection = pika.BlockingConnection(
        pika.ConnectionParameters('192.168.1.1', credentials=PlainCredentials("gast", "gast")))

    programm_channel = connection.channel()
    programm_channel.queue_declare(PROGRAMM_QUEUE, durable=True)
    while True:
        time.sleep(9)
        konsumenten_queues = ermittle_consumer_pro_queue('curl -i -u guest:guest http://localhost:15672/api/queues/')
        cls()
        for r in konsumenten_queues:
            programmname= ermittle_programm_name(str(r['name']))
            print("Programm: " + programmname + " Anzahl: " + str(r['consumers']))
            if r['name'] !=  'starts' and r['consumers'] < 1:
                print("\t Starte neuen " + programmname)
                programm_channel.basic_publish(exchange='',routing_key=PROGRAMM_QUEUE,body=programmname)
