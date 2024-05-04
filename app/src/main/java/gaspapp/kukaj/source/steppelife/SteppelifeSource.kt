package gaspapp.kukaj.source.steppelife

import com.android.volley.RequestQueue
import gaspapp.kukaj.source.DetailLoader
import gaspapp.kukaj.source.LiveStreamSource
import gaspapp.kukaj.store.LiveStreamStore

class SteppelifeSource(
    private var liveStreamStore: LiveStreamStore,
    private var httpQueue: RequestQueue
) : LiveStreamSource {
    private var detailLoader = SteppelifeDetailLoader(liveStreamStore, httpQueue)

    override fun canHandleDetailUrl(detailUrl: String): Boolean {
        return detailUrl.matches(Regex(SteppelifeSource.HOST_REGEXP))
    }

    override fun getDetailLoader(): DetailLoader {
        return this.detailLoader
    }

    companion object {
        const val HOST_REGEXP = "^https?://(www.)?steppelife.eu/.*$"
    }
}