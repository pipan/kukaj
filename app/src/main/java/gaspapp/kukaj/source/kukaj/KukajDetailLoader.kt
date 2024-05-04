package gaspapp.kukaj.source.kukaj

import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import gaspapp.kukaj.model.LiveStream
import gaspapp.kukaj.source.DetailLoader
import gaspapp.kukaj.store.LiveStreamStore
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.util.LinkedList

class KukajDetailLoader (
    private var liveStreamStore: LiveStreamStore,
    private var httpQueue: RequestQueue
) : DetailLoader {
    override fun load(liveStream: LiveStream, responseListener: Response.Listener<String>, errorListener: Response.ErrorListener) {
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

                this.liveStreamStore.updateItem(liveStream)
                responseListener.onResponse(response)
            },
            errorListener)
        request.setRetryPolicy(DefaultRetryPolicy(6000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT))
        this.httpQueue.add(request)
    }
}