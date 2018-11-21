package com.r0adkll.deckbuilder.arch.ui.features.filter.di


import com.r0adkll.deckbuilder.arch.ui.features.search.DrawerInteractor
import dagger.Module
import dagger.Provides

/**
 * The Dagger module link for screens who want to use the filter component
 *
 * @param filterIntentions a set of intentions that the parent screen can use to communicate with the filter component
 * @param drawerInteractor an interactor for the Filter component to use to control the Drawer that it is a part of
 */
@Module
class FilterableModule(
        val filterIntentions: FilterIntentions,
        val drawerInteractor: DrawerInteractor
) {

    @Provides
    fun provideFilterIntentions(): FilterIntentions = filterIntentions

    @Provides
    fun provideDrawerInteractor(): DrawerInteractor = drawerInteractor
}