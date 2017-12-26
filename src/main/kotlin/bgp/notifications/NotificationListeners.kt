package bgp.notifications

/**
 * An [ExportListener] listens for [ExportNotification]s.
 *
 * Created on 25-07-2017.
 *
 * @author David Fialho
 */
interface ExportListener {

    /**
     * Invoked when an export notification is issued.
     */
    fun onExport(notification: ExportNotification)
}

/**
 * An [LearnListener] listens for [LearnNotification]s.
 *
 * Created on 25-07-2017.
 *
 * @author David Fialho
 */
interface LearnListener {

    /**
     * Invoked when a learn notification is issued.
     */
    fun onLearn(notification: LearnNotification)
}

/**
 * An [DetectListener] listens for [DetectNotification]s.
 *
 * Created on 25-07-2017.
 *
 * @author David Fialho
 */
interface DetectListener {

    /**
     * Invoked when a detect notification is issued.
     */
    fun onDetect(notification: DetectNotification)
}

/**
 * An [SelectListener] listens for [SelectNotification]s.
 *
 * Created on 25-07-2017.
 *
 * @author David Fialho
 */
interface SelectListener {

    /**
     * Invoked when a select notification is issued.
     */
    fun onSelect(notification: SelectNotification)
}
