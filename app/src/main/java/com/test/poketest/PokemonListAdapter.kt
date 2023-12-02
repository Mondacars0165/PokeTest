import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.test.poketest.R



class PokemonListAdapter : ListAdapter<PokemonListItem, PokemonListAdapter.PokemonViewHolder>(PokemonDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PokemonViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pokemon, parent, false)
        return PokemonViewHolder(view)
    }

    override fun onBindViewHolder(holder: PokemonViewHolder, position: Int) {
        val pokemon = getItem(position)
        holder.bind(pokemon)
    }

    inner class PokemonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val pokemonNameTextView: TextView = itemView.findViewById(R.id.pokemonNameTextView)

        fun bind(pokemon: PokemonListItem) {
            pokemonNameTextView.text = pokemon.name
            // Puedes personalizar la presentación según tus necesidades
        }
    }
}

class PokemonDiffCallback : DiffUtil.ItemCallback<PokemonListItem>() {
    override fun areItemsTheSame(oldItem: PokemonListItem, newItem: PokemonListItem): Boolean {
        return oldItem.name == newItem.name
    }

    override fun areContentsTheSame(oldItem: PokemonListItem, newItem: PokemonListItem): Boolean {
        return oldItem == newItem
    }
}
