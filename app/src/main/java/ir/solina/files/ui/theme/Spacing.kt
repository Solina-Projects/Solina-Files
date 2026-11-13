package ir.solina.files.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Shared layout spacing for consistent Material 3 surfaces and file-browser content.
 * Keep one-off component dimensions local; use these tokens for recurring layout gaps.
 */
object SolinaSpacing {
    val extraSmall: Dp = 4.dp
    val small: Dp = 8.dp
    val medium: Dp = 12.dp
    val large: Dp = 16.dp
    val extraLarge: Dp = 24.dp
    val extraExtraLarge: Dp = 32.dp

    val rowHorizontal: Dp = 6.dp
    val rowMetadataGap: Dp = 6.dp
}
