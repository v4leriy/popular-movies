package com.udacity.popularmovies;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.GridView;
import android.widget.TextView;

import com.udacity.popularmovies.model.Movie;
import com.udacity.popularmovies.model.MovieListViewModel;
import com.udacity.popularmovies.utils.MovieAdapter;

public class MainActivity extends AppCompatActivity {

    public static final String PREF_SORT_ORDER = "sort_order";
    public static final String SORT_POPULAR = "popular";
    public static final String SORT_TOP_RATED = "top_rated";
    public static final String SORT_FAVORITES = "favorites";

    private MovieListViewModel movieListViewModel;
    private MovieAdapter adapter;
    private SharedPreferences preferences;
    private String sortOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        adapter = new MovieAdapter(this, R.layout.movie_item);

        GridView moviesView = findViewById(R.id.movies_view);
        moviesView.setAdapter(adapter);
        moviesView.setOnItemClickListener((adapterView, view, position, l) -> launchDetailActivity(adapter.getItem(position)));

        final TextView msgView = findViewById(R.id.message_view);
        moviesView.setEmptyView(msgView);

        movieListViewModel = ViewModelProviders.of(this).get(MovieListViewModel.class);
        movieListViewModel.getMovieListData().observe(this, movieList -> {
            adapter.clear();
            if (movieList != null) {
                if (movieList.isError()) {
                    msgView.setText(movieList.getMessage());
                } else {
                    adapter.addAll(movieList.getMovies());
                }
            } else {
                msgView.setText(R.string.msg_no_movies);
            }
        });

        preferences = getPreferences(MODE_PRIVATE);
        changeOrder(preferences.getString(PREF_SORT_ORDER, SORT_POPULAR));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (SORT_POPULAR.equals(sortOrder)) {
            menu.findItem(R.id.menu_popular).setChecked(true);
        } else if (SORT_TOP_RATED.equals(sortOrder)) {
            menu.findItem(R.id.menu_top_rated).setChecked(true);
        } else {
            menu.findItem(R.id.menu_favorites).setChecked(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_popular) {
            item.setChecked(true);
            changeOrder(SORT_POPULAR);
            return true;
        } else if (itemId == R.id.menu_top_rated) {
            item.setChecked(true);
            changeOrder(SORT_TOP_RATED);
            return true;
        } else if (itemId == R.id.menu_favorites) {
            item.setChecked(true);
            changeOrder(SORT_FAVORITES);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void changeOrder(String sortOrder) {
        this.sortOrder = sortOrder;
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PREF_SORT_ORDER, sortOrder);
        editor.apply();

        if (SORT_POPULAR.equals(sortOrder)) {
            setTitle(R.string.title_popular);
        } else if (SORT_TOP_RATED.equals(sortOrder)) {
            setTitle(R.string.title_top_rated);
        } else {
            setTitle(R.string.title_favorite);
        }

        movieListViewModel.changeOrder(sortOrder);
    }

    private void launchDetailActivity(Movie movie) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(DetailActivity.EXTRA_ID, movie.id);
        startActivity(intent);
    }
}
