package ir.solina.files.feature.explorer.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.solina.files.R
import ir.solina.files.feature.explorer.TaskState
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TaskOverlay(task: TaskState) {
    val motion = MaterialTheme.motionScheme
    AlertDialog(
        onDismissRequest = {  },
        confirmButton = {},
        title = { Text(task.title) },
        text = {
            Column(Modifier.fillMaxWidth()) {
                if (task.detail.isNotEmpty()) {
                    Text(
                        task.detail,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 12.dp),
                    )
                }
                // The progress area morphs into a checkmark + "Done" when
                // the task finishes, instead of vanishing at once.
                AnimatedContent(
                    targetState = task.done,
                    transitionSpec = {
                        (fadeIn(motion.fastEffectsSpec()) + scaleIn(motion.fastSpatialSpec(), initialScale = 0.8f))
                            .togetherWith(fadeOut(motion.fastEffectsSpec()) + scaleOut(motion.fastSpatialSpec(), targetScale = 0.8f))
                    },
                    label = "taskDone",
                ) { done ->
                    if (done) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Rounded.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp),
                            )
                            Text(
                                stringResource(R.string.common_done),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 8.dp),
                            )
                        }
                    } else if (task.indeterminate) {
                        LinearProgressIndicator(Modifier.fillMaxWidth())
                    } else {
                        GradientProgressBar(task.fraction)
                    }
                }
            }
        },
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun GradientProgressBar(fraction: Float) {
    val motion = MaterialTheme.motionScheme
    // The fraction eases towards each sampled value on the spatial spec,
    // so the bar glides instead of jumping between progress ticks; the
    // percentage rolls along with it.
    val animated by animateFloatAsState(fraction.coerceIn(0f, 1f), motion.defaultSpatialSpec(), label = "taskFraction")
    val colors = MaterialTheme.colorScheme
    val gradient = remember(colors.primary, colors.tertiary) {
        Brush.horizontalGradient(listOf(colors.primary, colors.tertiary))
    }
    Column(verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(99.dp))
                .background(colors.surfaceContainerHighest),
        ) {
            Box(
                Modifier
                    .fillMaxWidth(animated)
                    .height(8.dp)
                    .background(gradient, RoundedCornerShape(99.dp)),
            )
        }
        Text(
            "${(animated * 100).roundToInt()}%",
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant,
        )
    }
}
