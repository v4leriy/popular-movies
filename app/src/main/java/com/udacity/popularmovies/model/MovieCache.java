package com.udacity.popularmovies.model;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// Full repository implementation seems unnecessary for given requirements
class MovieCache {

    // cache by id
    private static final Map<String, MutableLiveData<Movie>> movies = new ConcurrentHashMap<>();

    static LiveData<Movie> getMovie(String id) {
        return movies.get(id);
    }

    static void putMovie(Movie movie) {
        if (movies.containsKey(movie.id)) {
            MutableLiveData<Movie> movieData = movies.get(movie.id);
            movieData.setValue(movie);
        } else {
            MutableLiveData<Movie> movieData = new MutableLiveData<>();
            movieData.setValue(movie);
            movies.put(movie.id, movieData);
        }
    }
}
