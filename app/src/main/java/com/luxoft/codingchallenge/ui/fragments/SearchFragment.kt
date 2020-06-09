package com.luxoft.codingchallenge.ui.fragments

import android.app.SearchManager
import android.database.Cursor
import android.database.MatrixCursor
import android.os.Bundle
import android.provider.BaseColumns
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.cursoradapter.widget.SimpleCursorAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.luxoft.codingchallenge.R
import com.luxoft.codingchallenge.models.Movie
import com.luxoft.codingchallenge.ui.activities.MovieDetailsActivity
import com.luxoft.codingchallenge.utils.livedata.HandleableEvent
import com.luxoft.codingchallenge.utils.ui.createToast
import com.luxoft.codingchallenge.viewmodels.SearchViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Unfortunately [SearchView] is not showing autocomplete for input shorter than 2.
 */
private const val MIN_INPUT_LENGTH = 2

class SearchFragment : Fragment() {
    private val searchViewModel: SearchViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        retainInstance = true
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        listenToNavigationRequests()

        /**
         * It seems that it is no longer valid to create view-less fragments
         * when biding is in use.
         */
        return View(context)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_fragment_menu_items, menu)
        val searchItem: MenuItem = menu.findItem(R.id.search)
        setupSearchView(searchItem.actionView as SearchView)
    }

    /**
     * Listens to and performs any navigation requests initiated by the view model.
     */
    private fun listenToNavigationRequests() {
        searchViewModel.showMovieDetailsRequests.observe(viewLifecycleOwner, Observer { wrappedMovie ->
            wrappedMovie.handle { movie ->
                if (movie != null) {
                    startActivity(MovieDetailsActivity.createStartingIntent(requireActivity(), movie))
                } else {
                    createToast(requireActivity().applicationContext, R.string.search_nothing_found).show()
                }
                true
            }
        })
    }

    private fun setupSearchView(searchView: SearchView) {
        searchView.setIconifiedByDefault(false)
        searchView.queryHint = getString(R.string.search_hint)

        // set suggestions adapter
        val suggestionsAdapter = SimpleCursorAdapter(requireActivity(), android.R.layout.simple_list_item_1,
            null, // cursor will be provided later
            arrayOf(SearchManager.SUGGEST_COLUMN_TEXT_1), intArrayOf(android.R.id.text1), 0)
        searchView.suggestionsAdapter = suggestionsAdapter
        searchViewModel.searchSuggestions.observe(viewLifecycleOwner, Observer { movies ->
            suggestionsAdapter.swapCursor(movies.toCursor())
        })

        // handling user input
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                /**
                 * #REVIEW#: In a full-scale application clicking on the Submit button/icon (magnifying tool)
                 * should navigate user to the list where all search results are shown.
                 *
                 * As the coding challenge requirements clearly mention only two main screens:
                 * - Now Playing list
                 * - Movie Details screen
                 * I assume that implementing this All Search Results screen is not required.
                 *
                 * I have chosen a more simple approach: I simply go to the details of the movie
                 * which is the best match (first on the list).
                 */
                searchViewModel.onSearchSuggestionClicked(getBestMatch())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText?.length ?: 0 >= MIN_INPUT_LENGTH) {
                    searchViewModel.searchInput.postValue(HandleableEvent(newText ?: ""))
                    return true
                }
                return false
            }
        })

        // handling suggestions selection
        searchView.setOnSuggestionListener(object : SearchView.OnSuggestionListener {
            override fun onSuggestionSelect(position: Int): Boolean {
                return false
            }

            override fun onSuggestionClick(position: Int): Boolean {
                searchViewModel.onSearchSuggestionClicked(getBestMatch())
                return true
            }
        })
    }

    /**
     * @return  Returns a first [Movie] that matches search criteria. First movie on the list
     *          is the one with the best match.
     */
    private fun getBestMatch(): Movie? {
        return searchViewModel.searchSuggestions.value?.firstOrNull()
    }
}

/**
 * Standard columns used by the adapter utilized by [SearchView].
 */
private val SUGGESTIONS_CURSOR_COLUMNS = arrayOf(BaseColumns._ID,
    SearchManager.SUGGEST_COLUMN_TEXT_1,
    SearchManager.SUGGEST_COLUMN_INTENT_DATA)

private fun List<Movie>.toCursor(): Cursor {
    val cursor = MatrixCursor(SUGGESTIONS_CURSOR_COLUMNS)
    for (i in indices) {
        cursor.addRow(arrayOf(i, get(i).title, get(i).id))
    }
    return cursor
}