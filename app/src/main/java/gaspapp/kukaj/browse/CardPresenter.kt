package gaspapp.kukaj.browse

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.text.style.LineHeightSpan.WithDensity
import android.util.DisplayMetrics
import android.util.Log
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import com.bumptech.glide.Glide
import gaspapp.kukaj.R
import gaspapp.kukaj.model.LiveStream
import kotlin.math.roundToInt
import kotlin.properties.Delegates


/**
 * A CardPresenter is used to generate Views and bind Objects to them on demand.
 * It contains an ImageCardView.
 */
class CardPresenter(private val displayMetrics: DisplayMetrics) : Presenter() {
    private var sSelectedBackgroundColor: Int by Delegates.notNull()
    private var sDefaultBackgroundColor: Int by Delegates.notNull()

    override fun onCreateViewHolder(parent: ViewGroup): Presenter.ViewHolder {
        sDefaultBackgroundColor = ContextCompat.getColor(parent.context, R.color.default_background)
        sSelectedBackgroundColor = ContextCompat.getColor(parent.context,
            R.color.selected_background
        )

        val cardView = object : ImageCardView(parent.context) {
            override fun setSelected(selected: Boolean) {
                updateCardBackgroundColor(this, selected)
                super.setSelected(selected)
            }
        }

        cardView.isFocusable = true
        cardView.isFocusableInTouchMode = true
        updateCardBackgroundColor(cardView, false)
        return Presenter.ViewHolder(cardView)
    }

    override fun onBindViewHolder(viewHolder: Presenter.ViewHolder, item: Any) {
        val liveStream = item as LiveStream
        val cardView = viewHolder.view as ImageCardView

        if (liveStream.cardImageUrl != null) {
            val width = convertDpToPixel(displayMetrics, CARD_WIDTH)
            val height = convertDpToPixel(displayMetrics, CARD_HEIGHT)
            cardView.titleText = liveStream.title
            cardView.setMainImageDimensions(width, height)
            Glide.with(viewHolder.view.context)
                    .load(liveStream.cardImageUrl)
                    .centerCrop()
                    .error(R.drawable.default_background)
                    .into(cardView.mainImageView)
        }
    }

    override fun onUnbindViewHolder(viewHolder: Presenter.ViewHolder) {
        val cardView = viewHolder.view as ImageCardView
        // Remove references to images so that the garbage collector can free up memory
        cardView.badgeImage = null
        cardView.mainImage = null
    }

    private fun updateCardBackgroundColor(view: ImageCardView, selected: Boolean) {
        val colorFrom: Int = if (selected) sDefaultBackgroundColor else sSelectedBackgroundColor
        val colorTo: Int = if (selected) sSelectedBackgroundColor else sDefaultBackgroundColor
        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
        if (selected != view.isSelected) {
            colorAnimation.setDuration(160)
        } else {
            colorAnimation.setDuration(0)
        }
        colorAnimation.addUpdateListener { animator ->
            view.setBackgroundColor(animator.animatedValue as Int)
            view.setInfoAreaBackgroundColor(animator.animatedValue as Int)
        }
        colorAnimation.start()
    }

    private fun convertDpToPixel(displayMetrics: DisplayMetrics, dp: Int): Int {
        val density = displayMetrics.density
        return (dp.toFloat() * density).roundToInt()
    }

    companion object {
        private val TAG = "CardPresenter"

        private val CARD_WIDTH = 150
        private val CARD_HEIGHT = 100
    }
}