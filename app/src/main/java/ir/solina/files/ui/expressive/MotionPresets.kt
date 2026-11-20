package ir.solina.files.ui.expressive

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset

// d3motion-inspired gliding pill, reimplemented natively: the selected
// segment's background slides between segments on the motion scheme's
// spatial spec, so REDUCED motion snaps it instead of animating it.

/**
 * Horizontal offset, in pixels, of the segment at [selectedIndex] from the
 * layout start of its row. [stepPx] is the distance between two adjacent
 * segment starts (segment width + spacing). Mirrored in RTL, where the
 * first segment sits at the right edge and later segments lie to its left.
 */
fun pillOffsetX(selectedIndex: Int, stepPx: Int, rtl: Boolean): Int =
    if (rtl) -selectedIndex * stepPx else selectedIndex * stepPx

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun rememberPillOffset(targetX: Int): State<IntOffset> {
    val motion = MaterialTheme.motionScheme
    return animateIntOffsetAsState(
        targetValue = IntOffset(targetX, 0),
        animationSpec = motion.defaultSpatialSpec(),
        label = "glidingPill",
    )
}

/**
 * The gliding pill itself: a coloured rounded rect placed behind the
 * segments. The caller measures the segment size and drives [offset] with
 * [rememberPillOffset]; the pill is drawn first so the segments sit on top.
 */
@Composable
fun GlidingPill(
    offset: State<IntOffset>,
    width: Dp,
    height: Dp,
    color: Color,
    shape: Shape,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .offset { offset.value }
            .size(width, height)
            .background(color, shape),
    )
}

// Pop-in/out for boolean-flip checkmarks (row checkbox, tile check
// circle): a small scale + fade, driven by the motion scheme so REDUCED
// motion snaps the check in and out.

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun MotionScheme.checkPopEnter(): EnterTransition =
    scaleIn(animationSpec = fastSpatialSpec(), initialScale = 0.6f) +
        fadeIn(animationSpec = fastEffectsSpec())

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun MotionScheme.checkPopExit(): ExitTransition =
    scaleOut(animationSpec = fastSpatialSpec(), targetScale = 0.6f) +
        fadeOut(animationSpec = fastEffectsSpec())

/**
 * Cached [checkPopEnter]/[checkPopExit] (PERF_AUDIT_2 item 7): the builders
 * above allocate fresh Enter/ExitTransition objects on every call, and
 * FileRow called them during every row's composition — pure garbage while
 * scrolling. The transitions only depend on the motion scheme, so they are
 * remembered per scheme and rebuilt only if the scheme itself changes.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun rememberCheckPopEnter(motion: MotionScheme): EnterTransition =
    remember(motion) { motion.checkPopEnter() }

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun rememberCheckPopExit(motion: MotionScheme): ExitTransition =
    remember(motion) { motion.checkPopExit() }
