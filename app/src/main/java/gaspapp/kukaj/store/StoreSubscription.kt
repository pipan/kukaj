package gaspapp.kukaj.store

class StoreSubscription<T>(
    private var store: Store<T>,
    private var selector: StoreSelector<T>
) : Subscription<T> {

    override fun unsubscribe() {
        this.store.unsubscribe(this.selector)
    }
}