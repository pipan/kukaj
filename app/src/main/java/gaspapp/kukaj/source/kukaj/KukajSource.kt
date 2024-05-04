package gaspapp.kukaj.source.kukaj

import com.android.volley.RequestQueue
import gaspapp.kukaj.source.DetailLoader
import gaspapp.kukaj.source.LiveStreamSource
import gaspapp.kukaj.store.LiveStreamStore

class KukajSource(
    private var liveStreamStore: LiveStreamStore,
    private var httpQueue: RequestQueue
) : LiveStreamSource {
    private var detailLoader = KukajDetailLoader(liveStreamStore, httpQueue)

    override fun getDetailLoader(): DetailLoader {
        return this.detailLoader
    }

    override fun canHandleDetailUrl(detailUrl: String): Boolean {
        return detailUrl.matches(Regex(KukajSource.HOST_REGEXP))
    }

    companion object {
        const val HOST_REGEXP = "^https?://(www.)?kukaj.sk/.*$"

        fun parseId(detailUrl: String): Long {
            return detailUrl.split("/").last().split("-").first().toLong()
        }
    }
}