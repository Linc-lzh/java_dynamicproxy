package net.sf.cglib.core.internal;

import java.util.concurrent.*;

public class LoadingCache<K, KK, V> {
    protected final ConcurrentMap<KK, Object> map;
    protected final Function<K, V> loader;
    protected final Function<K, KK> keyMapper;

    public static final Function IDENTITY = new Function() {
        public Object apply(Object key) {
            return key;
        }
    };

    public LoadingCache(Function<K, KK> keyMapper, Function<K, V> loader) {
        this.keyMapper = keyMapper;
        this.loader = loader;
        this.map = new ConcurrentHashMap<KK, Object>();
    }

    @SuppressWarnings("unchecked")
    public static <K> Function<K, K> identity() {
        return IDENTITY;
    }

    public V get(K key) {
        // 这里得到就是EnhancerKey的代理对象
        final KK cacheKey = keyMapper.apply(key);
        Object v = map.get(cacheKey);
        if (v != null && !(v instanceof FutureTask)) {
            return (V) v;
        }
        // 传进去的key为Enhancer对象，cacheKey为EnhancerKey的代理对象，表示所设置的代理信息
        return createEntry(key, cacheKey, v);
    }

    /**
     * Loads entry to the cache.
     * If entry is missing, put {@link FutureTask} first so other competing thread might wait for the result.
     * @param key original key that would be used to load the instance
     * @param cacheKey key that would be used to store the entry in internal map
     * @param v null or {@link FutureTask<V>}
     * @return newly created instance
     */
    protected V createEntry(final K key, KK cacheKey, Object v) {
        FutureTask<V> task;
        boolean creator = false;
        if (v != null) {
            // Another thread is already loading an instance
            task = (FutureTask<V>) v;
        } else {
            // loader.apply(key)就会去创建代理对象
            task = new FutureTask<V>(new Callable<V>() {
                public V call() throws Exception {
                    // 这里就会去创建代理类了
                    return loader.apply(key);
                }
            });
            Object prevTask = map.putIfAbsent(cacheKey, task);
            if (prevTask == null) {
                // creator does the load
                creator = true;
                task.run();
            } else if (prevTask instanceof FutureTask) {
                task = (FutureTask<V>) prevTask;
            } else {
                return (V) prevTask;
            }
        }

        V result;
        try {
            result = task.get();
        } catch (InterruptedException e) {
            throw new IllegalStateException("Interrupted while loading cache item", e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw ((RuntimeException) cause);
            }
            throw new IllegalStateException("Unable to load cache item", cause);
        }
        if (creator) {
            // 代理类创建完之后会进行缓存
            map.put(cacheKey, result);
        }
        return result;
    }
}
