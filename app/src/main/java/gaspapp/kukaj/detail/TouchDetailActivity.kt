package gaspapp.kukaj.detail

import android.app.PictureInPictureParams
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.util.Rational
import android.util.Size
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.core.view.WindowCompat
import gaspapp.kukaj.Repository
import gaspapp.kukaj.Services
import gaspapp.kukaj.compose.StreamPlayer
import gaspapp.kukaj.error.TouchErrorActivity
import gaspapp.kukaj.model.LiveStream
import gaspapp.kukaj.source.LiveStreamSource
import gaspapp.kukaj.store.EmptySubscription
import gaspapp.kukaj.store.Subscription
import gaspapp.kukaj.theme.KukajTheme

class TouchDetailActivity : ComponentActivity() {
    private var streamSubscription: Subscription<List<LiveStream>> = EmptySubscription()
    private var streamIntentData: LiveStream = LiveStream()
    private val liveStream = mutableStateOf<LiveStream?>(null)
    private val videoLoading = mutableStateOf(true)
    private val loadingError = mutableStateOf(false)
    private val inPipMode = mutableStateOf(false)
    private val isLandscapeOrientation = mutableStateOf(false)

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.isLandscapeOrientation.value = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        if (this.isLandscapeOrientation.value) {
            onLandscape()
        }

        initDetail(intent)

        setContent {
            KukajTheme {
                var surfaceModifier = Modifier.fillMaxSize()
                if (!this.isLandscapeOrientation.value) {
                    surfaceModifier = surfaceModifier.statusBarsPadding()
                        .navigationBarsPadding()
                }
                Surface(
                    modifier = surfaceModifier,
                    color = if (this.isLandscapeOrientation.value) Color(0xFF000000) else MaterialTheme.colorScheme.background
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
                                    onClick = { reloadVideo() }
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
                                val displaySize = getDisplaySize() // has to be inside composable, to trigger reCompose on sizeChange
                                StreamPlayer(
                                    url = liveStream.value?.videoUrl!!,
                                    fullscreen = isLandscapeOrientation.value,
                                    enableZoom = isLandscapeOrientation.value,
                                    displaySize = displaySize,
                                    inPipMode = inPipMode.value,
                                    onPip = {rect ->
                                        inPipMode.value = true
                                        enterPictureInPictureMode(
                                            PictureInPictureParams.Builder()
                                                .setAspectRatio(Rational(rect.right - rect.left, rect.bottom - rect.top))
                                                .setSourceRectHint(rect)
                                                .build()
                                        )
                                    }
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
                        if (!inPipMode.value) {
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
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        if (intent != null) {
            initDetail(intent)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        val isNewOrientationLandscape = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE
        if (this.isLandscapeOrientation.value == isNewOrientationLandscape) {
            return
        }
        this.isLandscapeOrientation.value = isNewOrientationLandscape

        if (this.isLandscapeOrientation.value) {
            onLandscape()
        } else {
            onPortrait()
        }
    }

    private fun onLandscape() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        actionBar?.hide()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            window.insetsController?.apply {
                hide(WindowInsets.Type.statusBars())
                systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
    }

    private fun onPortrait() {
        WindowCompat.setDecorFitsSystemWindows(window, true)
        actionBar?.show()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            window.insetsController?.apply {
                show(WindowInsets.Type.statusBars())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    systemBarsBehavior = WindowInsetsController.BEHAVIOR_DEFAULT
                }
            }
        }
    }

    private fun getDisplaySize(): Size {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            val displayBounds = getSystemService(WindowManager::class.java).currentWindowMetrics.bounds
            return Size(displayBounds.width(), displayBounds.height())
        } else {
            val displayMetrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            return Size(displayMetrics.widthPixels, displayMetrics.heightPixels)
        }
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode)
        this.inPipMode.value = isInPictureInPictureMode
    }

    override fun onDestroy() {
        super.onDestroy()
        streamSubscription.unsubscribe()
    }

    private fun initDetail(intent: Intent) {
        val newStream = intent.getSerializableExtra(DetailsActivity.MOVIE) as LiveStream? ?: return openError()
        if (newStream.detailUrl == this.streamIntentData.detailUrl) {
            return
        }
        this.streamIntentData = newStream

        streamSubscription.unsubscribe()
        streamSubscription = Repository.getLiveStreamStore().subscribe { value ->
            liveStream.value = value.find { item -> item.detailUrl == streamIntentData.detailUrl }
        }

        reloadVideo()
    }

    private fun reloadVideo() {
        val source: LiveStreamSource = Services.getSourceService().getHandler(streamIntentData.detailUrl)
            ?: return openError()
        loadDetail(source)
    }

    private fun openError() {
        val intent = Intent(applicationContext, TouchErrorActivity::class.java)
        startActivity(intent)
        finishAffinity()
    }
    
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onUserLeaveHint() {
        enterPictureInPictureMode()
    }

    private fun loadDetail(source: LiveStreamSource) {
        videoLoading.value = true
        loadingError.value = false
        source.getDetailLoader().load(streamIntentData, { _ ->
            videoLoading.value = false
        }, { error ->
            Log.d("load error", error.toString())
            loadingError.value = true
        })
    }
}