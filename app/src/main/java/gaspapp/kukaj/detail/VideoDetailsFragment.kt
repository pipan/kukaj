package gaspapp.kukaj.detail

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.leanback.app.DetailsSupportFragment
import androidx.leanback.app.DetailsSupportFragmentBackgroundController
import androidx.leanback.widget.AbstractDetailsDescriptionPresenter
import androidx.leanback.widget.Action
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.ClassPresenterSelector
import androidx.leanback.widget.DetailsOverviewRow
import androidx.leanback.widget.FullWidthDetailsOverviewRowPresenter
import androidx.leanback.widget.FullWidthDetailsOverviewSharedElementHelper
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.OnActionClickedListener
import androidx.leanback.widget.OnItemViewClickedListener
import androidx.leanback.widget.Presenter
import androidx.leanback.widget.Row
import androidx.leanback.widget.RowPresenter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import gaspapp.kukaj.browse.MainActivity
import gaspapp.kukaj.playback.PlaybackActivity
import gaspapp.kukaj.R
import gaspapp.kukaj.Repository
import gaspapp.kukaj.Services
import gaspapp.kukaj.SpinnerFragment
import gaspapp.kukaj.model.LiveStream
import gaspapp.kukaj.source.LiveStreamSource
import gaspapp.kukaj.store.StoreSelector
import jp.wasabeef.glide.transformations.MaskTransformation
import kotlin.math.roundToInt


/**
 * A wrapper fragment for leanback details screens.
 * It shows a detailed view of video and its metadata plus related videos.
 */
class VideoDetailsFragment : DetailsSupportFragment(), StoreSelector<List<LiveStream>> {

    private var mSelectedStream: LiveStream? = null

    private lateinit var mDetailsBackground: DetailsSupportFragmentBackgroundController
    private lateinit var mPresenterSelector: ClassPresenterSelector
    private lateinit var mAdapter: ArrayObjectAdapter
    private var actionAdapter: ArrayObjectAdapter = ArrayObjectAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mDetailsBackground = DetailsSupportFragmentBackgroundController(this)
        val liveStream = activity!!.intent.getSerializableExtra(DetailsActivity.MOVIE) as LiveStream?

