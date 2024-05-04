package gaspapp.kukaj.store

import gaspapp.kukaj.model.CategoryModel
import gaspapp.kukaj.model.LiveStream
import gaspapp.kukaj.source.kukaj.KukajIdCategorizer
import gaspapp.kukaj.source.kukaj.KukajOtherCategorizer
import gaspapp.kukaj.source.steppelife.SteppelifeHostCategorizer

class CategoryStore : Store<List<CategoryModel>>(ArrayList()) {
    init {
        var feederCategorizer = KukajIdCategorizer(arrayOf<Long>(313, 314, 302, 312, 277, 254, 286).toList())
        var storkCategorizer = KukajIdCategorizer(arrayOf<Long>(223, 224, 269, 271, 202, 203, 204, 205, 206, 209, 210, 211, 214, 226, 308, 310, 317, 318, 319, 320, 311).toList())
        var owlCategorizer = KukajIdCategorizer(arrayOf<Long>(272, 273, 257, 258, 315, 316).toList())
        var falconCategorizer = KukajIdCategorizer(arrayOf<Long>(303, 304, 261, 294, 262, 263, 309, 264, 265, 305, 266, 267, 290, 287, 219, 229, 249, 326).toList())
        var steppelifeHostCategorizer = SteppelifeHostCategorizer()
        var otherCategorizer = KukajOtherCategorizer(arrayOf(feederCategorizer, storkCategorizer, owlCategorizer, falconCategorizer).toList())


        val value: List<CategoryModel> = arrayOf(
            CategoryModel("FEEDER", "Kŕmidla", feederCategorizer),
            CategoryModel("STORK", "Bociany", storkCategorizer),
            CategoryModel("OWL", "Sovy", owlCategorizer),
            CategoryModel("FALCON", "Sokoly", falconCategorizer),
            CategoryModel("STEPPELIFE", "Steppelife", steppelifeHostCategorizer),
            CategoryModel("OTHER", "Ostatné", otherCategorizer)
        ).toList()
        this.update(value)
    }

    fun findByLiveStream(liveStream: LiveStream): CategoryModel? {
        for (category in this.getValue()) {
            if (category.categorizer.matches(liveStream)) {
                return category
            }
        }
        return this.findById("OTHER")
    }

    private fun findById(id: String): CategoryModel? {
        for (category in this.getValue()) {
            if (category.id == id) {
                return category
            }
        }
        return null
    }
}