package com.udacity.popularmovies;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.udacity.popularmovies.database.MovieDao;
import com.udacity.popularmovies.database.MovieDatabase;
import com.udacity.popularmovies.model.Movie;
import com.udacity.popularmovies.model.MovieDetailViewModel;
import com.udacity.popularmovies.model.MovieDetailViewModelFactory;
import com.udacity.popularmovies.model.Review;
import com.udacity.popularmovies.model.Trailer;

import java.util.Locale;

public class DetailActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "extra_id";

    private Movie movie;

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

        MovieDao movieDao = MovieDatabase.getInstance(this).movieDao();

        // Not convinced ButterKnife makes much difference or improvement
        ImageView image = findViewById(R.id.image_iv);
        TextView overviewView = findViewById(R.id.overview_view);
        TextView yearView = findViewById(R.id.year_view);
        TextView ratingView = findViewById(R.id.rating_view);
        CheckBox favoriteCheckBox = findViewById(R.id.favorite_cb);
        favoriteCheckBox.setOnCheckedChangeListener((button, isChecked) -> {
            if (movie != null && movie.favorite != isChecked) {
                movie.favorite = isChecked;
                AsyncTask.execute(() -> {
                    if (isChecked) {
                        movieDao.insertMovie(movie);
                    } else {
                        movieDao.deleteMovie(movie);
                    }
                });
            }
        });

        MovieDetailViewModelFactory factory = new MovieDetailViewModelFactory(getApplication(), id);
        MovieDetailViewModel viewModel = ViewModelProviders.of(this, factory).get(MovieDetailViewModel.class);

        LiveData<Movie> movieData = viewModel.getMovieData();
        movieData.observe(this, new Observer<Movie>() {
            @Override
            public void onChanged(@Nullable Movie movie) {
                if (movie == null) {
                    closeOnError();
                } else {
                    DetailActivity.this.movie = movie;
                    setTitle(movie.title);
                    Picasso.with(DetailActivity.this).load(movie.image).into(image);
                    overviewView.setText(movie.overview);
                    yearView.setText(movie.releaseDate.substring(0, 4));
                    ratingView.setText(String.format(Locale.US, "%3.1f/10", movie.voteAverage));
                    favoriteCheckBox.setChecked(movie.favorite);

                    // Not interested in updates, especially for our own changes
                    movieData.removeObserver(this);
                }
            }
        });

        // Android does not support ListView inside of ScrollView
        // Recommendation that I think is most clean is to use LinearLayout and add views in code
        LinearLayout trailersView = findViewById(R.id.trailers_view);
        viewModel.getTrailersData().observe(this, trailers -> {
            trailersView.removeViews(0, trailersView.getChildCount());
            if (trailers != null && trailers.size() > 0) {
                for (int i = 0; i < trailers.size(); i++) {
                    Trailer trailer = trailers.get(i);
                    Button trailerView = (Button) LayoutInflater.from(this).inflate(R.layout.trailer_item, trailersView, false);
                    trailerView.setText(trailer.name);
                    trailerView.setOnClickListener(view -> launchTrailer(trailer));
                    trailersView.addView(trailerView);
                }
            }
        });

        LinearLayout reviewsView = findViewById(R.id.reviews_view);
        viewModel.getReviewsData().observe(this, reviews -> {
            reviewsView.removeViews(0, reviewsView.getChildCount());
            if (reviews != null && reviews.size() > 0) {
                for (int i = 0; i < reviews.size(); i++) {
                    Review review = reviews.get(i);
                    TextView reviewView = (TextView) LayoutInflater.from(this).inflate(R.layout.review_item, reviewsView, false);
                    reviewView.setText(review.content);
                    reviewsView.addView(reviewView);
                }
            }
        });
    }

    private void launchTrailer(Trailer trailer) {
        Toast.makeText(this, trailer.name, Toast.LENGTH_SHORT).show();
        //Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + trailer.id));
        Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + trailer.key));
        try {
            startActivity(webIntent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.play_error_message, Toast.LENGTH_SHORT).show();
        }
    }

    private void closeOnError() {
        finish();
        Toast.makeText(this, R.string.detail_error_message, Toast.LENGTH_SHORT).show();
    }

}
