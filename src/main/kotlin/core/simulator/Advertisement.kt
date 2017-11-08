package core.simulator

/**
 * Created on 08-11-2017
 *
 * @author David Fialho
 *
 * An advertisement is a data class that specifies the advertiser and the time at which it will/did take place.
 */
data class Advertisement(val advertiser: Advertiser, val time: Time)