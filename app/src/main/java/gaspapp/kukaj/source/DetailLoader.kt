package gaspapp.kukaj.source

import com.android.volley.Response
import gaspapp.kukaj.model.LiveStream

interface DetailLoader {
    fun load(liveStream: LiveStream, responseListener: Response.Listener<String>, errorListener: Response.ErrorListener)
}