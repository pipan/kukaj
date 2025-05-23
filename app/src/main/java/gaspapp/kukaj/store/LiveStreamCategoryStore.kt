package gaspapp.kukaj.store

import android.content.Context
import android.util.Log
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import gaspapp.kukaj.R
import gaspapp.kukaj.Repository
import gaspapp.kukaj.Services
import gaspapp.kukaj.browse.MainActivity
import gaspapp.kukaj.model.CategoryModel
import gaspapp.kukaj.model.LiveStream
import gaspapp.kukaj.model.LiveStreamCategory
import org.json.JSONObject
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

    fun loadCategories(applicationContext: Context) {
        val json: JSONObject = try {
            applicationContext.openFileInput(LiveStreamCategoryStore.CATEGORIES_CACHE_FILENAME).bufferedReader().use { JSONObject(it.readText()) }
        } catch (ex: Exception) {
            try {
                applicationContext.resources.openRawResource(R.raw.categories).bufferedReader().use { JSONObject(it.readText()) }
            } catch (ex: Exception) {
                Log.w("json", ex.toString())
                JSONObject("{\"hasOther\":true}")
            }
        }
        Repository.getCategoryStore().setCategories(json)
        this.loadLatestCategoryData(applicationContext)
    }

    private fun loadLatestCategoryData(applicationContext: Context) {
        val request = StringRequest(
            Request.Method.GET, "https://raw.githubusercontent.com/pipan/kukaj/main/app/src/main/res/raw/categories.json",
            { response ->
                try {
                    val json = JSONObject(response.toString())
                    Repository.getCategoryStore().setCategories(json)

                    applicationContext.openFileOutput(LiveStreamCategoryStore.CATEGORIES_CACHE_FILENAME, Context.MODE_PRIVATE).use {
                        Log.d("json", json.toString())
                        it.write(json.toString().toByteArray())
                    }
                } catch (ex: Exception) {
                    Log.w("json", ex.toString())
                }
            }, { error ->
                Log.w("json", error)
                Log.w("json", error.networkResponse.statusCode.toString())
                Log.w("json", error.networkResponse.data.decodeToString())
            })
        request.setRetryPolicy(DefaultRetryPolicy(6000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT))
        Services.getHttpQueue().add(request)
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
//        var inCategory: List<Long> = LinkedList()
//        var otherCategory: CategoryModel? = null
        for (category in this.categoryList!!) {
//            if (category.id == "OTHER") {
//                otherCategory = category
//            }
//            inCategory = inCategory + category.streamIdList

            var items: List<LiveStream> = ArrayList()
            for (item in this.liveStreamList!!) {
                if (!category.categorizer.matches(item)) {
                    continue
                }
                items = items + item
            }
            if (items.isNotEmpty()) {
                liveStreamCategoryList = liveStreamCategoryList + LiveStreamCategory(category, items)
            }
        }

//        var items: List<LiveStream> = ArrayList()
//        for (item in this.liveStreamList!!) {
//            if (inCategory.contains(item.id)) {
//                continue
//            }
//            items = items + item
//        }
//        if (items.isNotEmpty() && otherCategory != null) {
//            liveStreamCategoryList = liveStreamCategoryList + LiveStreamCategory(otherCategory, items)
//        }
        this.update(liveStreamCategoryList)
    }
    companion object {
        const val CATEGORIES_CACHE_FILENAME = "categories.json"
    }

}