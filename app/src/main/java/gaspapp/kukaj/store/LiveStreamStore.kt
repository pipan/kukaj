package gaspapp.kukaj.store

import android.util.Log
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response.ErrorListener
import com.android.volley.Response.Listener
import com.android.volley.toolbox.StringRequest
import gaspapp.kukaj.model.LiveStream
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.util.LinkedList

class LiveStreamStore(private val httpQueue: RequestQueue) : Store<List<LiveStream>>(ArrayList()) {
    fun loadList(responseListener: Listener<String>, errorListener: ErrorListener) {
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

                this.update(list)
                responseListener.onResponse(response)
            },
            { error ->
                Log.e("httpError", error.toString())
                errorListener.onErrorResponse(error)
            })
        request.setRetryPolicy(DefaultRetryPolicy(6000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT))
        this.httpQueue.add(request)
    }

    fun loadDetail(liveStream: LiveStream, responseListener: Listener<String>, errorListener: ErrorListener) {
        val request = StringRequest(
            Request.Method.GET, liveStream.detailUrl,
            { response ->
                val document: Document = Jsoup.parse(response.toString())

                liveStream.videoUrl = document.select("body video-js source").attr("src")
                if (liveStream.videoUrl == "") {
                    val maintenanceImage: String = document.select("body .projekt_view .text-center img").attr("src")
                    liveStream.isInMaintenance = (maintenanceImage == "https://www.kukaj.sk/images/udrzba.jpg")
                } else {
                    liveStream.isInMaintenance = false
                }

                val paragraphsElements: Elements = document.select("body .text > .container > .text > p")
                var descriptionParagraphs: List<String> = LinkedList()
                for (element in paragraphsElements) {
                    val pText = element.text()
                    if (pText.equals("")) {
                        continue
                    }
                    descriptionParagraphs = descriptionParagraphs + pText
                }
                liveStream.description = descriptionParagraphs.joinToString(System.lineSeparator() + System.lineSeparator())

                val newValue = this.getValue().map { item ->
                    if (item.id == liveStream.id) {
                        liveStream
                    } else {
                        item
                    }
                }
                this.update(newValue)
                responseListener.onResponse(response)
            },
            errorListener)
        request.setRetryPolicy(DefaultRetryPolicy(6000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT))
        this.httpQueue.add(request)
    }
}