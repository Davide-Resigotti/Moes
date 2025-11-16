package com.moes.ui.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mapbox.search.result.SearchSuggestion
import kotlinx.coroutines.delay

/**
 * A reusable, "dumb" composable that provides a search bar UI.
 * It does not know about any ViewModel. It only receives state and exposes events.
 * It includes a debounce mechanism to delay search queries.
 */
@Composable
fun SearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    suggestions: List<SearchSuggestion>,
    onSuggestionSelected: (SearchSuggestion) -> Unit
) {
    // This internal state holds the text field's value and updates immediately as the user types.
    var internalQuery by remember { mutableStateOf(query) }

    // When the external query changes (e.g., cleared by the ViewModel after a selection),
    // we update our internal state to match.
    LaunchedEffect(query) {
        if (internalQuery != query) {
            internalQuery = query
        }
    }

    // This effect listens for changes in the internal state.
    // When the user stops typing for 300ms, it calls the onQueryChanged event
    // to trigger the actual search. This is called "debouncing".
    LaunchedEffect(internalQuery) {
        if (internalQuery != query) {
            delay(300L) // Wait for 300ms of inactivity
            onQueryChanged(internalQuery)
        }
    }

    Column {
        OutlinedTextField(
            value = internalQuery,
            // Update the internal state on every character change.
            onValueChange = { internalQuery = it },
            label = { Text("Search") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            items(suggestions) { suggestion ->
                Text(
                    text = suggestion.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSuggestionSelected(suggestion) }
                        .padding(vertical = 12.dp)
                )
            }
        }
    }
}
