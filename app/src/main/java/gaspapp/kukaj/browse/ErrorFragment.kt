package gaspapp.kukaj.browse

import android.content.Intent
import android.os.Bundle
import android.view.View

import androidx.leanback.app.ErrorSupportFragment
import gaspapp.kukaj.R

/**
 * This class demonstrates how to extend [ErrorSupportFragment].
 */
class ErrorFragment : ErrorSupportFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        message = resources.getString(R.string.browse_error_message)
        setDefaultBackground(TRANSLUCENT)

        buttonText = resources.getString(R.string.error_retry_action)
        buttonClickListener = View.OnClickListener {
            val intent = Intent(context!!, MainActivity::class.java)
            startActivity(intent)
        }
    }

    companion object {
        private const val TRANSLUCENT = true
    }
}