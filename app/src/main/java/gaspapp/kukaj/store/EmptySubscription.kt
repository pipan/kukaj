package gaspapp.kukaj.store

class EmptySubscription<T>: Subscription<T> {
    override fun unsubscribe() {
        return
    }
}