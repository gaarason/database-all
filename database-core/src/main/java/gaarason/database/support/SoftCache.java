package gaarason.database.support;

import gaarason.database.lang.Nullable;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A memory-sensitive implementation of the <code>Map</code> interface.
 *
 * <p> A <code>SoftCache</code> object uses {@link java.lang.ref.SoftReference
 * soft references} to implement a memory-sensitive hash map.  If the garbage
 * collector determines at a certain point in time that a value object in a
 * <code>SoftCache</code> entry is no longer strongly reachable, then it may
 * remove that entry in order to release the memory occupied by the value
 * object.  All <code>SoftCache</code> objects are guaranteed to be completely
 * cleared before the virtual machine will throw an
 * <code>OutOfMemoryError</code>.  Because of this automatic clearing feature,
 * the behavior of this class is somewhat different from that of other
 * <code>Map</code> implementations.
 *
 * <p> Both null values and the null key are supported.  This class has the
 * same performance characteristics as the <code>HashMap</code> class, and has
 * the same efficiency parameters of <em>initial capacity</em> and <em>load
 * factor</em>.
 *
 * <p> Like most collection classes, this class is not synchronized.  A
 * synchronized <code>SoftCache</code> may be constructed using the
 * <code>Collections.synchronizedMap</code> method.
 *
 * <p> In typical usage this class will be subclassed and the <code>fill</code>
 * method will be overridden.  When the <code>get</code> method is invoked on a
 * key for which there is no mapping in the cache, it will in turn invoke the
 * <code>fill</code> method on that key in an attempt to construct a
 * corresponding value.  If the <code>fill</code> method returns such a value
 * then the cache will be updated and the new value will be returned.  Thus,
 * for example, a simple URL-content cache can be constructed as follows:
 *
 * <pre>
 *     public class URLCache extends SoftCache {
 *         protected Object fill(Object key) {
 *             return ((URL)key).getContent();
 *         }
 *     }
 * </pre>
 *
 * <p> The behavior of the <code>SoftCache</code> class depends in part upon
 * the actions of the garbage collector, so several familiar (though not
 * required) <code>Map</code> invariants do not hold for this class.  <p>
 * Because entries are removed from a <code>SoftCache</code> in response to
 * dynamic advice from the garbage collector, a <code>SoftCache</code> may
 * behave as though an unknown thread is silently removing entries.  In
 * particular, even if you synchronize on a <code>SoftCache</code> instance and
 * invoke none of its mutator methods, it is possible for the <code>size</code>
 * method to return smaller values over time, for the <code>isEmpty</code>
 * method to return <code>false</code> and then <code>true</code>, for the
 * <code>containsKey</code> method to return <code>true</code> and later
 * <code>false</code> for a given key, for the <code>get</code> method to
 * return a value for a given key but later return <code>null</code>, for the
 * <code>put</code> method to return <code>null</code> and the
 * <code>remove</code> method to return <code>false</code> for a key that
 * previously appeared to be in the map, and for successive examinations of the
 * key set, the value set, and the entry set to yield successively smaller
 * numbers of elements.
 * @author Mark Reinhold
 * @see java.util.HashMap
 * @see java.lang.ref.SoftReference
 * @see sun.misc.SoftCache
 * @since 1.2
 */


public class SoftCache<K, V> extends AbstractMap<K, V> implements Map<K, V> {

    /* Hash table mapping keys to ValueCells */
    private final Map<K, ValueCell<V>> hash;

    /* Reference queue for cleared ValueCells */
    private final ReferenceQueue<V> queue = new ReferenceQueue<>();

    @Nullable
    private transient Set<Map.Entry<K, V>> entrySet = null;

    /* -- Constructors -- */

    /**
     * Construct a new, empty <code>SoftCache</code> with the given
     * initial capacity and the given load factor.
     * @param initialCapacity The initial capacity of the cache
     * @param loadFactor A number between 0.0 and 1.0
     * @throws IllegalArgumentException If the initial capacity is less than
     * or equal to zero, or if the load
     * factor is less than zero
     */
    public SoftCache(int initialCapacity, float loadFactor) {
        hash = new ConcurrentHashMap<>(initialCapacity, loadFactor);
    }

