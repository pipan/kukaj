package gaspapp.kukaj.playback

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityOptionsCompat
import androidx.leanback.app.VideoSupportFragment
import androidx.leanback.app.VideoSupportFragmentGlueHost
import androidx.leanback.media.MediaPlayerAdapter
import androidx.leanback.media.PlaybackTransportControlGlue
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.ClassPresenterSelector
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.ListRowPresenter
import androidx.leanback.widget.OnItemViewClickedListener
import androidx.leanback.widget.PlaybackControlsRow
import androidx.leanback.widget.Presenter
import androidx.leanback.widget.Row
import androidx.leanback.widget.RowPresenter
import gaspapp.kukaj.R
import gaspapp.kukaj.Repository
import gaspapp.kukaj.browse.BrowseErrorActivity
import gaspapp.kukaj.browse.CardPresenter
import gaspapp.kukaj.detail.DetailsActivity
import gaspapp.kukaj.model.CategoryModel
import gaspapp.kukaj.model.LiveStream
import gaspapp.kukaj.store.SingleLiveStreamCateogryStore


/** Handles video playback with media controls. */
class PlaybackVideoFragment : VideoSupportFragment() {

    private lateinit var mTransportControlGlue: PlaybackTransportControlGlue<MediaPlayerAdapter>
    private var singleLiveStreamCategoryStore: SingleLiveStreamCateogryStore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val liveStream = activity?.intent?.getSerializableExtra(DetailsActivity.MOVIE) as LiveStream

        val glueHost = VideoSupportFragmentGlueHost(this@PlaybackVideoFragment)
        val playerAdapter = MediaPlayerAdapter(context)
        playerAdapter.setRepeatAction(PlaybackControlsRow.RepeatAction.INDEX_NONE)

        mTransportControlGlue = PlaybackTransportControlGlue(activity, playerAdapter)
        mTransportControlGlue.host = glueHost
        mTransportControlGlue.title = liveStream.title
        mTransportControlGlue.subtitle = liveStream.description
        mTransportControlGlue.playWhenPrepared()

        try {
            playerAdapter.setDataSource(Uri.parse(liveStream.videoUrl))

            val category: CategoryModel? = Repository.getCategoryStore().fingByLiveStreamId(liveStream.id)
            if (category != null) {
                this.singleLiveStreamCategoryStore = SingleLiveStreamCateogryStore(Repository.getLiveStreamCategoryStore(), category.id)
                this.singleLiveStreamCategoryStore!!.subscribeAndUpdate { liveStreamCategory ->
                    Log.d("category", liveStreamCategory!!.category.title.toString())
                    val presenterSelector = ClassPresenterSelector()
                    presenterSelector.addClassPresenter(
                        mTransportControlGlue.controlsRow.javaClass,
                        mTransportControlGlue.playbackRowPresenter
                    )
                    presenterSelector.addClassPresenter(ListRow::class.java, ListRowPresenter())
                    val rowsAdapter = ArrayObjectAdapter(presenterSelector)
                    rowsAdapter.add(mTransportControlGlue.controlsRow)

                    val cardPresenter = CardPresenter(context!!.applicationContext.resources.displayMetrics)
                    val listRowAdapter = ArrayObjectAdapter(cardPresenter)
                    val header = HeaderItem(liveStreamCategory.category.title)
                    for (item in liveStreamCategory.liveStreamList) {
                        listRowAdapter.add(item)
                    }
                    if (listRowAdapter.size() > 0) {
                        rowsAdapter.add(ListRow(header, listRowAdapter))
                        setOnItemViewClickedListener(ItemViewClickedListener())
                    }
                    adapter = rowsAdapter
                }
            }
        } catch (ex: Exception) {
            Log.e("Playback", ex.toString())
            Toast.makeText(context, "Nepodarilo sa načítať video", Toast.LENGTH_LONG).show()
        }
    }

    override fun onPause() {
        super.onPause()
        mTransportControlGlue.pause()
    }

    override fun onResume() {
        super.onResume()
        mTransportControlGlue.play()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View? = super.onCreateView(inflater, container, savedInstanceState)
        view!!.keepScreenOn = true
        return view
    }

    override fun onDestroyView() {
        view!!.keepScreenOn = false
        this.singleLiveStreamCategoryStore!!.unsubscribeAll()
        super.onDestroyView()
    }

    private inner class ItemViewClickedListener : OnItemViewClickedListener {
        override fun onItemClicked(
            itemViewHolder: Presenter.ViewHolder,
            item: Any,
            rowViewHolder: RowPresenter.ViewHolder,
            row: Row
        ) {

            if (item is LiveStream) {
                val intent = Intent(context!!, DetailsActivity::class.java)
                intent.putExtra(DetailsActivity.MOVIE, item)

                val bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    activity!!,
                    (itemViewHolder.view as ImageCardView).mainImageView,
                    DetailsActivity.SHARED_ELEMENT_NAME)
                    .toBundle()
                startActivity(intent, bundle)
                activity!!.finish()
            } else if (item is String) {
                if (item.contains(getString(R.string.error_fragment))) {
                    val intent = Intent(context!!, BrowseErrorActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(context!!, item, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}