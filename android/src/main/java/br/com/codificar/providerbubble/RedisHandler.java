package br.com.codificar.providerbubble;

import android.util.Log;

import java.util.logging.Handler;

import io.lettuce.core.RedisClient;
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands;
import io.lettuce.core.pubsub.RedisPubSubAdapter;

import br.com.codificar.providerbubble.RNProviderBubbleModule;

/**
 * A handler to a Redis PubSub client. Can handle multiple subscribed channels at a time.
 */
public class RedisHandler {

    private String redisURI;    // Redis connection  URI: redis://[password@]host[:port][/databaseNumber]
    private RedisPubSubAsyncCommands<String, String> redisInConnection;  // the Redis connection for subscribe
    private RedisPubSubAsyncCommands<String, String> redisOutConnection;  // the Redis connection for publish
    private RedisPubSubAdapter<String, String> listener;    // the Redis channels listener

    private RNProviderBubbleModule bridgeModule;

    private static RedisHandler singletonHandler;   // singleton instance

    /**
     * Get the singleton Redis handler instance
     *
     * @param redisURI the Redis connection URI
     * @param module the RNProviderBubbleModule instance
     *
     * @return the handler singleton instance
     */
    public static RedisHandler getInstance(String redisURI, RNProviderBubbleModule module) {
        try {
            if(singletonHandler == null) {
              singletonHandler = new RedisHandler(redisURI, module);
            }
            return singletonHandler;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Create a new Redis connection client with a single listener for all channels subscribed
     *
     * @param redisURI the Redis connection URI
     * @param module the RNProviderBubbleModule instance
     */
    private RedisHandler(String redisURI, RNProviderBubbleModule module) {
        try {
            this.redisURI = redisURI;
            this.bridgeModule = module;

            // Create connection
            RedisClient client = RedisClient.create(this.redisURI);

            redisInConnection = client.connectPubSub().async();
            redisOutConnection = client.connectPubSub().async();

            // Create and add the listener
            listener = new RedisPubSubAdapter<String, String>() {
                @Override
                public void message(String channel, String message) {
                  singletonHandler.bridgeModule.handleMessage("redis", message);
                }
            };

            redisInConnection.getStatefulConnection().addListener(listener);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Subscribe a PubSub channel
     *
     * @param channel the redis channel name
     */
    public void subscribePubSub(String channel) {
        try {
            redisInConnection.subscribe(channel);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Unsubscribe a PubSub channel
     *
     * @param channel the Redis channel name
     */
    public void unsubscribePubSub(String channel){
        try {
            if(redisInConnection != null) {
              redisInConnection.unsubscribe(channel);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Publish message on a PubSub channel
     *
     * @param channel the Redis channel name
     * @param message the message to publish
     */
    public void publishPubSub(String channel, String message) {
        try {
            if(redisOutConnection != null) {
              redisOutConnection.publish(channel, message);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}