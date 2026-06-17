package gaspapp.kukaj.compose

import android.annotation.SuppressLint
import android.graphics.Rect
import android.util.Log
import android.util.Size
import android.widget.VideoView
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toAndroidRectF
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.graphics.toRect
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun StreamPlayer(
    url: String,
    enableZoom: Boolean = false,
    fullscreen: Boolean = false,
    displaySize: Size,
    inPipMode: Boolean = false,
    onPip: (rect: Rect) -> Unit,
) {
    val videoAspectRatio = 16F / 9F
    val zoomScaleState = remember { mutableFloatStateOf(1F) }
    val zoomOffsetState = remember { mutableStateOf(Offset.Zero) }
    val fillMode = remember { mutableStateOf("maxSize") }
    val videoSize = remember { mutableStateOf(Size(0, 0)) }
    val isDragging = remember { mutableStateOf(false) }
    val dragState = remember { mutableFloatStateOf(0f) }
    val globalPosition = remember { mutableStateOf(Rect()) }

    if (!inPipMode && !isDragging.value) {
        dragState.floatValue = 0f
    }

    var modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(videoAspectRatio)
        .zIndex(2f)

    if (fullscreen && fillMode.value == "maxSize") {
        modifier = Modifier.fillMaxSize()
    }

    if (enableZoom) {
        val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
            zoomScaleState.floatValue = min(4f, max(zoomScaleState.floatValue * zoomChange, 1f))
            val zoomedSize = Size((videoSize.value.width * zoomScaleState.floatValue).roundToInt(), (videoSize.value.height * zoomScaleState.floatValue).roundToInt())
            val changedOffset = Offset(zoomOffsetState.value.x + (offsetChange.x * zoomScaleState.floatValue), zoomOffsetState.value.y + (offsetChange.y * zoomScaleState.floatValue))
            val boundingOffset = Offset(
                max(0F, (zoomedSize.width - displaySize.width).toFloat() / 2),
                max(0F, (zoomedSize.height - displaySize.height).toFloat() / 2)
            )
            zoomOffsetState.value = Offset(
                min(boundingOffset.x, max(boundingOffset.x * -1, changedOffset.x)),
                min(boundingOffset.y, max(boundingOffset.y * -1, changedOffset.y))
            )
        }

        modifier = modifier.graphicsLayer {
                scaleX = zoomScaleState.floatValue
                scaleY = zoomScaleState.floatValue
                translationX = zoomOffsetState.value.x
                translationY = zoomOffsetState.value.y
            }
            .transformable(transformState)
    }

    if (!fullscreen) {
        val revDragValue = 1f - dragState.floatValue
        val scaleValue by derivedStateOf { revDragValue + dragState.floatValue / 2 }
        val translationValue by derivedStateOf {  dragState.floatValue * displaySize.height }
        val animateScale: Float by animateFloatAsState(scaleValue,
            label = "video scale"
        )
        val animateTranslation: Float by animateFloatAsState(translationValue,
            label = "video translation"
        )

        modifier = modifier
            .pointerInput(Unit) {
                detectVerticalDragGestures({ _ ->
                    isDragging.value = true
                }, {
                    if (dragState.floatValue > 0.3f) {
                        onPip(globalPosition.value)
                    } else {
                        dragState.floatValue = 0f
                    }
                    isDragging.value = false
                }, {
                    isDragging.value = false
                }, { change, dragAmount ->
                    change.consume()
                    dragState.floatValue =
                        min(0.7f, max(0f, dragState.floatValue + dragAmount / displaySize.height))
                })
            }
            .graphicsLayer {
                scaleX = if (isDragging.value) scaleValue else animateScale
                scaleY = if (isDragging.value) scaleValue else animateScale
                translationY = if (isDragging.value) (dragState.floatValue * displaySize.height) else animateTranslation
                transformOrigin = TransformOrigin(0.9f, 0.1f)
            }
            .onGloballyPositioned { layoutCoordinates ->
                globalPosition.value = layoutCoordinates
                    .boundsInWindow()
                    .toAndroidRectF()
                    .toRect()
            }
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            VideoView(context).apply {
                setMediaController(null)
                setOnPreparedListener {
                    start()
                }
                addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
                    videoSize.value = Size(width, height)
                }
                setOnErrorListener { _, _, error ->
                    Log.d("videoerror", error.toString())
                    true
                }
            }
        },
        update = {
            it.setVideoPath(url)
        }
    )
}