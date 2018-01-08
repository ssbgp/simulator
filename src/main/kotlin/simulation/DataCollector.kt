package simulation

/**
 * Created on 29-08-2017
 *
 * @author David Fialho
 *
 * A data collector is responsible for collecting data during an execution.
 *
 * Any method can be used to collect that. One of the most important methods is by using the notifications issued by
 * the simulator during the simulation. To do this look into the notifier classes tagged with the Notifier interfaced.
 */
interface DataCollector {

    /**
     * Helper method to perform the collection of data. It handles registering and un-registering the collector with
     * the necessary notifiers.
     */
    fun collect(body: () -> Unit): DataCollector {

        register()
        try {
            body()
        } finally {
            unregister()
        }

        return this
    }

    /**
     * Adds the collector as a listener for notifications the collector needs to listen to collect data.
     */
    fun register()

    /**
     * Removes the collector from all notifiers
     */
    fun unregister()

    /**
     * Reports the currently collected data.
     */
    fun report()

    /**
     * Clears all collected data.
     */
    fun clear()

}