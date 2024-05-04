package gaspapp.kukaj.source

import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import gaspapp.kukaj.model.LiveStream

interface DetailLoader {
    fun load(liveStream: LiveStream, responseListener: Response.Listener<String>, errorListener: Response.ErrorListener)
}