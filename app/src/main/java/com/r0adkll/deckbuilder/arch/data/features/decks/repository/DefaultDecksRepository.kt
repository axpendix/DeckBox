package com.r0adkll.deckbuilder.arch.data.features.decks.repository

import com.r0adkll.deckbuilder.arch.domain.features.cards.model.PokemonCard
import com.r0adkll.deckbuilder.arch.domain.features.decks.model.Deck
import com.r0adkll.deckbuilder.arch.domain.features.decks.repository.DecksRepository
import io.reactivex.Observable
import javax.inject.Inject


class DefaultDecksRepository @Inject constructor(

) : DecksRepository {

    override fun createDeck(cards: List<PokemonCard>, name: String, description: String?): Observable<Deck> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    override fun updateDeck(id: Long, cards: List<PokemonCard>, name: String, description: String?): Observable<Deck> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    override fun getDecks(): Observable<List<Deck>> {
        return Observable.just(emptyList()
//                listOf(
//                        Deck(0, "Roaring Fire", "", listOf(
//                                PokemonCard("sm1-001", "Incineroar", 0, "https://images.pokemontcg.io/smp/SM38_hires.png",
//                                        "https://images.pokemontcg.io/smp/SM38_hires.png", null, SuperType.POKEMON, SubType.GX, "Torracat", 160, null, "5", "", "", "",
//                                        CardSet("", "", "", "", 100, false, false, "", ""),
//                                        null, null, null, null)
//                        ))
//                )
        )
    }


    override fun deleteDeck(deck: Deck): Observable<List<Deck>> {
        return Observable.empty() // FIXME: Implement this
    }
}