package com.r0adkll.deckbuilder.arch.ui.features.search.filter.adapter


import android.support.annotation.StringRes
import com.r0adkll.deckbuilder.R
import com.r0adkll.deckbuilder.arch.domain.Rarity
import com.r0adkll.deckbuilder.arch.domain.features.cards.model.Expansion
import com.r0adkll.deckbuilder.arch.ui.components.RecyclerItem
import com.r0adkll.deckbuilder.arch.ui.features.search.filter.FilterUi
import com.r0adkll.deckbuilder.arch.ui.features.search.filter.FilterUi.FilterAttribute
import io.pokemontcg.model.SubType


sealed class Item : RecyclerItem {

    abstract val itemId: Long


    data class Header(@StringRes val title: Int) : Item() {

        override fun isItemSame(new: RecyclerItem): Boolean = when(new) {
            is Header -> new.title == title
            else -> false
        }


        override fun isContentSame(new: RecyclerItem): Boolean = when(new) {
            is Header -> new.title == title
            else -> false
        }

        override val itemId: Long = title.toLong()
        override val layoutId: Int = R.layout.item_filter_header
    }


    data class Type(
            val key: String,
            val selected: List<io.pokemontcg.model.Type>
    ) : Item() {

        override fun isItemSame(new: RecyclerItem): Boolean = when(new) {
            is Type -> new.key == key
            else -> false
        }


        override fun isContentSame(new: RecyclerItem): Boolean = when(new) {
            is Type -> new == this
            else -> false
        }

        override val itemId: Long = key.hashCode().toLong()
        override val layoutId: Int = R.layout.item_filter_types
    }


    data class Attribute(
            val attributes: List<FilterAttribute>,
            val selected: List<FilterAttribute>
    ) : Item() {

        override fun isItemSame(new: RecyclerItem): Boolean = when(new) {
            is Attribute -> true
            else -> false
        }

        override fun isContentSame(new: RecyclerItem): Boolean = when(new) {
            is Attribute -> new == this
            else -> false
        }

        override val itemId: Long = this.attributes.hashCode().toLong()
        override val layoutId: Int = R.layout.item_filter_attributes
    }


    sealed class Option<out T>(
            open val key: String,
            open val option: T,
            open val isSelected: Boolean
    ) : Item() {

        abstract val text: String

        override fun isItemSame(new: RecyclerItem): Boolean = when(new) {
            is Option<*> -> new.key == key
            else -> false
        }


        override fun isContentSame(new: RecyclerItem): Boolean = when(new) {
            is Option<*> -> new == this
            else -> false
        }


        override val layoutId: Int = R.layout.item_filter_option


        data class ExpansionOption(
                override val key: String,
                override val option: Expansion,
                override val isSelected: Boolean
        ) : Option<Expansion>(key, option, isSelected) {

            override val itemId: Long = option.code.hashCode().toLong()
            override val text: String = option.name
        }


        data class RarityOption(
                override val key: String,
                override val option: Rarity,
                override val isSelected: Boolean
        ) : Option<Rarity>(key, option, isSelected) {

            override val itemId: Long = option.hashCode().toLong()
            override val text: String = option.name.toLowerCase().capitalize()
        }

    }


    data class ViewMore(@StringRes val title: Int) : Item() {

        override fun isItemSame(new: RecyclerItem): Boolean = when(new) {
            is ViewMore -> new.title == title
            else -> false
        }


        override fun isContentSame(new: RecyclerItem): Boolean = when(new) {
            is ViewMore -> new.title == title
            else -> false
        }

        override val itemId: Long = title.toLong()
        override val layoutId: Int = R.layout.item_filter_view_more
    }


    data class ValueRange(
            val key: String,
            val min: Int,
            val max: Int,
            val value: Value = Value(0, Modifier.NONE)
    ) : Item() {

        override fun isItemSame(new: RecyclerItem): Boolean = when(new) {
            is ValueRange -> new.key == key
            else -> false
        }


        override fun isContentSame(new: RecyclerItem): Boolean = when(new) {
            is ValueRange -> new == this
            else -> false
        }


        override val itemId: Long = key.hashCode().toLong()
        override val layoutId: Int = R.layout.item_filter_value_range


        data class Value(
                val value: Int,
                val modifier: Modifier
        ) {
            fun toFilter(): String = if (value == 0 && modifier == Modifier.NONE) {
                ""
            } else {
                "${modifier.value}$value"
            }
        }


        enum class Modifier(val value: String){
            NONE(""),
            LESS_THAN("lt"),
            LESS_THAN_EQUALS("lte"),
            GREATER_THAN("gt"),
            GREATER_THAN_EQUALS("gte")
        }

    }

}