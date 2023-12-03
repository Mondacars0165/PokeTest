import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.test.poketest.R

interface PokemonClickListener {
    fun onPokemonClick(pokemon: PokemonListItem)
}

class PokemonListAdapter(private val clickListener: PokemonClickListener) :
    ListAdapter<PokemonListItem, PokemonListAdapter.PokemonViewHolder>(PokemonDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PokemonViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pokemon, parent, false)
        return PokemonViewHolder(view)
    }

    override fun onBindViewHolder(holder: PokemonViewHolder, position: Int) {
        val pokemon = getItem(position)
        holder.bind(pokemon, clickListener)
    }

    inner class PokemonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val pokemonNameTextView: TextView = itemView.findViewById(R.id.pokemonNameTextView)
        private val imageViewSprite: ImageView = itemView.findViewById(R.id.imageViewSprite)

        fun bind(pokemon: PokemonListItem, clickListener: PokemonClickListener) {
            pokemonNameTextView.text = pokemon.name

            // Cargar la imagen con Glide
            Glide.with(itemView.context)
                .load(pokemon.imageUrl)
                .into(imageViewSprite)

            // Manejar clics en el elemento
            itemView.setOnClickListener {
                clickListener.onPokemonClick(pokemon)
            }
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
