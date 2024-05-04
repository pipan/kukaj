package gaspapp.kukaj.source

import gaspapp.kukaj.model.LiveStream

interface Categorizer {
    fun matches(liveStream: LiveStream): Boolean
}