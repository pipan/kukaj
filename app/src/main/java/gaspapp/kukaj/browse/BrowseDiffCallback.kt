package gaspapp.kukaj.browse

import androidx.leanback.widget.DiffCallback
import androidx.leanback.widget.ListRow

class BrowseDiffCallback : DiffCallback<ListRow>() {
    override fun areItemsTheSame(oldItem: ListRow, newItem: ListRow): Boolean {
        return oldItem.headerItem.name == newItem.headerItem.name
    }

    override fun areContentsTheSame(oldItem: ListRow, newItem: ListRow): Boolean {
        return oldItem.adapter.size() == newItem.adapter.size()
    }
}