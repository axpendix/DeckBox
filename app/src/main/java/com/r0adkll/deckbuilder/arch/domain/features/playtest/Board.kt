package com.r0adkll.deckbuilder.arch.domain.features.playtest

import com.r0adkll.deckbuilder.arch.domain.features.cards.model.PokemonCard
import java.util.*


/**
 * This represents the entire board state of a game
 */
data class Board(
        val player: Player,
        val opponent: Player,
        val turn: Turn
) {

    /**
     * Get the boards active stadium, if exists
     */
    val stadium: PokemonCard?
        get() = player.stadium ?: opponent.stadium


    data class Turn(
            val count: Int,
            val whos: Player.Type
    )


    /**
     * Represents a player's board state in the game
     */
    data class Player(
            val hand: List<PokemonCard>,
            val prizes: List<Ordered<PokemonCard>>,
            val deck: ArrayDeque<PokemonCard>,
            val discard: List<PokemonCard>,
            val lostZone: List<PokemonCard>,
            val bench: Bench,
            val active: Card?,
            val stadium: PokemonCard?
    ) {

        enum class Type {
            PLAYER,
            OPPONENT
        }
    }

    /**
     * Represents a bench state on the board with a default size of 5, and possible expansion
     * of up to 8 via SkyField
     */
    data class Bench(
            val cards: List<Ordered<Card>> = emptyList(),
            val size: Int = 5
    )


    /**
     * Represents a card on the board, including it's evolutions, energy, tools, status effects,
     * damage, and so on
     */
    data class Card(
            val pokemons: Stack<PokemonCard>,
            val energy: List<PokemonCard>,
            val tool: PokemonCard?,
            val isPoisoned: Boolean,
            val isBurned: Boolean,
            val statusEffect: Status?,
            val damage: Int
    ) {

        /**
         * Conditional status effects
         */
        enum class Status {
            CONFUSED,
            SLEEPING,
            PARALYZED
        }
    }


    /**
     * Class wrapper to indicate specific indexes on items without having to maintain nullable
     * valued arrays. i.e. for prize cards, or for benched cards. This is primarily for UI purposes
     */
    abstract class Ordered<C>(val index: Int, val item: C)
}