package gaspapp.kukaj.detail

import android.util.Log
import androidx.leanback.widget.AbstractDetailsDescriptionPresenter
import androidx.leanback.widget.Presenter
import gaspapp.kukaj.model.LiveStream

class DetailsDescriptionPresenter : AbstractDetailsDescriptionPresenter() {
    override fun onBindDescription(
            viewHolder: AbstractDetailsDescriptionPresenter.ViewHolder,
            item: Any) {
        val liveStream = item as LiveStream
        viewHolder.title.text = liveStream.title
        viewHolder.body.text = liveStream.description
    }
}