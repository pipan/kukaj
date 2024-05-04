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

class LiveStreamStore() : Store<List<LiveStream>>(ArrayList()) {
    fun updateItem(liveStream: LiveStream) {
        val newValue = this.getValue().map { item ->
            if (item.detailUrl == liveStream.detailUrl) {
                liveStream
            } else {
                item
            }
        }
        this.update(newValue)
    }

    fun updateList(newValue: List<LiveStream>) {
        this.update(newValue)
    }
}