package com.udacity.popularmovies.model;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.udacity.popularmovies.R;
import com.udacity.popularmovies.database.MovieDatabase;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MovieDetailViewModel extends AndroidViewModel {

    private final String apiKey;
    private final LiveData<Movie> movieData;
    private final MutableLiveData<List<Trailer>> trailersData = new MutableLiveData<>();
    private final MutableLiveData<List<Review>> reviewsData = new MutableLiveData<>();

    public MovieDetailViewModel(@NonNull Application application, String movieId) {
        super(application);
        this.apiKey = application.getString(R.string.api_key);

        // Source is not expected to change within lifetime of this model view
        LiveData<Movie> movieData = MovieCache.getMovie(movieId);
        if (movieData == null) {
            MovieDatabase movieDatabase = MovieDatabase.getInstance(application);
            movieData = movieDatabase.movieDao().loadMovieById(movieId);
        }
        this.movieData = movieData;

        TrailersTask trailersTask = new TrailersTask(movieId, apiKey, trailersData);
        trailersTask.execute();

        ReviewsTask reviewsTask = new ReviewsTask(movieId, apiKey, reviewsData);
        reviewsTask.execute();
    }

    public LiveData<Movie> getMovieData() {
        return movieData;
    }

    public LiveData<List<Trailer>> getTrailersData() {
        return trailersData;
    }

    public LiveData<List<Review>> getReviewsData() {
        return reviewsData;
    }

    static class TrailersTask extends AsyncTask<Void, Void, List<Trailer>> {

        private static final String BASE_LIST_URL = "https://api.themoviedb.org/3/movie";
        private static final String TAG = "TrailersTask";
        private static final int MAX_RESULTS = 50;

        private String movieId;
        private String apiKey;
        private MutableLiveData<List<Trailer>> liveData;

        TrailersTask(String movieId, String apiKey, MutableLiveData<List<Trailer>> liveData) {
            this.movieId = movieId;
            this.apiKey = apiKey;
            this.liveData = liveData;
        }

        @Override
        protected List<Trailer> doInBackground(Void... voids) {
            String url = BASE_LIST_URL + "/" + movieId +  "/videos?api_key=" + apiKey;

            List<Trailer> trailers = new ArrayList<>();
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
                    for (int i = 0; i < count; i++) {
                        JSONObject result = results.getJSONObject(i);
                        Trailer trailer = new Trailer();
                        trailer.id = result.getString("id");
                        trailer.key = result.getString("key");
                        trailer.name = result.getString("name");
                        trailer.site = result.getString("site");
                        trailers.add(trailer);
                    }
                } else {
                    Log.e(TAG, "List trailers failed: " + response.message());
                }
            } catch (IOException e) {
                Log.e(TAG, "List trailers failed", e);
            } catch (Exception e) {
                Log.e(TAG, "List trailers failed", e);
            }

            return trailers;
        }

        @Override
        protected void onPostExecute(List<Trailer> trailers) {
            liveData.setValue(trailers);
        }
    }

    static class ReviewsTask extends AsyncTask<Void, Void, List<Review>> {

        private static final String BASE_LIST_URL = "https://api.themoviedb.org/3/movie";
        private static final String TAG = "ReviewsTask";
        private static final int MAX_RESULTS = 50;

        private String movieId;
        private String apiKey;
        private MutableLiveData<List<Review>> liveData;

        ReviewsTask(String movieId, String apiKey, MutableLiveData<List<Review>> liveData) {
            this.movieId = movieId;
            this.apiKey = apiKey;
            this.liveData = liveData;
        }

        @Override
        protected List<Review> doInBackground(Void... voids) {
            String url = BASE_LIST_URL + "/" + movieId +  "/reviews?api_key=" + apiKey;

            List<Review> reviews = new ArrayList<>();
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
                    for (int i = 0; i < count; i++) {
                        JSONObject result = results.getJSONObject(i);
                        Review review = new Review();
                        review.id = result.getString("id");
                        review.author = result.optString("author");
                        review.content = result.getString("content");
                        review.url = result.optString("url");
                        reviews.add(review);
                    }
                } else {
                    Log.e(TAG, "List reviews failed: " + response.message());
                }
            } catch (IOException e) {
                Log.e(TAG, "List reviews failed", e);
            } catch (Exception e) {
                Log.e(TAG, "List reviews failed", e);
            }

            return reviews;
        }

        @Override
        protected void onPostExecute(List<Review> reviews) {
            liveData.setValue(reviews);
        }
    }
}
