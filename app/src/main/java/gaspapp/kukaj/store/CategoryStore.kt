package gaspapp.kukaj.store

import android.util.Log
import gaspapp.kukaj.model.CategoryModel
import gaspapp.kukaj.model.LiveStream
import gaspapp.kukaj.source.Categorizer
import gaspapp.kukaj.source.kukaj.KukajIdCategorizer
import gaspapp.kukaj.source.kukaj.KukajOtherCategorizer
import gaspapp.kukaj.source.steppelife.SteppelifeHostCategorizer
import org.json.JSONObject

class CategoryStore() : Store<List<CategoryModel>>(ArrayList()) {
    fun setCategories(json: JSONObject) {
        var categorizerList: List<Categorizer> = ArrayList()
        var categoryList: List<CategoryModel> = ArrayList()
        try {
            for (i in 0 until json.getJSONArray("byId").length()) {
                val jsonObject = json.getJSONArray("byId").getJSONObject(i)
                val jsonIdList = jsonObject.getJSONArray("idList")
                var idList: List<Long> = ArrayList()
                for (j in 0 until jsonIdList.length()) {
                    idList += jsonIdList.getLong(j)
                }
                val categorizer = KukajIdCategorizer(idList)
                categorizerList += categorizer
                categoryList += CategoryModel(jsonObject.getString("id"), jsonObject.getString("title"), categorizer)
            }
        } catch (ex: Exception) { Log.w("json", ex.toString()) }
        try {
            if (json.getBoolean("hasSteppelife")) {
                val steppelifeHostCategorizer = SteppelifeHostCategorizer()
                categoryList += CategoryModel("STEPPELIFE", "Steppelife", steppelifeHostCategorizer)
            }
        } catch (ex: Exception) { Log.w("json", ex.toString()) }
        try {
            if (json.getBoolean("hasOther")) {
                val otherCategorizer = KukajOtherCategorizer(categorizerList)
                categoryList += CategoryModel("OTHER", "Ostatné", otherCategorizer)
            }
        } catch (ex: Exception) { Log.w("json", ex.toString()) }

        this.update(categoryList)
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