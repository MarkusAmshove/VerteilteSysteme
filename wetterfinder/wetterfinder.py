import os
import pika
import sys
import pyowm
import json
from pika import PlainCredentials

OWM_API_KEY = os.getenv("OWM_API_KEY")
RAW_GEOLOCATION_QUEUE = "rawGeoLocations"
WETTER_QUEUE = "gefundenesWetter"

owm = pyowm.OWM(OWM_API_KEY)

connection = pika.BlockingConnection(
    pika.ConnectionParameters('192.168.0.151', credentials=PlainCredentials("gast", "gast")))

locationChannel = connection.channel()
locationChannel.queue_declare(RAW_GEOLOCATION_QUEUE, durable=True)

wetterChannel = connection.channel()
wetterChannel.queue_declare(WETTER_QUEUE, durable=True)


class Location:
    def __init__(self, latitude, longitude):
        self.latitude = latitude
        self.longitude = longitude


class Wetter:
    def __init__(self, status):
        self.status = status


def location_decoder(obj):
    return Location(obj['latitude'], obj['longitude'])


class WetterFinder:
    def __init__(self, owm, publishChannel, publishQueueName):
        self.owm = owm
        self.publishChannel = publishChannel
        self.publishQueueName = publishQueueName
        self.gefundeneWetter = 0

    def get_wetter(self, wetterresponse):
        return wetterresponse.get_weather().get_status()

    def tweet_erhalten(self, channel, method, properties, body):
        location = json.loads(body, object_hook=location_decoder)
        if location.latitude < -90 or location.latitude > 90 or location.longitude < -90 or location.longitude > 90:
            return

        wetterresponse = self.owm.weather_at_coords(location.longitude, location.latitude)
        daswetter = self.get_wetter(wetterresponse)
        channel.basic_ack(delivery_tag=method.delivery_tag)
        if daswetter is None:
            return
        self.gefundeneWetter += 1
        sys.stdout.write("\rGefundene Wetter: " + str(self.gefundeneWetter))
        sys.stdout.flush()
        wetter = Wetter(daswetter)
        self.publishChannel.basic_publish(exchange='',
                                          routing_key=self.publishQueueName,
                                          body=json.dumps(wetter.__dict__))


wetterfinder = WetterFinder(owm, wetterChannel, WETTER_QUEUE)

locationChannel.basic_consume(wetterfinder.tweet_erhalten,
                              queue=RAW_GEOLOCATION_QUEUE)

locationChannel.start_consuming()
