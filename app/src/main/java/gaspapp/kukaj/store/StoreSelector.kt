package gaspapp.kukaj.store

fun interface StoreSelector<T> {
    fun onStoreUpdate(value: T)
}