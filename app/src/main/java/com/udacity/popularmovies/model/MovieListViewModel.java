package com.udacity.popularmovies.model;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.udacity.popularmovies.MainActivity;
import com.udacity.popularmovies.R;
import com.udacity.popularmovies.database.MovieDatabase;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MovieListViewModel extends AndroidViewModel {

    // TODO somewhere else in phase 2

    private final String apiKey;
    private final MovieDatabase movieDatabase;
    private final LiveData<MovieList> movieListData;
    private final LiveData<List<Movie>> favoritesData;
    private final MutableLiveData<MovieList> remoteData;
    private String sortOrder;

    public MovieListViewModel(@NonNull Application application) {
        super(application);
        this.apiKey = application.getString(R.string.api_key);
        this.movieDatabase = MovieDatabase.getInstance(application);
        this.favoritesData = movieDatabase.movieDao().loadAllMovies();
        this.remoteData = new MutableLiveData<>();
        // merging local favorites data with remote list
        this.movieListData = Transformations.switchMap(favoritesData, favorites -> {
            LiveData<MovieList> result = Transformations.map(remoteData, remoteList -> {
                if (sortOrder.equals(MainActivity.SORT_FAVORITES)) {
                    // return only favorites
                    return new MovieList(favorites);
                } else {
                    // mark favorites
                    if (remoteList != null && !remoteList.isError()) {
                        Map<String, Movie> favoritesMap = new HashMap<>();
                        if (favorites != null) {
                            for (Movie movie : favorites) {
                                favoritesMap.put(movie.id, movie);
                            }
                        }
                        for (Movie movie : remoteList.getMovies()) {
                            movie.favorite = favoritesMap.containsKey(movie.id);
                        }
                    }
                    return remoteList;
                }
            });
            return result;
        });
    }

    public LiveData<MovieList> getMovieListData() {
        return movieListData;
    }

    public void changeOrder(String sortOrder) {
        if (sortOrder != null && !sortOrder.equals(this.sortOrder)) {
            this.sortOrder = sortOrder;
            refreshMovieList();
        }
    }

    private void refreshMovieList() {
        if (sortOrder.equals(MainActivity.SORT_FAVORITES)) {
            remoteData.setValue(null);
        } else {
            MovieListTask task = new MovieListTask(sortOrder, apiKey, remoteData);
            task.execute();
        }
    }

    class MovieListTask extends AsyncTask<Void, Void, MovieList> {

        private static final String BASE_LIST_URL = "https://api.themoviedb.org/3/movie";
        private static final String BASE_POSTER_URL = "https://image.tmdb.org/t/p/w342";
        private static final String TAG = "MovieListTask";
        private static final int MAX_RESULTS = 50;

        private String type;
        private String apiKey;
        private MutableLiveData<MovieList> liveData;

        MovieListTask(String type, String apiKey, MutableLiveData<MovieList> liveData) {
            this.type = type;
            this.apiKey = apiKey;
            this.liveData = liveData;
        }

        @Override
        protected MovieList doInBackground(Void... voids) {
            String url = BASE_LIST_URL + "/" + type +  "?api_key=" + apiKey;

            MovieList movieList = new MovieList();
            OkHttpClient client = new OkHttpClient();
            try {
                Request request = new Request.Builder()
                        .url(url)
                        .get()
                        .build();

                Log.d(TAG, "Call: " + url);
                Response response = client.newCall(request).execute();
                String json = response.body().string();
                Log.d(TAG, "Got: " + json);

                if (response.isSuccessful()) {
                    JSONObject jsonObject = new JSONObject(json);
                    JSONArray results = jsonObject.getJSONArray("results");
                    int count = Math.min(results.length(), MAX_RESULTS);
                    List<Movie> movies = new ArrayList<>(count);
                    for (int i = 0; i < count; i++) {
                        JSONObject result = results.getJSONObject(i);
                        Movie movie = new Movie();
                        movie.id = result.getString("id");
                        movie.title = result.getString("original_title");
                        movie.image = BASE_POSTER_URL + result.getString("poster_path");
                        movie.overview = result.getString("overview");
                        movie.voteAverage = result.getDouble("vote_average");
                        movie.releaseDate = result.getString("release_date");
                        movies.add(movie);
                    }
                    if (movies.size() > 0) {
                        movieList.setMovies(movies);
                    } else {
                        movieList.setMessage(getApplication().getString(R.string.msg_no_movies));
                    }
                } else {
                    Log.e(TAG, "List movies failed: " + response.message());
                    movieList.setMessage(getApplication().getString(R.string.msg_error, response.message()));
                }
            } catch (IOException e) {
                Log.e(TAG, "List movies failed", e);
                movieList.setMessage(getApplication().getString(R.string.msg_no_connection));
            } catch (Exception e) {
                Log.e(TAG, "List movies failed", e);
                movieList.setMessage(getApplication().getString(R.string.msg_error, e.getMessage()));
            }

            return movieList;
        }

        @Override
        protected void onPostExecute(MovieList movieList) {
            liveData.setValue(movieList);
            // update cache
            if (!movieList.isError()) {
                for (Movie movie : movieList.getMovies()) {
                    MovieCache.putMovie(movie);
                }
            }
        }
    }
}
