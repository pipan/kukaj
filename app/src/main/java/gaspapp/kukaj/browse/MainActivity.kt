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
        Repository.getLiveStreamCategoryStore().loadCategories(applicationContext)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.main_browse_fragment, MainFragment())
                    .commitNow()
        }
    }
}