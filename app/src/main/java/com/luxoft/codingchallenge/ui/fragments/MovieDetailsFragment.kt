package com.luxoft.codingchallenge.ui.fragments

import android.content.res.Resources
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.luxoft.codingchallenge.R
import com.luxoft.codingchallenge.databinding.FragmentMovieDetailsBinding
import com.luxoft.codingchallenge.models.Movie
import com.luxoft.codingchallenge.services.api.MovieImageUrlResolver
import com.luxoft.codingchallenge.ui.arguments.ArgumentKeys
import com.luxoft.codingchallenge.utils.ui.createToastWithPlainBackground
import com.luxoft.codingchallenge.viewmodels.MovieDetailsViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf


class MovieDetailsFragment : Fragment() {
    private val viewModel: MovieDetailsViewModel by viewModel {
        parametersOf(resolveMovieFromExtras(requireArguments()))
    }
    private val imageUrlsResolver: MovieImageUrlResolver by inject()

    private lateinit var binding: FragmentMovieDetailsBinding
    private var addRemoveFromFavouritesItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
        Companion.imageUrlsResolver = imageUrlsResolver
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentMovieDetailsBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        bindFavouritesToggle()

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.movie_details_fragment_menu_items, menu)
        addRemoveFromFavouritesItem = menu.findItem(R.id.addRemoveFromFavourites)
        updateFavouriteMenuOption(viewModel.movie.value)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item == addRemoveFromFavouritesItem) {
            viewModel.onToggleFavouriteClicked(viewModel.movie.value)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun bindFavouritesToggle() {
        viewModel.movie.observe(viewLifecycleOwner, Observer { movie ->
            updateFavouriteMenuOption(movie)
        })

        viewModel.onAddedToFavourites.observe(viewLifecycleOwner, Observer { movie ->
            movie.handle {
                createToastWithPlainBackground(requireActivity().applicationContext,
                    R.string.toast_added_to_favourites,
                    backgroundColor = android.R.color.black)
                    .show()
                true
            }
        })

        viewModel.onRemovedFromFavourites.observe(viewLifecycleOwner, Observer { movie ->
            movie.handle {
                createToastWithPlainBackground(requireActivity().applicationContext,
                    R.string.toast_removed_from_favourites,
                    backgroundColor = android.R.color.black)
                    .show()
                true
            }
        })
    }

    private fun updateFavouriteMenuOption(movie: Movie?) {
        addRemoveFromFavouritesItem?.let { menuItem ->
            if (movie?.isFavourite == true) {
                menuItem.icon = ContextCompat.getDrawable(requireContext(), R.drawable.start_filled)
                menuItem.title = resources.getString(R.string.movie_details_menu_remove_from_favourites)
            } else {
                menuItem.icon = ContextCompat.getDrawable(requireContext(), R.drawable.start_outline)
                menuItem.title = resources.getString(R.string.movie_details_menu_add_to_favourites)
            }
        }
    }

    companion object {
        lateinit var imageUrlsResolver: MovieImageUrlResolver

        @JvmStatic
        @BindingAdapter("backdropImageSrc")
        fun loadBackdropImage(view: ImageView, imageUrl: String?) {
            val screenWidth = Resources.getSystem().displayMetrics.widthPixels
            val resolvedImageUrl = imageUrlsResolver.getImageUrl(imageUrl, MovieImageUrlResolver.ImageType.BACKDROP, screenWidth)
            Glide.with(view.context)
                .load(resolvedImageUrl)
                .placeholder(R.drawable.movie_placeholder)
                .centerCrop()
                .into(view)
        }

        private fun resolveMovieFromExtras(extras: Bundle): Movie? {
            return extras.getParcelable(ArgumentKeys.ARGUMENT_KEY_MOVIE)
        }
    }
}