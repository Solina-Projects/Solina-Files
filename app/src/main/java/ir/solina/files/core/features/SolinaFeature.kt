package ir.solina.files.core.features

/**
 * Optional capabilities of Solina Files that the user may turn off from Settings.
 *
 * Rules:
 *  - [essential] features can never be disabled (file browsing, settings).
 *  - A feature whose [requires] set contains a disabled feature is effectively disabled too.
 *  - Disabling only hides the entry points; stored data (keys, connections, plugin pins) is never touched.
 */
enum class SolinaFeature(val key: String, val essential: Boolean = false) {
    FILE_BROWSER("file_browser", essential = true),
    SETTINGS("settings", essential = true),
    NETWORK_STORAGE("network_storage"),
    LAN_SHARING("lan_sharing"),
    LANGUAGES("languages"),
    APP_MANAGER("app_manager"),
    COLOR_PICKER("color_picker"),
    SIGNING_KEYS("signing_keys"),
    PLUGINS("plugins"),
    VAULT("vault"),
    ;

    /** Features that must be enabled for this one to work. */
    val requires: Set<SolinaFeature>
        get() = when (this) {
            LAN_SHARING -> setOf(NETWORK_STORAGE)
            else -> emptySet()
        }

    companion object {
        fun fromKey(key: String): SolinaFeature? = entries.firstOrNull { it.key == key }
    }
}

/** Immutable set of features the user turned off, with the resolution rules in one place. */
data class FeatureSet(val disabled: Set<SolinaFeature> = emptySet()) {

    fun isEnabled(feature: SolinaFeature): Boolean = resolve(feature, HashSet())

    private fun resolve(feature: SolinaFeature, visiting: MutableSet<SolinaFeature>): Boolean {
        if (feature.essential) return true
        if (feature in disabled) return false
        if (!visiting.add(feature)) return false // dependency cycle: fail closed
        val ok = feature.requires.all { resolve(it, visiting) }
        visiting.remove(feature)
        return ok
    }

    /** Returns a copy with [feature] switched on or off. Essential features are ignored. */
    fun with(feature: SolinaFeature, enabled: Boolean): FeatureSet {
        if (feature.essential) return this
        return FeatureSet(if (enabled) disabled - feature else disabled + feature)
    }

    fun encode(): String = disabled.filterNot { it.essential }.map { it.key }.sorted().joinToString(",")

    companion object {
        fun decode(text: String?): FeatureSet {
            if (text.isNullOrBlank()) return FeatureSet()
            val parsed = text.split(',').mapNotNull { SolinaFeature.fromKey(it.trim()) }.filterNot { it.essential }
            return FeatureSet(parsed.toSet())
        }
    }
}
