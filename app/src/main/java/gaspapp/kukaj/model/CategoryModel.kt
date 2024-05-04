package gaspapp.kukaj.model

import gaspapp.kukaj.source.Categorizer

class CategoryModel(
    var id: String,
    var title: String? = null,
    var categorizer: Categorizer
) {}