    /**
     * Construct a new, empty <code>SoftCache</code> with the given
     * initial capacity and the default load factor.
     * @param initialCapacity The initial capacity of the cache
     * @throws IllegalArgumentException If the initial capacity is less than
     * or equal to zero
     */
    public SoftCache(int initialCapacity) {
        hash = new ConcurrentHashMap<>(initialCapacity);
    }

    /**
     * Construct a new, empty <code>SoftCache</code> with the default
     * capacity and the default load factor.
     */
    public SoftCache() {
        hash = new ConcurrentHashMap<>();
    }

    /* -- Simple queries -- */

    private static boolean valEquals(@Nullable Object o1, @Nullable Object o2) {
        return Objects.equals(o1, o2);
    }

    /**
     * Return the number of key-value mappings in this cache.  The time
     * required by this operation is linear in the size of the map.
     */
    public int size() {
        return entrySet().size();
    }

    /**
     * Return <code>true</code> if this cache contains no key-value mappings.
     */
    public boolean isEmpty() {
        return entrySet().isEmpty();
    }

    /* -- Lookup and modification operations -- */

    /**
     * Return <code>true</code> if this cache contains a mapping for the
     * specified key.  If there is no mapping for the key, this method will not
     * attempt to construct one by invoking the <code>fill</code> method.
     * @param key The key whose presence in the cache is to be tested
     */
    public boolean containsKey(Object key) {
        return ValueCell.strip(hash.get(key), false) != null;
    }

    @Nullable
    public V get(Object key) {
        processQueue();
        ValueCell<V> v = hash.get(key);
        return ValueCell.strip(v, false);
    }

    /**
     * Update this cache so that the given <code>key</code> maps to the given
     * <code>value</code>.  If the cache previously contained a mapping for
     * <code>key</code> then that mapping is replaced and the old value is
     * returned.
     * @param key The key that is to be mapped to the given
     * <code>value</code>
     * @param value The value to which the given <code>key</code> is to be
     * mapped
     * @return The previous value to which this key was mapped, or
     * <code>null</code> if is there was no mapping for the key
     */
    public V put(K key, V value) {
        processQueue();
        ValueCell<V> vc = ValueCell.create(key, value, queue);
        return ValueCell.strip(hash.put(key, vc), true);
    }

    /**
     * Remove the mapping for the given <code>key</code> from this cache, if
     * present.
     * @param key The key whose mapping is to be removed
     * @return The value to which this key was mapped, or <code>null</code> if
     * there was no mapping for the key
     */
    @Nullable
    public V remove(Object key) {
        processQueue();
        return ValueCell.strip(hash.remove(key), true);
    }

    /**
     * Remove all mappings from this cache.
     */
    public void clear() {
        processQueue();
        hash.clear();
    }

    /**
     * Return a <code>Set</code> view of the mappings in this cache.
     */
    public Set<Map.Entry<K, V>> entrySet() {
        if (this.entrySet == null) {
            Set<? extends Map.Entry<K, V>> entrySetTemp = new EntrySet();
            this.entrySet = (Set<Map.Entry<K, V>>) entrySetTemp;
        }
        return this.entrySet;
    }

    /* -- Views -- */

    /* Process any ValueCells that have been cleared and enqueued by the
       garbage collector.  This method should be invoked once by each public
       mutator in this class.  We don't invoke this method in public accessors
       because that can lead to surprising ConcurrentModificationExceptions.
     */
    private void processQueue() {
        ValueCell<?> vc;
        while ((vc = (ValueCell<?>) queue.poll()) != null) {
            if (vc.isValid()) {
                hash.remove(vc.key);
            } else {
                ValueCell.dropped--;
            }
        }
    }

