package gaspapp.kukaj

import android.content.res.ColorStateList
import android.content.res.Resources
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class SpinnerFragment(private val gravity: Int) : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val progressBar = ProgressBar(container?.context)
        progressBar.indeterminateTintList = ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.selected_background))
        if (container is FrameLayout) {
            val layoutParams = FrameLayout.LayoutParams(SPINNER_WIDTH, SPINNER_HEIGHT, gravity)
            progressBar.layoutParams = layoutParams
        }
        return progressBar
    }

    companion object {
        private val SPINNER_WIDTH = 120
        private val SPINNER_HEIGHT = 120
    }
}