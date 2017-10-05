import subprocess
import os
import json
import time


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
        'tweetSammler': 'Tweetsammler'
    }[consumername]


if __name__ == '__main__':
    while True:
        time.sleep(2)
        konsumenten_queues = ermittle_consumer_pro_queue('curl -i -u guest:guest http://localhost:15672/api/queues/')
        cls()
        for r in konsumenten_queues:
            print("Programm: " + ermittle_programm_name(str(r['name'])) + " Anzahl: " + str(r['consumers']))
