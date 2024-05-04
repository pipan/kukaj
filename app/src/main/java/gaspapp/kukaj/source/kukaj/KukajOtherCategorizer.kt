package gaspapp.kukaj.source.kukaj

import gaspapp.kukaj.model.LiveStream
import gaspapp.kukaj.source.Categorizer
import gaspapp.kukaj.source.ComplementCategorizer
import gaspapp.kukaj.source.UnionCategorizer

class KukajOtherCategorizer(var categorizerList: List<Categorizer> = ArrayList()) : Categorizer {
    private var internalCategorizer: Categorizer = ComplementCategorizer(UnionCategorizer(this.categorizerList))

    override fun matches(liveStream: LiveStream): Boolean {
        if (!liveStream.detailUrl.matches(Regex(KukajSource.HOST_REGEXP))) {
            return false
        }
        return this.internalCategorizer.matches(liveStream)
    }
}