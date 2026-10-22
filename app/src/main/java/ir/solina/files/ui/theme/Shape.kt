package ir.solina.files.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.Shapes
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import ir.solina.files.core.model.IconShape

fun solinaShapes(scale: Float): Shapes {
    fun r(dp: Int) = RoundedCornerShape((dp * scale).coerceAtLeast(2f).dp)
    return Shapes(
        extraSmall = r(8),
        small = r(12),
        medium = r(16),
        large = r(24),
        extraLarge = r(32),
        largeIncreased = r(28),
        extraLargeIncreased = r(36),
        extraExtraLarge = r(48),
    )
}

val SolinaShapes = solinaShapes(1f)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun IconShape.toShape(): Shape = when (this) {
    IconShape.ROUNDED -> RoundedCornerShape(30)
    IconShape.CIRCLE -> CircleShape
    IconShape.COOKIE -> MaterialShapes.Cookie4Sided.toShape()
    IconShape.CLOVER -> MaterialShapes.Clover4Leaf.toShape()
    IconShape.SUNNY -> MaterialShapes.Sunny.toShape()
    IconShape.GEM -> MaterialShapes.Gem.toShape()
    IconShape.PUFFY -> MaterialShapes.Puffy.toShape()
    IconShape.ARCH -> MaterialShapes.Arch.toShape()
}
