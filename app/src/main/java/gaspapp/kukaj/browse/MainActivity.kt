package gaspapp.kukaj.browse

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import gaspapp.kukaj.R
import gaspapp.kukaj.Repository

/**
 * Loads [MainFragment].
 */
class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Repository.init(applicationContext)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.main_browse_fragment, MainFragment())
                    .commitNow()
        }
    }
}