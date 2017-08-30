package io

import simulation.DataSet

/**
 * Created on 29-08-2017
 *
 * @author David Fialho
 */
interface Reporter<in S: DataSet> {

    /**
     * Reports a data set.
     */
    fun report(data: S)

    /**
     * Resets the reporter to its initial state.
     */
    fun reset()

}