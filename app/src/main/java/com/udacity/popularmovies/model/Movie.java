package com.udacity.popularmovies.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "movie")
public class Movie {

    @PrimaryKey
    @NonNull
    public String id;
    public String title;
    public String image;
    public String overview;
    public Double voteAverage;
    public String releaseDate;
    public boolean favorite;

}
