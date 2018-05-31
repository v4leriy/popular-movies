
package com.udacity.popularmovies.model;

import java.util.List;

public class MovieList {

    private List<Movie> movies;
    private String message;

    public List<Movie> getMovies() {
        return movies;
    }

    public void setMovies(List<Movie> movies) {
        this.movies = movies;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isError() {
        return movies == null && message != null;
    }
}
