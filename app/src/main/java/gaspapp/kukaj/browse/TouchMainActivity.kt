package gaspapp.kukaj.browse

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import gaspapp.kukaj.R
import gaspapp.kukaj.Repository
import gaspapp.kukaj.Services
import gaspapp.kukaj.detail.DetailsActivity
import gaspapp.kukaj.detail.TouchDetailActivity
import gaspapp.kukaj.theme.KukajTheme
import gaspapp.kukaj.model.LiveStream
import gaspapp.kukaj.model.LiveStreamCategory
import gaspapp.kukaj.store.Subscription

class TouchMainActivity : ComponentActivity() {
    private val categoryList = mutableStateListOf<LiveStreamCategory>()
    private val streamList = mutableStateListOf<LiveStream>()
    private var streamListAll: List<LiveStream> = ArrayList()
    private val selectedFilter = mutableStateOf<String>("")
    private val loading = mutableStateOf(true)
    private val hasError = mutableStateOf(false)

    private lateinit var categorySubscription: Subscription<List<LiveStreamCategory>>
    private lateinit var streamSubscription: Subscription<List<LiveStream>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            Repository.init()
            Services.init(applicationContext)
        }
        this.categorySubscription = Repository.getLiveStreamCategoryStore().subscribe { value ->
            categoryList.clear();
            categoryList.addAll(value);
        }
        this.streamSubscription = Repository.getLiveStreamStore().subscribe { value ->
            streamListAll = value
            streamList.clear();
            if (selectedFilter.value == "") {
                streamList.addAll(value);
            } else {
                val category = categoryList.find { item -> item.category.id == selectedFilter.value }
                if (category != null) {
                    streamList.addAll(category.liveStreamList)
                }
            }
        }

        Repository.getLiveStreamCategoryStore().loadCategories(applicationContext)
        loadList()
        setContent {
            KukajTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DisplayList()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        this.streamSubscription.unsubscribe()
        this.categorySubscription.unsubscribe()
    }

    private fun loadList() {
        hasError.value = false
        Services.getListLoader().load({ _ ->
            loading.value = false
        }, { _ ->
            hasError.value = true
        })
    }

    private fun openWeb() {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://kukaj.sk"))
        startActivity(browserIntent)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun DisplayList() {
        val selectedFilterState = remember { selectedFilter }
        val categoryListState = remember { this.categoryList }
        val streamListState = remember { this.streamList }
        val loadingState = remember { this.loading }

        Box() {
            Column {
                Image(
                    painter = painterResource(id = R.drawable.kukaj_badge),
                    contentDescription = "logo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.width(180.dp)
                        .padding(12.dp)
                        .clickable { openWeb() }
                )
                LazyRow(
                    modifier = Modifier.fillMaxWidth()
                        .padding(start = 12.dp, end = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    state = rememberLazyListState(),
                ) {
                    itemsIndexed(categoryListState) { index, item ->
                        FilterChip(
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            onClick = {
                                selectedFilterState.value = if (selectedFilterState.value == item.category.id) "" else item.category.id
                                streamListState.clear()
                                if (selectedFilterState.value == "") {
                                    streamListState.addAll(streamListAll)
                                } else {
                                    val category = categoryList.find { item -> item.category.id == selectedFilter.value }
                                    if (category != null) {
                                        streamListState.addAll(category.liveStreamList)
                                    }
                                }
                            },
                            selected = item.category.id == selectedFilter.value,
                            label = { Text(item.category.title!!) },
                        )
                    }
                }
                if (loadingState.value) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                            .aspectRatio(16F / 9F)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.width(64.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surface,
                        )
                    }
                }
                if (hasError.value) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                            .aspectRatio(16F / 9F)
                    ) {
                        Text(text = "Chyba pri načítaní zoznamu")
                        Button(
                            onClick = { loadList() }
                        ) {
                            Text(text = "Skúsiť znovu")
                        }
                    }
                }
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    state = rememberLazyListState(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(streamListState) { index, item ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                val intent = Intent(applicationContext, TouchDetailActivity::class.java)
                                intent.putExtra(DetailsActivity.MOVIE, item)
                                startActivity(intent)
                            }
                        ) {
                            AsyncImage(
                                source = item.backgroundImageUrl!!,
                                description = item.title!!,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .aspectRatio(16F / 9F)
                            )
                            Text(
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(12.dp),
                                text = item.title!!,
                                fontSize = 4.em,
                                lineHeight = 1.3.em
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun AsyncImage(
        source: String,
        description: String,
        modifier: Modifier
    ) {
        val resource = remember { mutableStateOf<ImageBitmap?>(null) }

        Glide.with(this)
            .asBitmap()
            .load(source)
            .diskCacheStrategy(DiskCacheStrategy.DATA)
            .into(object: CustomTarget<Bitmap> () {
                override fun onLoadCleared(placeholder: Drawable?) {
                    resource.value = null
                }

                override fun onResourceReady(
                    bitmapResource: Bitmap,
                    transition: Transition<in Bitmap>?
                ) {
                    resource.value = bitmapResource.asImageBitmap()
                }
            })
        if (resource.value == null) {
            return
        }
        Image(
            bitmap = resource.value!!,
            contentDescription = description,
            contentScale = ContentScale.FillWidth,
            modifier = modifier
        )
    }
}


