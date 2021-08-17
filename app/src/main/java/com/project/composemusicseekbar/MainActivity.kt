package com.project.composemusicseekbar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension
import com.project.composemusicseekbar.ui.theme.ComposeMusicSeekbarTheme
import com.project.composemusicseekbar.ui.theme.MusicItemScreenPadding
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeMusicSeekbarTheme {
                var timePosition by remember {
                    mutableStateOf(1092L)
                }
                MusicSeekBar(timePosition = timePosition, duration = 191059) { newPosition ->
                    timePosition = newPosition
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    ComposeMusicSeekbarTheme {
        MusicSeekBar(timePosition = 1092, duration = 191059) {
            /** New position of the song */
        }
    }
}

@Composable
fun MusicSeekBar(
    modifier: Modifier = Modifier,
    timePosition: Long,
    duration: Long,
    pointColor: Color = Color.White,
    progressLineColor: Color = Color.White,
    backgroundLineColor: Color = Color.Gray.copy(alpha = 0.4f),
    strokeWidth: Dp = 8.dp,
    /** step frequency (optimal 1000)*/
    stepFreq: Int = 1000,
    onDragEvent: (position: Long) -> Unit
) {
    var isDragging by remember {
        mutableStateOf(false)
    }

    var curTimePosition by remember {
        mutableStateOf(0L)
    }
    if(!isDragging)
        curTimePosition = timePosition

    var curSongDuration by remember {
        mutableStateOf(0L)
    }
    if(!isDragging)
        curSongDuration = duration

    var intSize by remember {
        mutableStateOf(Size.Zero)
    }

    var progressPosition by remember {
        mutableStateOf(0f)
    }

    if (!isDragging)
        progressPosition =
            progress(
                curSongPosition = curTimePosition,
                curSongDuration = curSongDuration,
                size = intSize.width,
                stepFreq = stepFreq
            )

    val constraintSet = ConstraintSet {
        val textCurSongPos = createRefFor("textCurSongPos")
        val progressIndicator = createRefFor("progressIndicator")
        val textCurSongDuration = createRefFor("textCurSongDuration")

        constrain(textCurSongPos) {
            top.linkTo(progressIndicator.top)
            bottom.linkTo(progressIndicator.bottom)
            start.linkTo(parent.start)
        }
        constrain(progressIndicator) {
            top.linkTo(parent.top)
            bottom.linkTo(parent.bottom)
            start.linkTo(textCurSongPos.end)
            end.linkTo(textCurSongDuration.start)
            width = Dimension.fillToConstraints
        }
        constrain(textCurSongDuration) {
            top.linkTo(progressIndicator.top)
            bottom.linkTo(progressIndicator.bottom)
            end.linkTo(parent.end)
        }
    }
    ConstraintLayout(
        constraintSet = constraintSet,
        modifier = modifier
            .fillMaxWidth()
    ) {

        Text(
            text = SimpleDateFormat(
                "mm:ss",
                Locale.getDefault()
            ).format(
                if (!isDragging) curTimePosition else dragSongPos(
                    position = progressPosition,
                    curSongDuration = curSongDuration,
                    size = intSize.width,
                    stepFreq = stepFreq
                )
            ),
            modifier = Modifier
                .padding(horizontal = MusicItemScreenPadding)
                .layoutId("textCurSongPos")
        )
        Box(modifier = Modifier
            .layoutId("progressIndicator")
            .height(20.dp)
            .draggable(rememberDraggableState { delta ->
                if (delta >= -progressPosition && delta <= intSize.width - progressPosition) {
                    progressPosition += delta
                }
            }, orientation = Orientation.Horizontal, onDragStarted = {
                isDragging = true
            }, onDragStopped = {
                curTimePosition = dragSongPos(
                    position = progressPosition,
                    curSongDuration = curSongDuration,
                    size = intSize.width,
                    stepFreq = stepFreq
                )
                onDragEvent(curTimePosition)
                isDragging = false
            }
            )
        ) {
            Canvas(
                modifier = Modifier.matchParentSize()
            ) {
                intSize = size
                drawLine(
                    color = backgroundLineColor,
                    start = Offset(x = 0f, y = (intSize.height / 2)),
                    end = Offset(x = intSize.width, y = (intSize.height / 2)),
                    strokeWidth = strokeWidth.value,
                    cap = StrokeCap.Round
                )

                drawLine(
                    color = progressLineColor,
                    start = Offset(x = 0f, y = (intSize.height / 2)),
                    end = Offset(x = progressPosition, y = (intSize.height / 2)),
                    strokeWidth = strokeWidth.value,
                    cap = StrokeCap.Round
                )

                drawPoints(
                    points = listOf(Offset(progressPosition, center.y)),
                    pointMode = PointMode.Points,
                    color = pointColor,
                    strokeWidth = (strokeWidth * 2f).toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
        Text(
            text = SimpleDateFormat("mm:ss", Locale.getDefault()).format(curSongDuration),
            modifier = Modifier
                .padding(horizontal = MusicItemScreenPadding)
                .layoutId("textCurSongDuration")
        )
    }
}

private fun dragSongPos(position: Float, curSongDuration: Long, size: Float, stepFreq: Int): Long {
    val percent = (stepFreq * position / size).toInt()
    return curSongDuration * percent / stepFreq
}

private fun progress(
    curSongPosition: Long,
    curSongDuration: Long,
    size: Float,
    stepFreq: Int
): Float {
    val percent = if (curSongDuration != 0L)
        curSongPosition * stepFreq / curSongDuration
    else 0
    return (size * percent / stepFreq)
}