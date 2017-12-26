package core.simulator.notifications

import core.routing.Message
import core.routing.Topology
import core.simulator.Time
import core.simulator.currentTime

/**
 * Created on 25-07-2017.
 *
 * @author David Fialho
 *
 * Base class for all notifications. All notifications are associated with the [time] at which
 * they were issued.
 *
 * @property time the time at which the notification was issued
 */
abstract class Notification(val time: Time = currentTime())

/**
 * Created on 25-07-2017.
 *
 * @author David Fialho
 *
 * Notification issued when a simulation starts.
 *
 * @property seed     the initial seed used to generate the message delays
 * @property topology the topology used for the simulation
 */
data class StartNotification(val seed: Long, val topology: Topology<*>) : Notification()

/**
 * Created on 25-07-2017.
 *
 * @author David Fialho
 *
 * Notification issued when a simulation ends. It is issued regardless of whether or not the
 * threshold of the simulation was reached.
 *
 * @property topology the topology used for the simulation
 */
data class EndNotification(val topology: Topology<*>) : Notification()

/**
 * Created on 25-07-2017.
 *
 * @author David Fialho
 *
 * Notification issued when the threshold of the simulation is reached.
 *
 * @property threshold the value of the threshold set for the simulation
 */
class ThresholdReachedNotification(val threshold: Time) : Notification()

/**
 * Created on 29-08-2017
 *
 * @author David Fialho
 *
 * Notification issued when a [message] is sent.
 */
data class MessageSentNotification(val message: Message<*>) : Notification()

/**
 * Created on 29-08-2017
 *
 * @author David Fialho
 *
 * Notification issued when a [message] arrives at its recipient.
 */
data class MessageReceivedNotification(val message: Message<*>) : Notification()