package slatekit.core.alerts

/**
 * Settings for Alerts, currently just a list of targets
 */
data class AlertSettings(val targets:List<AlertTarget>)