        if (liveStream != null) {
            onItemViewClickedListener = ItemViewClickedListener()
            val descriptionPresenter = DetailsDescriptionPresenter()
            mPresenterSelector = ClassPresenterSelector()
            mAdapter = ArrayObjectAdapter(mPresenterSelector)
            setupDetailsOverviewRowPresenter(descriptionPresenter)
            adapter = mAdapter

            this.setSelectedStream(liveStream)
            val spinnerFragment = SpinnerFragment(Gravity.BOTTOM.xor(Gravity.CENTER_HORIZONTAL))
            fragmentManager!!
                .beginTransaction()
                .add(R.id.details_fragment, spinnerFragment)
                .commit()
            val source: LiveStreamSource? = Services.getSourceService().getHandler(liveStream.detailUrl)
            if (source != null) {
                source.getDetailLoader().load(liveStream, { _ ->
                    fragmentManager!!
                        .beginTransaction()
                        .remove(spinnerFragment)
                        .commit()
                    }, { errorResponse ->
                        Log.e("http", errorResponse.toString())
                        val intent = Intent(context!!, DetailErrorActivity::class.java)
                        startActivity(intent)
                    })
            } else {
                // todo: what to do if no source is found? snackbar? error? log?
            }
        } else {
            val intent = Intent(context!!, MainActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onPause() {
        Repository.getLiveStreamStore().unsubscribe(this)
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        Repository.getLiveStreamStore().subscribe(this)
    }

    override fun onStoreUpdate(value: List<LiveStream>) {
        val liveStream = value.find { i -> i.detailUrl == mSelectedStream!!.detailUrl }
        this.setSelectedStream(liveStream)
    }

    private fun setSelectedStream(liveStream: LiveStream?) {
        mSelectedStream = liveStream
        if (mSelectedStream == null) {
            val intent = Intent(context!!, DetailErrorActivity::class.java)
            startActivity(intent)
            return
        }

        setupDetailsOverviewRow(mSelectedStream!!)
        initializeBackground(mSelectedStream)

        this.actionAdapter.clear()
        if (mSelectedStream!!.isInMaintenance) {
            this.actionAdapter.add(Action(ACTION_MAINTENANCE, resources.getString(R.string.maintenance), "", ContextCompat.getDrawable(context!!, R.drawable.block)))
            adapter.notifyItemRangeChanged(0, adapter.size())
            return
        }

        this.actionAdapter.add(Action(ACTION_WATCH, resources.getString(R.string.watch), "", ContextCompat.getDrawable(context!!, R.drawable.play_arrow)))
    }

    private fun initializeBackground(liveStream: LiveStream?) {
        mDetailsBackground.enableParallax()
        Glide.with(context!!)
                .asBitmap()
                .apply(
                    RequestOptions.bitmapTransform(MultiTransformation<Bitmap>(
                        CenterCrop(),
                        MaskTransformation(R.drawable.gradient_mask)
                    )
                ))
                .load(liveStream?.backgroundImageUrl)
                .transform()
                .error(R.drawable.default_background)
                .into<SimpleTarget<Bitmap>>(object : SimpleTarget<Bitmap>() {
                    override fun onResourceReady(bitmap: Bitmap, transition: Transition<in Bitmap>?) {
                        mDetailsBackground.coverBitmap = bitmap
                        mAdapter.notifyArrayItemRangeChanged(0, mAdapter.size())
                    }
                })
    }

    private fun setupDetailsOverviewRow(liveStream: LiveStream) {
        val row = DetailsOverviewRow(liveStream)
        row.imageDrawable = ContextCompat.getDrawable(context!!, R.drawable.default_background)
        val width = convertDpToPixel(context!!, DETAIL_THUMB_WIDTH)
        val height = convertDpToPixel(context!!, DETAIL_THUMB_HEIGHT)
        Glide.with(context!!)
                .load(liveStream.cardImageUrl)
                .centerCrop()
                .error(R.drawable.default_background)
                .into<SimpleTarget<Drawable>>(object : SimpleTarget<Drawable>(width, height) {
                    override fun onResourceReady(drawable: Drawable, transition: Transition<in Drawable>?) {
                        row.imageDrawable = drawable
                        mAdapter.notifyArrayItemRangeChanged(0, mAdapter.size())
                    }
                })

        row.actionsAdapter = this.actionAdapter
        mAdapter.add(row)
    }

    private fun setupDetailsOverviewRowPresenter(descriptionPresenter: AbstractDetailsDescriptionPresenter) {
        // Set detail background.
        val detailsPresenter = FullWidthDetailsOverviewRowPresenter(descriptionPresenter)
        detailsPresenter.backgroundColor = ContextCompat.getColor(context!!,
            R.color.default_background
        )
        detailsPresenter.actionsBackgroundColor = ContextCompat.getColor(context!!,
            R.color.selected_background
        )

        // Hook up transition element.
        val sharedElementHelper = FullWidthDetailsOverviewSharedElementHelper()
        sharedElementHelper.setSharedElementEnterTransition(
                activity, DetailsActivity.SHARED_ELEMENT_NAME
        )
        detailsPresenter.setListener(sharedElementHelper)
        detailsPresenter.isParticipatingEntranceTransition = true

        detailsPresenter.onActionClickedListener = OnActionClickedListener { action ->
            if (action.id == ACTION_WATCH) {
                if (mSelectedStream!!.videoUrl == "") {
                    val intent = Intent(context!!, DetailErrorActivity::class.java)
                    startActivity(intent)
                    return@OnActionClickedListener
                }
                val intent = Intent(context!!, PlaybackActivity::class.java)
                intent.putExtra(DetailsActivity.MOVIE, mSelectedStream)
                startActivity(intent)
            }
        }
        mPresenterSelector.addClassPresenter(DetailsOverviewRow::class.java, detailsPresenter)
    }

    private fun convertDpToPixel(context: Context, dp: Int): Int {
        val density = context.applicationContext.resources.displayMetrics.density
        return (dp.toFloat() * density).roundToInt()
    }

    private inner class ItemViewClickedListener : OnItemViewClickedListener {
        override fun onItemClicked(
                itemViewHolder: Presenter.ViewHolder?,
                item: Any?,
                rowViewHolder: RowPresenter.ViewHolder,
                row: Row) {
            if (item is LiveStream) {
                val intent = Intent(context!!, DetailsActivity::class.java)
                intent.putExtra(resources.getString(R.string.movie), mSelectedStream)

                val bundle =
                        ActivityOptionsCompat.makeSceneTransitionAnimation(
                                activity!!,
                                (itemViewHolder?.view as ImageCardView).mainImageView,
                            DetailsActivity.SHARED_ELEMENT_NAME
                        )
                                .toBundle()
                startActivity(intent, bundle)
            }
        }
    }

    companion object {
        private const val ACTION_WATCH = 1L
        private const val ACTION_MAINTENANCE = 2L

        private const val DETAIL_THUMB_WIDTH = 500
        private const val DETAIL_THUMB_HEIGHT = 333
    }
}