package ir.solina.files.core.features

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SolinaFeatureTest {

    @Test
    fun everythingIsEnabledByDefault() {
        val set = FeatureSet()
        SolinaFeature.entries.forEach { assertThat(set.isEnabled(it)).isTrue() }
    }

    @Test
    fun essentialFeaturesCannotBeDisabled() {
        var set = FeatureSet()
        SolinaFeature.entries.filter { it.essential }.forEach { set = set.with(it, false) }
        assertThat(set.disabled).isEmpty()
        assertThat(set.isEnabled(SolinaFeature.FILE_BROWSER)).isTrue()
        assertThat(set.isEnabled(SolinaFeature.SETTINGS)).isTrue()
    }

    @Test
    fun disablingAFeatureDisablesItsDependents() {
        val set = FeatureSet().with(SolinaFeature.NETWORK_STORAGE, false)
        assertThat(set.isEnabled(SolinaFeature.NETWORK_STORAGE)).isFalse()
        assertThat(set.isEnabled(SolinaFeature.LAN_SHARING)).isFalse()
        assertThat(set.isEnabled(SolinaFeature.PLUGINS)).isTrue()
    }

    @Test
    fun reEnablingRestoresDependents() {
        val set = FeatureSet().with(SolinaFeature.NETWORK_STORAGE, false).with(SolinaFeature.NETWORK_STORAGE, true)
        assertThat(set.isEnabled(SolinaFeature.LAN_SHARING)).isTrue()
    }

    @Test
    fun encodeDecodeRoundTrips() {
        val set = FeatureSet().with(SolinaFeature.PLUGINS, false).with(SolinaFeature.VAULT, false)
        val text = set.encode()
        assertThat(text).isEqualTo("plugins,vault")
        assertThat(FeatureSet.decode(text)).isEqualTo(set)
    }

    @Test
    fun decodeIgnoresUnknownBlankAndEssentialKeys() {
        assertThat(FeatureSet.decode(null)).isEqualTo(FeatureSet())
        assertThat(FeatureSet.decode("")).isEqualTo(FeatureSet())
        val set = FeatureSet.decode("bogus, file_browser ,plugins,")
        assertThat(set.disabled).containsExactly(SolinaFeature.PLUGINS)
    }

    @Test
    fun keysAreUnique() {
        assertThat(SolinaFeature.entries.map { it.key }.toSet()).hasSize(SolinaFeature.entries.size)
    }
}
