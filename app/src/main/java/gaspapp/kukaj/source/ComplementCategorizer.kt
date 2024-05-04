package gaspapp.kukaj.source

import gaspapp.kukaj.model.LiveStream

class ComplementCategorizer(var categorizer: Categorizer) : Categorizer {
    override fun matches(liveStream: LiveStream): Boolean {
        return !this.categorizer.matches(liveStream)
    }
}