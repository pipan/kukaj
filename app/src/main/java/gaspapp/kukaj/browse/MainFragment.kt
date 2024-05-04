package gaspapp.kukaj.browse

import android.content.Intent
import android.os.Bundle
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.ListRowPresenter
import androidx.leanback.widget.OnItemViewClickedListener
import androidx.leanback.widget.Presenter
import androidx.leanback.widget.Row
import androidx.leanback.widget.RowPresenter
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import android.util.Log
import android.view.Gravity
import android.widget.Toast
import gaspapp.kukaj.R
import gaspapp.kukaj.Repository
import gaspapp.kukaj.Services
import gaspapp.kukaj.SpinnerFragment
import gaspapp.kukaj.detail.DetailsActivity
import gaspapp.kukaj.model.LiveStream
import gaspapp.kukaj.model.LiveStreamCategory
import gaspapp.kukaj.store.StoreSelector
import java.util.LinkedList

/**
 * Loads a grid of cards with movies to browse.
 */
class MainFragment : BrowseSupportFragment(), StoreSelector<List<LiveStreamCategory>> {
    private val adapter: ArrayObjectAdapter = ArrayObjectAdapter(ListRowPresenter())

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setAdapter(adapter)
        setupUIElements()
        loadRows()
        setupEventListeners()
    }

    override fun onPause() {
        Repository.getLiveStreamCategoryStore().unsubscribe(this)
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        Repository.getLiveStreamCategoryStore().subscribe(this)
    }

    override fun onStoreUpdate(value: List<LiveStreamCategory>) {
//        Log.d()
//        val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
        adapter.setItems(this.generateLeanbackRowList(value), BrowseDiffCallback())
//        adapter.notifyItemRangeChanged(0, adapter.size())

//        adapter = rowsAdapter
        if (selectedPosition == -1) {
            selectedPosition = 0
        }
    }

    private fun setupUIElements() {
        title = getString(R.string.browse_title)
        headersState = HEADERS_DISABLED
        isHeadersTransitionOnBackEnabled = true
        badgeDrawable = ContextCompat.getDrawable(context!!, R.drawable.kukaj_badge)

        // set fastLane (or headers) background color
        brandColor = ContextCompat.getColor(context!!, R.color.default_background)
    }

    private fun loadRows() {
        val spinnerFragment = SpinnerFragment(Gravity.CENTER)
        fragmentManager!!
            .beginTransaction()
            .add(R.id.main_browse_fragment, spinnerFragment)
            .commit()

        Services.getListLoader().load({ _ ->
                fragmentManager!!
                    .beginTransaction()
                    .remove(spinnerFragment)
                    .commit()
            }, { _ ->
                val intent = Intent(context!!, BrowseErrorActivity::class.java)
                startActivity(intent)
            })
    }

    private fun setupEventListeners() {
        onItemViewClickedListener = ItemViewClickedListener()
    }

    private fun generateLeanbackRowList(list: List<LiveStreamCategory>): List<ListRow> {
        val cardPresenter = CardPresenter(context!!.applicationContext.resources.displayMetrics)
        var leanbackRowList: List<ListRow> = LinkedList()

        for (group in list) {
            val header = HeaderItem(group.category.title)
            val listRowAdapter = ArrayObjectAdapter(cardPresenter)
            for (item in group.liveStreamList) {
                listRowAdapter.add(item)
            }
            if (listRowAdapter.size() > 0) {
                leanbackRowList = leanbackRowList + ListRow(header, listRowAdapter)
            }
        }
        return leanbackRowList
    }

    private inner class ItemViewClickedListener : OnItemViewClickedListener {
        override fun onItemClicked(
                itemViewHolder: Presenter.ViewHolder,
                item: Any,
                rowViewHolder: RowPresenter.ViewHolder,
                row: Row) {

            if (item is LiveStream) {
                val intent = Intent(context!!, DetailsActivity::class.java)
                intent.putExtra(DetailsActivity.MOVIE, item)

                val bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        activity!!,
                        (itemViewHolder.view as ImageCardView).mainImageView,
                        DetailsActivity.SHARED_ELEMENT_NAME)
                        .toBundle()
                startActivity(intent, bundle)
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