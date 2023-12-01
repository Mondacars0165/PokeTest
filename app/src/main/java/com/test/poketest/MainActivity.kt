package com.test.poketest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.icons.Icons
import androidx.compose.material3.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.test.poketest.api.PokemonApi
import com.test.poketest.api.model.PokemonDetailsResponse
import com.test.poketest.ui.theme.PokeTestTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PokeTestTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PokemonListScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PokemonListScreen() {
    var searchQuery by remember { mutableStateOf("") }
    var pokemonDetails by remember { mutableStateOf<PokemonDetailsResponse?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search Bar
        SearchBar(
            searchQuery = searchQuery,
            onSearchQueryChange = { newQuery ->
                searchQuery = newQuery
            },
            onSearchPerform = {
                if (searchQuery.isNotEmpty()) {
                    // Perform API call to get Pokémon details based on the searchQuery
                    scope.launch(Dispatchers.IO) {
                        isLoading = true
                        try {
                            val response = PokemonApi.pokemonApiService.getPokemonDetails(searchQuery)
                            pokemonDetails = response
                        } catch (e: Exception) {
                            // Handle error
                        } finally {
                            isLoading = false
                        }
                    }
                }
            }
        )

        // Pokemon Details
        if (isLoading) {
            // Show loading indicator
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(16.dp)
                    .size(50.dp)
                    .align(Alignment.CenterHorizontally)
            )
        } else {
            // Display Pokémon details
            pokemonDetails?.let {
                PokemonDetailsCard(it)
            }
        }
    }
}

@Composable
fun SearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchPerform: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            value = searchQuery,
            onValueChange = {
                onSearchQueryChange(it)
            },
            placeholder = {
                Text(text = "Search Pokémon")
            },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Search,
                keyboardType = KeyboardType.Text
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    onSearchPerform()
                }
            ),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search Icon"
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            onSearchQueryChange("")
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear Icon"
                        )
                    }
                }
            },
            singleLine = true,
            colors = TextFieldDefaults.textFieldColors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )

        // Search button
        Button(
            onClick = {
                onSearchPerform()
            },
            modifier = Modifier
                .padding(start = 8.dp)
                .heightIn(min = 48.dp),
        ) {
            Text(text = "Search")
        }
    }
}

@Composable
fun PokemonDetailsCard(pokemonDetails: PokemonDetailsResponse) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Name: ${pokemonDetails.name}",
                style = MaterialTheme.typography.h6
            )
            Text(
                text = "ID: ${pokemonDetails.id}",
                style = MaterialTheme.typography.body1
            )
            Text(
                text = "Types: ${pokemonDetails.types.joinToString { it.type.name }}",
                style = MaterialTheme.typography.body1
            )
            Text(
                text = "Abilities: ${pokemonDetails.abilities.joinToString
