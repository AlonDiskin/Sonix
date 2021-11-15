package com.diskin.alon.sonix.common.uitesting

import android.view.View
import android.widget.TextClock
import androidx.annotation.StringRes
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.matcher.BoundedMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher

fun isRecyclerViewItemsCount(size: Int): Matcher<View> {
    return object : BoundedMatcher<View, RecyclerView>(RecyclerView::class.java) {
        override fun describeTo(description: Description) {
            description.appendText("with items count:${size}")
        }

        override fun matchesSafely(item: RecyclerView): Boolean {
            return item.adapter!!.itemCount == size
        }

    }
}

fun withTimeZone(timeZone: String): Matcher<View> {
    return object : BoundedMatcher<View,TextClock>(TextClock::class.java) {
        override fun describeTo(description: Description) {
            description.appendText("with time zone :${timeZone}")
        }

        override fun matchesSafely(item: TextClock): Boolean {
            return item.timeZone == timeZone
        }
    }
}

fun withTimeFormat24(format: String?): Matcher<View> {
    return object : BoundedMatcher<View,TextClock>(TextClock::class.java) {
        override fun describeTo(description: Description) {
            description.appendText("with 24 time format:${format}")
        }

        override fun matchesSafely(item: TextClock): Boolean {
            return when(format) {
                null -> item.format24Hour == null
                else -> item.format24Hour.toString() == format
            }
        }
    }
}

fun withTimeFormat12(format: String?): Matcher<View> {
    return object : BoundedMatcher<View,TextClock>(TextClock::class.java) {
        override fun describeTo(description: Description) {
            description.appendText("with 12 time format:${format}")
        }

        override fun matchesSafely(item: TextClock): Boolean {
            return when(format) {
                null -> item.format12Hour == null
                else -> item.format12Hour.toString() == format
            }
        }
    }
}

fun withSearchViewHint(hint: String): Matcher<View> {
    return object : BoundedMatcher<View, SearchView>(SearchView::class.java) {
        override fun describeTo(description: Description) {
            description.appendText("with hint:${hint}")
        }

        override fun matchesSafely(item: SearchView): Boolean {
            return item.queryHint == hint
        }
    }
}

fun withSearchViewHint(@StringRes id: Int): Matcher<View> {
    return object : BoundedMatcher<View, SearchView>(SearchView::class.java) {
        private var hint = ""

        override fun describeTo(description: Description) {
            description.appendText("with hint:${hint}")
        }

        override fun matchesSafely(item: SearchView): Boolean {
            val context = item.context!!
            hint = context.getString(id)
            return item.queryHint == hint
        }
    }
}

fun withSearchViewQuery(query: String): Matcher<View> {
    return object : BoundedMatcher<View, SearchView>(SearchView::class.java) {
        override fun describeTo(description: Description) {
            description.appendText("with search query:${query}")
        }

        override fun matchesSafely(item: SearchView): Boolean {
            return item.query.toString() == query
        }
    }
}
