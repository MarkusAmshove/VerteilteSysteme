package de.hsosnabrueck.verteiltesysteme.tweetsammler;

import twitter4j.StatusListener;
import twitter4j.TwitterStream;

public class StreamListenerHelper {
    public static void addStatusListener(TwitterStream stream, StatusListener listener) {
        stream.addListener(listener);
    }

    public static void removeStatusListener(TwitterStream stream, StatusListener listener) {
        stream.removeListener(listener);
    }
}
