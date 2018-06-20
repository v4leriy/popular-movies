package com.udacity.popularmovies.model;

import android.app.Application;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

public class MovieDetailViewModelFactory implements ViewModelProvider.Factory {

    private final Application application;
    private final String movieId;

    public MovieDetailViewModelFactory(Application application, String movieId) {
        this.application = application;
        this.movieId = movieId;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new MovieDetailViewModel(application, movieId);
    }
}
