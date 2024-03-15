package gaspapp.kukaj.store

import gaspapp.kukaj.model.CategoryModel
import gaspapp.kukaj.model.LiveStream
import gaspapp.kukaj.model.LiveStreamCategory
import java.util.LinkedList

class LiveStreamCategoryStore(
    private val liveStreamStore: LiveStreamStore,
    private val categoryStore: CategoryStore) : Store<List<LiveStreamCategory>>(ArrayList()) {

    private var liveStreamList: List<LiveStream>? = null
    private var categoryList: List<CategoryModel>? = null

    init {
        liveStreamStore.subscribeAndUpdate { liveStreamList -> setLiveStream(liveStreamList) }
        categoryStore.subscribeAndUpdate { categoryList -> setCategory(categoryList) }
    }

    private fun setLiveStream(list: List<LiveStream>) {
        this.liveStreamList = list
        this.mapStores()
    }
    private fun setCategory(list: List<CategoryModel>) {
        this.categoryList = list
        this.mapStores()
    }

    private fun mapStores() {
        if (this.liveStreamList == null || this.categoryList == null) {
            return
        }
        var liveStreamCategoryList: List<LiveStreamCategory> = ArrayList()
        var inCategory: List<Long> = LinkedList()
        var otherCategory: CategoryModel? = null
        for (category in this.categoryList!!) {
            if (category.id == "OTHER") {
                otherCategory = category
            }
            inCategory = inCategory + category.streamIdList

            var items: List<LiveStream> = ArrayList()
            for (item in this.liveStreamList!!) {
                if (!category.streamIdList.contains(item.id)) {
                    continue
                }
                items = items + item
            }
            if (items.isNotEmpty()) {
                liveStreamCategoryList = liveStreamCategoryList + LiveStreamCategory(category, items)
            }
        }

        var items: List<LiveStream> = ArrayList()
        for (item in this.liveStreamList!!) {
            if (inCategory.contains(item.id)) {
                continue
            }
            items = items + item
        }
        if (items.isNotEmpty() && otherCategory != null) {
            liveStreamCategoryList = liveStreamCategoryList + LiveStreamCategory(otherCategory, items)
        }
        this.update(liveStreamCategoryList)
    }
}