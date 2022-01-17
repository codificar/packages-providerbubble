package br.com.codificar.providerbubble;

import android.util.Log;

import io.lettuce.core.RedisClient;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.RedisPubSubAdapter;

import br.com.codificar.providerbubble.RNProviderBubbleModule;
import io.lettuce.core.pubsub.api.sync.RedisPubSubCommands;

/**
 * A handler to a Redis PubSub client. Can handle multiple subscribed channels at a time.
 */
public class RedisHandler {

    private String redisURI;    // Redis connection  URI: redis://[password@]host[:port][/databaseNumber]

    RedisPubSubCommands<String, String> redisInConnection;
    RedisPubSubCommands<String, String> redisOutConnection;

    private RNProviderBubbleModule bridgeModule;

    private static RedisHandler singletonHandler;   // singleton instance
    public boolean alreadySubscription = false;

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
            if (redisURI != null && redisURI != "") {
                this.redisURI = redisURI;
                this.bridgeModule = module;

                // Create connection
                RedisClient client = RedisClient.create(this.redisURI);
                StatefulRedisPubSubConnection<String, String> connection = client.connectPubSub();

                connection.addListener(new RedisPubSubAdapter<String, String>() {
                    @Override
                    public void message(String channel, String message) {
                        singletonHandler.bridgeModule.handleMessage("redis", message);
                    }
                });

                redisInConnection = connection.sync();
                redisOutConnection = connection.sync();
            }
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
            if(redisInConnection != null && alreadySubscription == false) {
                redisInConnection.subscribe(channel);
                alreadySubscription = true;
            }
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
                alreadySubscription = false;
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
