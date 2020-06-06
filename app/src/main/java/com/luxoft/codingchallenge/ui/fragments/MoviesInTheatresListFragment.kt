package com.luxoft.codingchallenge.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.luxoft.codingchallenge.R
import com.luxoft.codingchallenge.databinding.FragmentMoviesInTheatresBinding
import com.luxoft.codingchallenge.databinding.MoviesInTheatresItemBinding
import com.luxoft.codingchallenge.databinding.MoviesInTheatresLoadingIndicatorBinding
import com.luxoft.codingchallenge.models.LoadingStatus
import com.luxoft.codingchallenge.models.Movie
import com.luxoft.codingchallenge.services.moviesrepository.MoviesInTheatresUpdater
import com.luxoft.codingchallenge.viewmodels.MoviesInTheatresListViewModel
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class MoviesInTheatresListFragment : Fragment() {
    private lateinit var binding: FragmentMoviesInTheatresBinding

    private val moviesListViewModel: MoviesInTheatresListViewModel by viewModel()
    private val moviesInTheatresUpdater: MoviesInTheatresUpdater by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentMoviesInTheatresBinding.inflate(inflater)
        binding.lifecycleOwner = this

        configureAndBindMoviesInTheatresView()
        bindSwipeToRefresh()

        return binding.root
    }

    private fun configureAndBindMoviesInTheatresView() {
        val adapter = MoviesInTheatresPagedListAdapter()
        adapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY

        val layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        binding.moviesList.layoutManager = layoutManager
        binding.moviesList.itemAnimator = null
        binding.moviesList.adapter = adapter

        Completable.fromCallable {
            moviesInTheatresUpdater.clearData()
        }
        .observeOn(AndroidSchedulers.mainThread())
        .andThen(Completable.fromCallable {
            moviesListViewModel.moviesInTheatres.observe(viewLifecycleOwner, Observer { newList ->
                if (layoutManager.findFirstVisibleItemPosition() == 0) {
                    binding.moviesList.post {
                        binding.moviesList.smoothScrollToPosition(0)
                    }
                }
                adapter.submitList(newList)
            })
        })
        .subscribeOn(Schedulers.io()).subscribe()
    }

    /**
     * Binds with SwipeRefreshLayout. Unfortunately this layout is not made for direct binding.
     * @SuppressLint("InflateParams") is added because the toast view parent is null during inflation.
     */
    @SuppressLint("InflateParams")
    private fun bindSwipeToRefresh() {
        binding.swipeToRefreshLayout.setOnRefreshListener {
            moviesListViewModel.loadRecentMoviesInTheatres()
        }
        moviesListViewModel.loadingRecentMoviesInTheatresStatus.observe(viewLifecycleOwner, Observer { status ->
            binding.swipeToRefreshLayout.isRefreshing = status == LoadingStatus.LOADING
        })
        moviesListViewModel.loadingRecentMoviesErrors.observe(viewLifecycleOwner, Observer { error ->
            error.handle {
                activity?.applicationContext?.let { applicationContext ->
                    val toastView: View = layoutInflater.inflate(R.layout.refreshing_failed_toast, null)
                    val toast = Toast(applicationContext)
                    toast.setGravity(Gravity.BOTTOM, 0, applicationContext.resources.getDimension(R.dimen.toast_bottom_margin).toInt())
                    toast.duration = Toast.LENGTH_SHORT
                    toast.view = toastView
                    toast.show()
                }
                true
            }
        })
    }

    /**
     * Classic [RecyclerView.Adapter]
     */
    private inner class MoviesInTheatresPagedListAdapter : PagedListAdapter<Movie, RecyclerView.ViewHolder>(moviesComparator) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return if (viewType == ViewTypes.MOVIE_VIEW) {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.movies_in_theatres_item, parent, false)
                MovieViewHolder(view)
            } else {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.movies_in_theatres_loading_indicator, parent, false)
                LoadingIndicatorHolder(view)
            }
        }

        override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
            if (holder is LoadingIndicatorHolder) {
                holder.unbind()
            } else if (holder is MovieViewHolder) {
                holder.unbind()
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is MovieViewHolder) {
                holder.bind(getItem(position))
            } else if (holder is LoadingIndicatorHolder) {
                holder.bind()
            }
        }

        override fun getItemViewType(position: Int): Int {
            return if (position == itemCount - 1) {
                ViewTypes.LOADING_INDICATOR
            } else {
                ViewTypes.MOVIE_VIEW
            }
        }

        override fun getItemCount(): Int {
            return super.getItemCount() + 1
        }
    }

    /**
     * Classic view holder that uses binding to hold references to the view and its parts.
     */
    private inner class MovieViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val binding: MoviesInTheatresItemBinding? = DataBindingUtil.bind(view)

        init {
            binding?.lifecycleOwner = viewLifecycleOwner
        }

        fun bind(movie: Movie?) {
            binding?.movie = movie
            binding?.executePendingBindings()
        }

        fun unbind() {
            binding?.movie = null
        }
    }

    /**
     * Classic view holder that uses binding to hold references to the view and its parts.
     */
    private inner class LoadingIndicatorHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val binding: MoviesInTheatresLoadingIndicatorBinding? = DataBindingUtil.bind(view)

        init {
            binding?.lifecycleOwner = viewLifecycleOwner
        }

        fun bind() {
            binding?.viewModel = moviesListViewModel
            binding?.executePendingBindings()
            Log.d("#TEST", "SET to $binding = ${moviesListViewModel.loadingMoreMoviesInTheatresStatus}")

        }
        fun unbind() {
            binding?.viewModel = null
        }
    }

    /**
     * Comparator checking equality and identity of the [Movie]s.
     */
    private val moviesComparator = object : DiffUtil.ItemCallback<Movie>() {
        override fun areItemsTheSame(movie1: Movie, movie2: Movie): Boolean {
            return movie1.id == movie2.id
        }
        override fun areContentsTheSame(movie1: Movie, movie2: Movie): Boolean {
            return movie1 == movie2
        }
    }

    /**
     * Defines the set of view types displayed on the 'movies in theatres' list.
     */
    private object ViewTypes {
        const val MOVIE_VIEW = 1
        const val LOADING_INDICATOR = 2
    }
}