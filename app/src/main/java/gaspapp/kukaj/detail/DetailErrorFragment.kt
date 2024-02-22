package gaspapp.kukaj.detail

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.leanback.app.ErrorSupportFragment
import gaspapp.kukaj.R
import gaspapp.kukaj.browse.MainActivity

class DetailErrorFragment : ErrorSupportFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        message = resources.getString(R.string.detail_error_message)
        setDefaultBackground(TRANSLUCENT)

        buttonText = resources.getString(R.string.error_home_action)
        buttonClickListener = View.OnClickListener {
            val intent = Intent(context!!, MainActivity::class.java)
            startActivity(intent)
        }
    }

    companion object {
        private const val TRANSLUCENT = true
    }
}