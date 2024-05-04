package gaspapp.kukaj.source.steppelife

import android.util.Log
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import gaspapp.kukaj.model.LiveStream
import gaspapp.kukaj.source.DetailLoader
import gaspapp.kukaj.store.LiveStreamStore
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.util.LinkedList

class SteppelifeDetailLoader(
    private var liveStreamStore: LiveStreamStore,
    private var httpQueue: RequestQueue
) : DetailLoader {
    override fun load(liveStream: LiveStream, responseListener: Response.Listener<String>, errorListener: Response.ErrorListener) {
        val detailRequest = StringRequest(
            Request.Method.GET, liveStream.detailUrl,
            { detailResponse ->
                val detailDocument: Document = Jsoup.parse(detailResponse.toString())
                val videoPlayerUrl = detailDocument.select(".avs-player iframe").attr("src")

                val playerRequest = StringRequest(
                    Request.Method.GET, videoPlayerUrl,
                    { playerResponse ->
                        val playerDocument: Document = Jsoup.parse(playerResponse.toString())
                        val hls = this.findHls(playerDocument)
                        if (hls == null) {
                            liveStream.isInMaintenance = true
                            // todo: hls not parsed error?
                            return@StringRequest
                        }

                        liveStream.videoUrl = hls
                        liveStream.isInMaintenance = false

                        val paragraphsElements: Elements = detailDocument.select(".sppb-addon-text-block p")
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
                        responseListener.onResponse(playerResponse)
                    }, errorListener)

                playerRequest.setRetryPolicy(DefaultRetryPolicy(6000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT))
                this.httpQueue.add(playerRequest)
            },
            errorListener)
        detailRequest.setRetryPolicy(DefaultRetryPolicy(6000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT))
        this.httpQueue.add(detailRequest)
    }

    private fun findHls(document: Document): String? {
        val scripts = document.select("script")
        for (s in scripts) {
            val lines = s.html().split("\n")
            for (l in lines) {
                val result = Regex("var settings = (?<json>\\{.*\"hls\":\"(.*)\".*\\});?$").find(l)
                if (result != null) {
                    val settingsJson = JSONObject(result.groups["json"]?.value ?: "{}")
                    return settingsJson.getString("hls")
                }
            }
        }
        return null
    }
}