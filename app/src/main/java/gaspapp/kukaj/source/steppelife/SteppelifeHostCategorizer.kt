package gaspapp.kukaj.source.steppelife

import gaspapp.kukaj.model.LiveStream
import gaspapp.kukaj.source.Categorizer

class SteppelifeHostCategorizer : Categorizer {
    override fun matches(liveStream: LiveStream): Boolean {
        return liveStream.detailUrl.matches(Regex(SteppelifeSource.HOST_REGEXP))
    }
}