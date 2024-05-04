package gaspapp.kukaj.source

interface LiveStreamSource {
    fun canHandleDetailUrl(detailUrl: String): Boolean
    fun getDetailLoader(): DetailLoader
}