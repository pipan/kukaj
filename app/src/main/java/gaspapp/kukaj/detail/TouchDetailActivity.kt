package gaspapp.kukaj.detail

import android.R
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.viewinterop.AndroidView
import gaspapp.kukaj.Repository
import gaspapp.kukaj.Services
import gaspapp.kukaj.error.TouchErrorActivity
import gaspapp.kukaj.model.LiveStream
import gaspapp.kukaj.source.LiveStreamSource
import gaspapp.kukaj.store.Subscription
import gaspapp.kukaj.theme.KukajTheme
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt


class TouchDetailActivity : ComponentActivity() {
    private lateinit var streamSubscription: Subscription<List<LiveStream>>
    private lateinit var streamIntentData: LiveStream
    private val liveStream = mutableStateOf<LiveStream?>(null)
    private val videoLoading = mutableStateOf(true)
    private val loadingError = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        streamSubscription = Repository.getLiveStreamStore().subscribe { value ->
            liveStream.value = value.find { item -> item.detailUrl == streamIntentData.detailUrl }
        }

        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
        this.streamIntentData = intent.getSerializableExtra(DetailsActivity.MOVIE) as LiveStream? ?: return openError()
        val source: LiveStreamSource = Services.getSourceService().getHandler(streamIntentData.detailUrl)
            ?: return openError()
        loadDetail(source)
        val displayMetrics = DisplayMetrics()
        windowManager.getDefaultDisplay().getMetrics(displayMetrics)
        setContent {
            KukajTheme {
                val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = if (isLandscape) Color(0xFF000000) else MaterialTheme.colorScheme.background
                ) {
                    Column {
                        if (loadingError.value) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(color = Color(0xFF000000))
                                    .aspectRatio(16F / 9F)
                            ) {
                                Text(
                                    text = "Chyba pri načítaní videa",
                                    color = Color(0xFFFFFFFF)
                                )
                                Button(
                                    onClick = { loadDetail(source) }
                                ) {
                                    Text(text = "Skúsiť znovu")
                                }
                            }
                        } else if (!videoLoading.value) {
                            if (liveStream.value?.isInMaintenance == true) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(color = Color(0xFF000000))
                                        .aspectRatio(16F / 9F)
                                ) {
                                    Text(
                                        text = "Údržba",
                                        fontSize = 6.em,
                                        color = Color(0xFFFFFFFF)
                                    )
                                    Text(
                                        text = "na kamere prebieha údržba",
                                        color = Color(0xFFFFFFFF)
                                    )
                                }
                            } else {
                                VideoPlayer(
                                    url = liveStream.value?.videoUrl!!,
                                    landscape = isLandscape,
                                    displayMetrics = displayMetrics
                                )
                            }
                        } else {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(color = MaterialTheme.colorScheme.surface)
                                    .aspectRatio(16F / 9F)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.width(64.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.surface,
                                )
                            }
                        }
                        Text(
                            modifier = Modifier.padding(12.dp),
                            text = streamIntentData.title!!,
                            fontSize = 6.em,
                            lineHeight = 1.3.em
                        )
                        Text(
                            modifier = Modifier.padding(12.dp),
                            text = streamIntentData.description!!,
                            fontSize = 3.em
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        streamSubscription.unsubscribe()
    }

    private fun openError() {
        val intent = Intent(applicationContext, TouchErrorActivity::class.java)
        startActivity(intent)
        finishAffinity()
    }

    private fun loadDetail(source: LiveStreamSource) {
        loadingError.value = false
        source.getDetailLoader().load(streamIntentData, { _ ->
            videoLoading.value = false
        }, { error ->
            Log.d("load error", error.toString())
            loadingError.value = true
        })
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun VideoPlayer(
    url: String,
    landscape: Boolean = false,
    displayMetrics: DisplayMetrics
) {
    val zoomState = remember { mutableStateOf(1F) }
    val offsetState = remember { mutableStateOf(Offset.Zero) }
    val fillMode = remember { mutableStateOf("maxSize") }

    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        zoomState.value = max(zoomState.value * zoomChange, 1f)
        val offsetLimitScale = (zoomState.value - 1f) / 2
        offsetState.value = Offset(
            min(
                displayMetrics.widthPixels * offsetLimitScale,
                max(displayMetrics.widthPixels * offsetLimitScale * -1, offsetState.value.x + (offsetChange.x * zoomState.value))
            ),
            min (
                displayMetrics.heightPixels * offsetLimitScale,
                max(displayMetrics.heightPixels * offsetLimitScale * -1, offsetState.value.y + (offsetChange.y * zoomState.value))
            )
        )
    }
    var modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(16F / 9F)
    if (landscape) {
        if (fillMode.value == "maxSize") {
            modifier = Modifier.fillMaxSize()
        }
        modifier = modifier
            .offset {
                IntOffset(
                    offsetState.value.x.roundToInt(),
                    offsetState.value.y.roundToInt()
                )
            }
            .scale(zoomState.value)
            .transformable(transformState)
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        if (zoomState.value != 1F || offsetState.value != Offset.Zero) {
                            zoomState.value = 1F
                            offsetState.value = Offset.Zero
                        } else {
                            fillMode.value = when {
                                fillMode.value == "maxWidth" -> "maxSize"
                                else -> "maxWidth"
                            }
                        }
                    }
                )
            }
    }
    AndroidView(
        modifier = modifier,
        factory = { context ->
            VideoView(context).apply {
                setVideoPath(url)
                setMediaController(null)
                setOnPreparedListener {
                    start()
                }
                setOnErrorListener { _, _, error ->
                    Log.d("videoerror", error.toString())
                    true
                }
            }
        }
    )
}