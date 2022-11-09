package br.com.codificar.providerbubble;

import android.util.Log;

import java.util.logging.Handler;

import br.com.codificar.providerbubble.RNProviderBubbleModule;

import io.lettuce.core.RedisClient;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.api.sync.RedisPubSubCommands;



/**
 * A handler to a Redis PubSub client. Can handle multiple subscribed channels at a time.
 */
public class RedisHandler {

    private String redisURI;    // Redis connection  URI: redis://[password@]host[:port][/databaseNumber]

    private RNProviderBubbleModule bridgeModule;

    private static RedisHandler singletonHandler;   // singleton instance

    private RedisPubSubCommands<String, String> redisInConnection;  // the Redis connection for subscribe
    private RedisPubSubCommands<String, String> redisOutConnection;  // the Redis connection for publish
    private boolean alreadySubscription = false;

    /**
     * Get the singleton Redis handler instance
     * 
     * @param redisURI the Redis connection URI
     * @param module the RNProviderBubbleModule instance
     * 
     * @return the handler singleton instance
     */
    public static synchronized RedisHandler getInstance(String redisURI, RNProviderBubbleModule module) {
        if (singletonHandler == null) {
            singletonHandler = new RedisHandler(redisURI, module);
        }
        
        return singletonHandler;
    }

    /**
     * Create a new Redis connection client with a single listener for all channels subscribed
     * 
     * @param redisURI the Redis connection URI
     * @param module the RNProviderBubbleModule instance
     */
    private RedisHandler(String redisURI, RNProviderBubbleModule module) {
        try {
            if (redisURI == null || redisURI.length() == 0) {
                return;
            }
    
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
    
            this.redisInConnection = connection.sync();
            this.redisOutConnection = connection.sync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Subscribe a PubSub channel
     * 
     * @param channel the redis channel name
     */
    public void subscribePubSub(String channel) {
        try {
            if (this.redisInConnection != null && this.alreadySubscription == false) {
                this.redisInConnection.subscribe(channel);
                this.alreadySubscription = true;
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
            if (this.redisInConnection != null) {
                this.redisInConnection.unsubscribe(channel);
                this.alreadySubscription = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
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
            if (this.redisOutConnection != null) {
              this.redisOutConnection.publish(channel, message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}