package com.r0adkll.deckbuilder.arch.data.features.cards.repository


import com.r0adkll.deckbuilder.arch.data.features.cards.repository.source.CardDataSource
import com.r0adkll.deckbuilder.arch.data.mappings.CardMapper
import com.r0adkll.deckbuilder.arch.domain.features.cards.model.Expansion
import com.r0adkll.deckbuilder.arch.domain.features.cards.model.PokemonCard
import com.r0adkll.deckbuilder.arch.domain.features.cards.repository.CardRepository
import com.r0adkll.deckbuilder.util.Schedulers
import io.pokemontcg.Pokemon
import io.pokemontcg.model.Card
import io.pokemontcg.model.SuperType
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import javax.inject.Inject


class DefaultCardRepository @Inject constructor(
        val api: Pokemon,
        val dataSource: CardDataSource,
        val schedulers: Schedulers
) : CardRepository {

    override fun getExpansions(): Observable<List<Expansion>> {
        return dataSource.getExpansions()
    }


    override fun search(type: SuperType, text: String): Observable<List<PokemonCard>> {
        return Observable.zip(getExpansions(), getSearchRequest(type, text), BiFunction { expansions, cards ->
            cards.map { CardMapper.to(it, expansions) }
        })
    }


    private fun getSearchRequest(type: SuperType, query: String): Observable<List<Card>> {
        return api.card()
                .where {
                    supertype = type.displayName
                    name = query
                }
                .observeAll()
                .subscribeOn(schedulers.network)
    }
}