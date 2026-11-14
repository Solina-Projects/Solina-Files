package ir.solina.files.feature.settings

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import ir.solina.files.R
import ir.solina.files.core.data.SolinaSettings
import ir.solina.files.core.features.SolinaFeature
import ir.solina.files.core.i18n.tr

private class FeatureRow(
    val feature: SolinaFeature,
    @StringRes val title: Int,
    @StringRes val summary: Int,
    val icon: ImageVector,
)

private val rows = listOf(
    FeatureRow(SolinaFeature.NETWORK_STORAGE, R.string.feature_network_title, R.string.feature_network_summary, Icons.Rounded.Cloud),
    FeatureRow(SolinaFeature.LAN_SHARING, R.string.feature_sharing_title, R.string.feature_sharing_summary, Icons.Rounded.Wifi),
    FeatureRow(SolinaFeature.LANGUAGES, R.string.feature_languages_title, R.string.feature_languages_summary, Icons.Rounded.Terminal),
    FeatureRow(SolinaFeature.APP_MANAGER, R.string.feature_apps_title, R.string.feature_apps_summary, Icons.Rounded.Apps),
    FeatureRow(SolinaFeature.COLOR_PICKER, R.string.feature_color_title, R.string.feature_color_summary, Icons.Rounded.Palette),
    FeatureRow(SolinaFeature.SIGNING_KEYS, R.string.feature_keys_title, R.string.feature_keys_summary, Icons.Rounded.Key),
    FeatureRow(SolinaFeature.PLUGINS, R.string.feature_plugins_title, R.string.feature_plugins_summary, Icons.Rounded.Extension),
    FeatureRow(SolinaFeature.VAULT, R.string.feature_vault_title, R.string.feature_vault_summary, Icons.Rounded.Lock),
)

/** Lets the user show or hide optional tools. Only entry points are hidden; stored data is never touched. */
@Composable
fun FeaturesPage(settings: SolinaSettings, act: SettingsActions, onBack: () -> Unit) {
    SettingsPageScaffold(stringResource(R.string.settings_page_features), onBack, subtitle = stringResource(R.string.settings_features_subtitle)) {
        SettingsGroup(stringResource(R.string.settings_features_group), footer = stringResource(R.string.settings_features_footer)) {
            rows.forEach { row ->
                val parentsOn = row.feature.requires.all { settings.features.isEnabled(it) }
                switch(
                    tr(row.title),
                    settings.features.isEnabled(row.feature),
                    { v -> act.run { setFeatureEnabled(row.feature, v) } },
                    summary = if (parentsOn) tr(row.summary) else tr(R.string.feature_needs_parent),
                    icon = row.icon,
                    enabled = parentsOn,
                )
            }
        }
        SettingsGroup {
            link(
                tr(R.string.settings_features_reset),
                icon = Icons.Rounded.RestartAlt,
            ) { SolinaFeature.entries.filterNot { it.essential }.forEach { f -> act.run { setFeatureEnabled(f, true) } } }
        }
    }
}
