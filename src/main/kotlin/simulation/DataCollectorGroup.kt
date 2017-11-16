package simulation

/**
 * Created on 29-08-2017
 *
 * @author David Fialho
 *
 * The data collector group handles multiple collectors. It provides the same interface as a data collector
 * implementation. Therefore, it can be used as any other data collector.
 */
class DataCollectorGroup: DataCollector {

    private val collectors = mutableListOf<DataCollector>()

    /**
     * Adds a new collector to the group.
     */
    fun add(collector: DataCollector) {
        collectors.add(collector)
    }

    /**
     * Registers all collectors in the group.
     */
    override fun register() {
        collectors.forEach { it.register() }
    }

    /**
     * Unregisters all collectors in the group.
     */
    override fun unregister() {
        collectors.forEach { it.unregister() }
    }

    /**
     * Reports collected data from all collectors in the group.
     */
    override fun report() {
        collectors.forEach { it.report() }
    }

    /**
     * Clears all data from all collectors in the group.
     */
    override fun clear() {
        collectors.forEach { it.clear() }
    }

}