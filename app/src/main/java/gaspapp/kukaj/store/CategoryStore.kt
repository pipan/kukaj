package gaspapp.kukaj.store

import gaspapp.kukaj.model.CategoryModel

class CategoryStore : Store<List<CategoryModel>>(ArrayList()) {
    init {
        val value: List<CategoryModel> = arrayOf(
            CategoryModel("FEEDER", "Kŕmidla", arrayOf<Long>(313, 314, 302, 312, 277, 254, 286).toList()),
            CategoryModel("STORK", "Bociany", arrayOf<Long>(223, 224, 269, 271, 202, 203, 204, 205, 206, 209, 210, 211, 214, 226, 308, 310, 317, 318, 319, 320, 311).toList()),
            CategoryModel("OWL", "Sovy", arrayOf<Long>(272, 273, 257, 258, 315, 316).toList()),
            CategoryModel("FALCON", "Sokoly", arrayOf<Long>(303, 304, 261, 294, 262, 263, 309, 264, 265, 305, 266, 267, 290, 287, 219, 229, 249, 326).toList()),
            CategoryModel("OTHER", "Ostatné", ArrayList())
        ).toList()
        this.update(value)
    }

    fun findByLiveStreamId(id: Long): CategoryModel? {
        for (category in this.getValue()) {
            if (category.streamIdList.contains(id)) {
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