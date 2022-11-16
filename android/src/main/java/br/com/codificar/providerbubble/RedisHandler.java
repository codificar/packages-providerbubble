package br.com.codificar.providerbubble;

import java.util.logging.Handler;

import br.com.codificar.providerbubble.RNProviderBubbleModule;

import io.lettuce.core.RedisClient;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.api.sync.RedisPubSubCommands;
import io.lettuce.core.RedisURI;

/**
 * A handler to a Redis PubSub client. Can handle multiple subscribed channels at a time.
 */
public class RedisHandler {
    private static RedisHandler singletonHandler;   // singleton instance
    private RedisPubSubCommands<String, String> redisInConnection = null;  // the Redis connection for subscribe

    /**
     * Get the singleton Redis handler instance
     * 
     * @param redisURI the Redis connection URI
     * @param module the RNProviderBubbleModule instance
     * 
     * @return the handler singleton instance
     */
    public static synchronized RedisHandler getInstance(String redisURI, RNProviderBubbleModule module) throws Exception {
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
    private RedisHandler(String redisURI, RNProviderBubbleModule module) throws Exception {
        try {
            if (redisURI == null || redisURI.length() == 0) {
                throw new Exception("Redis Uri is empty");
            }

            RedisClient redisClient = RedisClient.create(RedisURI.create(redisURI));

            StatefulRedisPubSubConnection<String, String> connection = redisClient.connectPubSub();
                
            connection.addListener(new RedisPubSubAdapter<String, String>() {
                @Override
                public void message(String channel, String message) {
                    module.handleMessage("redis", message);
                }
            });
    
            redisInConnection = connection.sync();
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }
    }

    /**
     * Subscribe a PubSub channel
     * 
     * @param channel the redis channel name
     */
    public void subscribePubSub(String channel) {
        try {
            if (redisInConnection != null) {
                redisInConnection.subscribe(channel);
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
            if (redisInConnection != null) {
                redisInConnection.unsubscribe(channel);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}