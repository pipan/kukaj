package gaspapp.kukaj.source

import gaspapp.kukaj.model.LiveStream

class UnionCategorizer(var categorizerList: List<Categorizer> = ArrayList()) : Categorizer {
    override fun matches(liveStream: LiveStream): Boolean {
        for (categorizer in this.categorizerList) {
            if (categorizer.matches(liveStream)) {
                return true
            }
        }
        return false
    }
}