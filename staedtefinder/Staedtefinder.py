import googlemaps
import json
import os
import pika
import sys
from pika import PlainCredentials

GOOGLE_API_KEY = os.getenv("GOOGLE_API_KEY")
RAW_GEOLOCATION_QUEUE = "rawGeoLocations"
STAEDTE_GEOLOCATION_QUEUE = "gefundeneStaedte"

gmaps = googlemaps.Client(key=GOOGLE_API_KEY)

connection = pika.BlockingConnection(
    pika.ConnectionParameters('192.168.0.151', credentials=PlainCredentials("gast", "gast")))

locationChannel = connection.channel()
locationChannel.queue_declare(RAW_GEOLOCATION_QUEUE, durable=True)

staedteChannel = connection.channel()
staedteChannel.queue_declare(STAEDTE_GEOLOCATION_QUEUE, durable=True)


class Location:
    def __init__(self, latitude, longitude):
        self.latitude = latitude
        self.longitude = longitude

    def jsonDefault(self):
        return object.__dict__


class Stadt:
    def __init__(self, name):
        self.name = name


def location_decoder(obj):
    return Location(obj['latitude'], obj['longitude'])


def get_city(response):
    try:
        first_result = response[0]
        for component in first_result['address_components']:
            if 'locality' in component['types']:
                return component['long_name']

        return None
    except:
        return None


class StaedteFinder:
    def __init__(self, publishChannel, publishQueueName):
        self.publishChannel = publishChannel
        self.publishQueueName = publishQueueName
        self.gefundeneStaedte = 0

    def tweet_erhalten(self, channel, method, properties, body):
        location = json.loads(body, object_hook=location_decoder)
        reverse_geocode = gmaps.reverse_geocode((location.latitude, location.longitude))
        city = get_city(reverse_geocode)
        channel.basic_ack(delivery_tag=method.delivery_tag)
        if city is None:
            return
        self.gefundeneStaedte += 1
        sys.stdout.write("\rGefundene Staedte: " + str(self.gefundeneStaedte))
        sys.stdout.flush()
        stadt = Stadt(city)
        self.publishChannel.basic_publish(exchange='',
                                          routing_key=self.publishQueueName,
                                          body=json.dumps(stadt.__dict__))


staedteFinder = StaedteFinder(staedteChannel, STAEDTE_GEOLOCATION_QUEUE)

locationChannel.basic_consume(staedteFinder.tweet_erhalten,
                              queue=RAW_GEOLOCATION_QUEUE)

locationChannel.start_consuming()
