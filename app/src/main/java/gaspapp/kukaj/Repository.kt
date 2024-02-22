package gaspapp.kukaj

import android.content.Context
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import gaspapp.kukaj.store.CategoryStore
import gaspapp.kukaj.store.LiveStreamCategoryStore
import gaspapp.kukaj.store.LiveStreamStore

class Repository {
    companion object {
        private lateinit var httpQueue: RequestQueue
        private lateinit var liveStreamStore: LiveStreamStore
        private lateinit var categoryStore: CategoryStore
        private lateinit var liveStreamCategoryStore: LiveStreamCategoryStore

        fun init(context: Context) {
            this.httpQueue = Volley.newRequestQueue(context)
            this.liveStreamStore = LiveStreamStore(this.httpQueue)
            this.categoryStore = CategoryStore()
            this.liveStreamCategoryStore = LiveStreamCategoryStore(this.liveStreamStore, this.categoryStore)
        }

        fun getLiveStreamStore(): LiveStreamStore {
            return this.liveStreamStore
        }

        fun getLiveStreamCategoryStore(): LiveStreamCategoryStore {
            return this.liveStreamCategoryStore
        }

        fun getCategoryStore(): CategoryStore {
            return this.categoryStore
        }
    }
}