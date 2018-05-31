package com.udacity.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.udacity.popularmovies.model.Movie;
import com.udacity.popularmovies.model.MovieListViewModel;

import java.util.Locale;

public class DetailActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "extra_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        if (intent == null) {
            closeOnError();
        }

        String id = intent.getStringExtra(EXTRA_ID);
        if (id == null) {
            // EXTRA_ID not found in intent
            closeOnError();
            return;
        }

        // TODO phase 2
        Movie movie = MovieListViewModel.getMovie(id);
        if (movie == null) {
            closeOnError();
            return;
        }

        setTitle(movie.title);

        ImageView image = findViewById(R.id.image_iv);
        Picasso.with(this)
                .load(movie.image)
                .into(image);

        TextView overviewView = findViewById(R.id.overview_view);
        overviewView.setText(movie.overview);

        TextView yearView = findViewById(R.id.year_view);
        yearView.setText(movie.releaseDate.substring(0, 4));

        TextView ratingView = findViewById(R.id.rating_view);
        ratingView.setText(String.format(Locale.US, "%3.1f/10", movie.voteAverage));
    }

    private void closeOnError() {
        finish();
        Toast.makeText(this, R.string.detail_error_message, Toast.LENGTH_SHORT).show();
    }

}
