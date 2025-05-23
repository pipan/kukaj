package gaspapp.kukaj.store

class Subscription<T>() {
    private lateinit var store: Store<T>
    private lateinit var selector: StoreSelector<T>

    constructor(store: Store<T>, selector: StoreSelector<T>) : this() {
        this.store = store
        this.selector = selector
    }

    fun unsubscribe() {
        this.store.unsubscribe(this.selector)
    }
}