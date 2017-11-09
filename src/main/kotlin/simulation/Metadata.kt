package simulation

/**
 * Created on 09-11-2017
 *
 * @author David Fialho
 *
 * Container holding all key and value pairs that constitute the simulation run metadata.
 */
class Metadata(version: String): Iterable<Pair<String, Any>> {

    private val data = LinkedHashMap<String, Any>()

    init {
        data["Version"] = version
    }

    operator fun set(key: String, value: Any) {
        data[key] = value
    }

    operator fun get(key: String): Any? = data[key]

    // Iterator based on the map's iterator. It converts entries to pairs of key values
    private class MetadataIterator(private val iterator: MutableIterator<MutableMap.MutableEntry<String, Any>>):
            Iterator<Pair<String, Any>> {

        /**
         * Returns `true` if the iteration has more elements.
         */
        override fun hasNext(): Boolean {
            return iterator.hasNext()
        }

        /**
         * Returns the next element in the iteration.
         */
        override fun next(): Pair<String, Any> {
            val (key, value) = iterator.next()
            return Pair(key, value)
        }

    }

    /**
     * Returns an iterator over the elements of this object.
     */
    override fun iterator(): Iterator<Pair<String, Any>> {
        return MetadataIterator(data.iterator())
    }
}
