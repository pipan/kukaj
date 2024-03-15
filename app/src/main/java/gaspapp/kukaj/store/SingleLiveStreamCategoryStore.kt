package gaspapp.kukaj.store

import gaspapp.kukaj.model.LiveStreamCategory
import java.io.Closeable

class SingleLiveStreamCategoryStore(
    private val liveStreamCategoryStore: LiveStreamCategoryStore,
    private val id: String) : Store<LiveStreamCategory?>(null), StoreSelector<List<LiveStreamCategory>>, Closeable {

    init {
        liveStreamCategoryStore.subscribeAndUpdate(this)
    }

    override fun onStoreUpdate(value: List<LiveStreamCategory>) {
        for (group in value) {
            if (group.category.id == this.id) {
                return this.update(group)
            }
        }
        this.update(null)
    }

    override fun close() {
        this.unsubscribeAll()
        liveStreamCategoryStore.unsubscribe(this)
    }
}