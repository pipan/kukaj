package gaspapp.kukaj.detail

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import gaspapp.kukaj.R

class DetailErrorActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        supportFragmentManager
            .beginTransaction()
            .add(R.id.details_fragment, DetailErrorFragment())
            .commit()
    }
}