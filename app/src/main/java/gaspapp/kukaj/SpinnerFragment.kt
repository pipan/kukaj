package gaspapp.kukaj

import android.content.Context
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
import kotlin.math.roundToInt

class SpinnerFragment(private val gravity: Int) : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val progressBar = ProgressBar(container?.context)
        progressBar.indeterminateTintList = ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.selected_background))
        if (container is FrameLayout) {
            val size = convertDpToPixel(context!!, SPINNER_SIZE)
            val layoutParams = FrameLayout.LayoutParams(size, size, gravity)
            progressBar.layoutParams = layoutParams
        }
        return progressBar
    }

    private fun convertDpToPixel(context: Context, dp: Int): Int {
        val density = context.applicationContext.resources.displayMetrics.density
        return (dp.toFloat() * density).roundToInt()
    }

    companion object {
        private const val SPINNER_SIZE = 80
    }
}