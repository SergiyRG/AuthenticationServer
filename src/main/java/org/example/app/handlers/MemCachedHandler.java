package org.example.app.handlers;

import com.whalin.MemCached.MemCachedClient;
import com.whalin.MemCached.SockIOPool;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component("memCachedHandler")
public class MemCachedHandler implements CachedHandler {

    public static final String CACHE = "AuthenticationCache";
    private static final MemCachedClient client;
    public static final int SECONDS_TO_MILLI = 1_000;

    static {
        String[] servers = {"host.docker.internal:11211"};
        SockIOPool pool = SockIOPool.getInstance(CACHE);
        pool.setMinConn(2);
        pool.setMaxConn(20);
        pool.setServers(servers);
        pool.setFailover(true);
        pool.setInitConn(30);
        pool.setMaintSleep(90);
        pool.setSocketTO(3000);
        pool.setAliveCheck(true);
        pool.initialize();

        client = new MemCachedClient(CACHE);
    }

    @Override
    public boolean add(String key, Object value, int secondLife) {
        return client.add(key, value, getExpiryDate(secondLife));
    }

    private Date getExpiryDate(int secondLife) {
        return new Date(SECONDS_TO_MILLI * secondLife);
    }

    @Override
    public boolean contains(String key) {
        return client.keyExists(key);
    }

    @Override
    public boolean set(String key, Object value, int secondLife) {
        return client.set(key, value, getExpiryDate(secondLife));
    }

    @Override
    public boolean delete(String key) {
        return set(key, get(key), 0);
    }

    @Override
    public Object get(String key) {
        return client.get(key);
    }
}
