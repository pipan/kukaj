package gaspapp.kukaj.browse

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import gaspapp.kukaj.R
import gaspapp.kukaj.Repository
import gaspapp.kukaj.Services
import org.json.JSONObject

/**
 * Loads [MainFragment].
 */
class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Repository.init()
        Services.init(applicationContext)
        this.setCategories()
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.main_browse_fragment, MainFragment())
                    .commitNow()
        }
    }

    private fun setCategories() {
        val json: JSONObject = try {
            applicationContext.openFileInput(CATEGORIES_CACHE_FILENAME).bufferedReader().use { JSONObject(it.readText()) }
        } catch (ex: Exception) {
            try {
                resources.openRawResource(R.raw.categories).bufferedReader().use { JSONObject(it.readText()) }
            } catch (ex: Exception) {
                Log.w("json", ex.toString())
                JSONObject("{\"hasOther\":true}")
            }
        }
        Repository.getCategoryStore().setCategories(json)
        this.loadLatestCategoryData()
    }

    private fun loadLatestCategoryData() {
        val request = StringRequest(
            Request.Method.GET, "https://raw.githubusercontent.com/pipan/kukaj/main/app/src/main/res/raw/categories.json",
            { response ->
                try {
                    val json = JSONObject(response.toString())
                    Repository.getCategoryStore().setCategories(json)

                    applicationContext.openFileOutput(CATEGORIES_CACHE_FILENAME, Context.MODE_PRIVATE).use {
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

    companion object {
        const val CATEGORIES_CACHE_FILENAME = "categories.json"
    }
}