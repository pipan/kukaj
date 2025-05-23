package gaspapp.kukaj.store

import java.util.LinkedList

open class Store<T>(private var value: T) {
    private var selectors: List<StoreSelector<T>> = LinkedList()

    fun subscribe(selector: StoreSelector<T>): Subscription<T> {
        if (!this.selectors.contains(selector)) {
            this.selectors = this.selectors + selector
        }
        return Subscription(this, selector)
    }

    fun subscribeAndUpdate(selector: StoreSelector<T>) {
        this.subscribe(selector)
        selector.onStoreUpdate(this.value)
    }

    fun unsubscribe(selector: StoreSelector<T>) {
        if (!this.selectors.contains(selector)) {
            return
        }
        this.selectors = this.selectors - selector
    }

    fun unsubscribeAll() {
        this.selectors = LinkedList()
    }

    fun getValue(): T {
        return this.value
    }

    protected fun update(value: T) {
        this.value = value
        for (selector in this.selectors) {
            selector.onStoreUpdate(value)
        }
    }
}