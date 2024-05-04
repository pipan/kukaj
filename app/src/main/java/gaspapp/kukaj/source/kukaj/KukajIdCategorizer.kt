package gaspapp.kukaj.source.kukaj

import android.util.Log
import gaspapp.kukaj.model.LiveStream
import gaspapp.kukaj.source.Categorizer

class KukajIdCategorizer(var streamIdList: List<Long> = ArrayList()) : Categorizer {
    override fun matches(liveStream: LiveStream): Boolean {
        if (!liveStream.detailUrl.matches(Regex(KukajSource.HOST_REGEXP))) {
            return false
        }
        val id = KukajSource.parseId(liveStream.detailUrl)
        if (id == 0.toLong()) {
            return false
        }
        return this.streamIdList.contains(id)
    }
}