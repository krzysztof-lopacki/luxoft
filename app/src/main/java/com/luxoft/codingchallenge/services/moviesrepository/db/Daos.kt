package com.luxoft.codingchallenge.services.moviesrepository.db

import androidx.paging.DataSource
import androidx.room.*
import com.luxoft.codingchallenge.models.Movie
import io.reactivex.Observable

@Dao
interface MoviesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun add(movies: List<MovieEntity>)

    @Query("DELETE FROM movies")
    fun clear()

    @Transaction
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT DISTINCT movies.*, COALESCE(favourites.isFavourite, 0) as isFavourite " +
            "FROM movies LEFT JOIN favourites ON movies.id = favourites.movieId ORDER BY movies.sortingIndex")
    fun getAllPagedList(): DataSource.Factory<Int, Movie>

    @Query("SELECT sortingIndex FROM movies WHERE id = :movieId LIMIT 1")
    fun getSortingIndex(movieId: Long): Long?

    @Query("SELECT MIN(sortingIndex) FROM movies")
    fun getMinSortingIndex(): Long?

    @Query("SELECT MAX(sortingIndex) FROM movies")
    fun getMaxSortingIndex(): Long?

    @Query("SELECT * FROM movies WHERE id in (:movieIds) ORDER BY sortingIndex")
    fun getMovies(movieIds: List<Long>): List<MovieEntity>
}

@Dao
interface FavouritesDao {
    @Query("SELECT COUNT(1) from favourites WHERE movieId = :movieId")
    fun isPresent(movieId: Long): Boolean

    @Query("INSERT INTO favourites (movieId, isFavourite) VALUES (:movieId, 1)")
    fun add(movieId: Long)

    @Transaction
    fun addIfNotPresent(movieId: Long) {
        if (!isPresent(movieId)) {
            add(movieId)
        }
    }

    @Query("DELETE FROM favourites WHERE movieId = :movieId")
    fun remove(movieId: Long)

    @Query("SELECT isFavourite FROM favourites WHERE movieId = :movieId UNION ALL SELECT 0")
    fun isFavourite(movieId: Long): Observable<Boolean>

    @Query("SELECT * from favourites")
    fun getAll(): List<FavouriteMovieEntity>
}