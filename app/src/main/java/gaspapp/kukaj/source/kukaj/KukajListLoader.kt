package gaspapp.kukaj.source.kukaj

import android.util.Log
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import gaspapp.kukaj.model.LiveStream
import gaspapp.kukaj.store.LiveStreamStore
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements

class KukajListLoader(
    private var liveStreamStore: LiveStreamStore,
    private var httpQueue: RequestQueue
) {
    fun load(responseListener: Response.Listener<String>, errorListener: Response.ErrorListener) {
        val request = StringRequest(
            Request.Method.GET, "https://www.kukaj.sk/",
            { response ->
                val document: Document = Jsoup.parse(response.toString())
                val items: Elements = document.select("body > .container .row > div")
                var list: List<LiveStream> = ArrayList<LiveStream>()
                for (item in items) {
                    val liveStreamItem = LiveStream.fromHtmlElement(item) ?: continue
                    list = list + liveStreamItem
                }

                this.liveStreamStore.updateList(list)
                responseListener.onResponse(response)
            },
            { error ->
                Log.e("httpError", error.toString())
                errorListener.onErrorResponse(error)
            })
        request.setRetryPolicy(DefaultRetryPolicy(6000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT))
        this.httpQueue.add(request)
    }
}