    /* The basic idea of this implementation is to maintain an internal HashMap
       that maps keys to soft references whose referents are the keys' values;
       the various accessor methods dereference these soft references before
       returning values.  Because we don't have access to the innards of the
       HashMap, each soft reference must contain the key that maps to it so
       that the processQueue method can remove keys whose values have been
       discarded.  Thus the HashMap actually maps keys to instances of the
       ValueCell class, which is a simple extension of the SoftReference class.
     */
    static private class ValueCell<V> extends SoftReference<V> {
        static private final Object INVALID_KEY = new Object();
        static private int dropped = 0;
        private Object key;

        private ValueCell(Object key, V value, ReferenceQueue<V> queue) {
            super(value, queue);
            this.key = key;
        }

        @Nullable
        private static <V> ValueCell<V> create(Object key, @Nullable V value, ReferenceQueue<V> queue) {
            if (value == null) {
                return null;
            }
            return new ValueCell<>(key, value, queue);
        }

        @Nullable
        private static <V> V strip(@Nullable ValueCell<V> val, boolean drop) {
            if (val == null) {
                return null;
            }
            V realValue = val.get();
            if (drop) {
                val.drop();
            }
            return realValue;
        }

        private boolean isValid() {
            return (key != INVALID_KEY);
        }

        private void drop() {
            super.clear();
            key = INVALID_KEY;
            dropped++;
        }

    }

    /* Internal class for entries.
       Because it uses SoftCache.this.queue, this class cannot be static.
     */
    private class Entry implements Map.Entry<K, V> {
        private final Map.Entry<K, ValueCell<V>> ent;
        @Nullable
        private V value;   /* Strong reference to value, to prevent the GC
                                   from flushing the value while this Entry
                                   exists */

        Entry(Map.Entry<K, ValueCell<V>> ent, V value) {
            this.ent = ent;
            this.value = value;
        }

        public K getKey() {
            return ent.getKey();
        }

        @Nullable
        public V getValue() {
            return value;
        }

        @Nullable
        public V setValue(@Nullable V value) {
            this.value = value;
            return ValueCell.strip(ent.setValue(ValueCell.create(ent.getKey(), value, queue)), true);
        }

        public boolean equals(Object o) {
            if (!(o instanceof Map.Entry)) return false;
            Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
            return (valEquals(ent.getKey(), e.getKey())
                && valEquals(value, e.getValue()));
        }

        public int hashCode() {
            Object k;
            return ((((k = getKey()) == null) ? 0 : k.hashCode())
                ^ ((value == null) ? 0 : value.hashCode()));
        }

    }

    /* Internal class for entry sets */
    private class EntrySet extends AbstractSet<Entry> {
        Set<Map.Entry<K, ValueCell<V>>> hashEntries = hash.entrySet();

        public Iterator<Entry> iterator() {
            return new Iterator<Entry>() {
                final Iterator<Map.Entry<K, ValueCell<V>>> hashIterator = hashEntries.iterator();
                @Nullable
                Entry next = null;

                public boolean hasNext() {
                    while (hashIterator.hasNext()) {
                        Map.Entry<K, ValueCell<V>> ent = hashIterator.next();
                        ValueCell<V> vc = ent.getValue();
                        V v;
                        // (vc != null) && ((v = vc.get()) == null)
                        if ((v = vc.get()) == null) {
                            /* Value has been flushed by GC */
                            continue;
                        }
                        next = new Entry(ent, v);
                        return true;
                    }
                    return false;
                }

                public Entry next() {
                    if ((next == null) && !hasNext()) {
                        throw new NoSuchElementException();
                    }
                    Entry e = next;
                    next = null;
                    assert e != null;
                    return e;
                }

                public void remove() {
                    hashIterator.remove();
                }

            };
        }

        public boolean isEmpty() {
            return !(iterator().hasNext());
        }

        public int size() {
            int j = 0;
            for (Iterator<Entry> i = iterator(); i.hasNext(); i.next()) j++;
            return j;
        }

        public boolean remove(Object o) {
            processQueue();
            if (o instanceof SoftCache.Entry) {
                return hashEntries.remove(((SoftCache<?, ?>.Entry) o).ent);
            } else {
                return false;
            }
        }
    }

}

