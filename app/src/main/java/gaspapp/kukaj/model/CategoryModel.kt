package gaspapp.kukaj.model

class CategoryModel(
    var id: String,
    var title: String? = null,
    var streamIdList: List<Long> = ArrayList()
) {}