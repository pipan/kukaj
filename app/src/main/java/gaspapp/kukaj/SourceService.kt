package gaspapp.kukaj

import gaspapp.kukaj.source.LiveStreamSource

class SourceService(val sourceList: List<LiveStreamSource> = ArrayList()) {
    fun getHandler(detailUrl: String): LiveStreamSource? {
        for (source in this.sourceList) {
            if (source.canHandleDetailUrl(detailUrl)) {
                return  source
            }
        }
        return null
    }
}