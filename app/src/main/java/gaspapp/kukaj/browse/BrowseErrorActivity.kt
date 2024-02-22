package gaspapp.kukaj.browse

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import gaspapp.kukaj.R

/**
 * BrowseErrorActivity shows how to use ErrorFragment.
 */
class BrowseErrorActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager
            .beginTransaction()
            .add(R.id.main_browse_fragment, ErrorFragment())
            .commit()
    }
}