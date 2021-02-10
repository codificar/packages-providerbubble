package providerbubble;

import android.util.Log;

import java.util.logging.Handler;

import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.pubsub.RedisPubSubAdapter;
import com.lambdaworks.redis.pubsub.RedisPubSubConnection;

import providerbubble.BubbleServiceBridgeModule;

/**
 * A handler to a Redis PubSub client. Can handle multiple subscribed channels at a time.
 */
public class RedisHandler {

    private String redisURI;    // Redis connection  URI: redis://[password@]host[:port][/databaseNumber]
    private RedisPubSubConnection<String, String> redisInConnection;  // the Redis connection for subscribe
    private RedisPubSubConnection<String, String> redisOutConnection;  // the Redis connection for publish
    private RedisPubSubAdapter<String, String> listener;    // the Redis channels listener

    private BubbleServiceBridgeModule bridgeModule;

    private static RedisHandler singletonHandler;   // singleton instance

    /**
     * Get the singleton Redis handler instance
     * 
     * @param redisURI the Redis connection URI
     * @param module the BubbleServiceBridgeModule instance
     * 
     * @return the handler singleton instance
     */
    public static RedisHandler getInstance(String redisURI, BubbleServiceBridgeModule module) {
        if(singletonHandler == null) {
            singletonHandler = new RedisHandler(redisURI, module);
        }
        return singletonHandler;
    }

    /**
     * Create a new Redis connection client with a single listener for all channels subscribed
     * 
     * @param redisURI the Redis connection URI
     * @param module the BubbleServiceBridgeModule instance
     */
    private RedisHandler(String redisURI, BubbleServiceBridgeModule module) {
        this.redisURI = redisURI;
        this.bridgeModule = module;

        // Create connection
        RedisClient client = RedisClient.create(this.redisURI);
        redisInConnection = client.connectPubSub();
        redisOutConnection = client.connectPubSub();

        // Create and add the listener
        listener = new RedisPubSubAdapter<String, String>() {
            @Override
            public void message(String channel, String message) {
                singletonHandler.bridgeModule.handleMessage("redis", message);
            }
        };
        redisInConnection.addListener(listener);
    }

    /**
     * Subscribe a PubSub channel
     * 
     * @param channel the redis channel name
     */
    public void subscribePubSub(String channel) {
        redisInConnection.subscribe(channel);
    }

    /**
     * Unsubscribe a PubSub channel
     * 
     * @param channel the Redis channel name
     */
    public void unsubscribePubSub(String channel){
        if(redisInConnection != null) {
            redisInConnection.unsubscribe(channel);
        }
    }

    /**
     * Publish message on a PubSub channel
     * 
     * @param channel the Redis channel name
     * @param message the message to publish
     */
    public void publishPubSub(String channel, String message) {
        if(redisOutConnection != null) {
            redisOutConnection.publish(channel, message);
        }
    }
}