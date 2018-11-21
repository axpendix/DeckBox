package com.r0adkll.deckbuilder.arch.ui.features.filter.di

/**
 * Dagger component extension that screens who want to implement and use the search Filter can
 * subclass
 */
interface FilterableComponent {

    fun plus(module: FilterModule): FilterComponent
}