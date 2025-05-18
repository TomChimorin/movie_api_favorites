package com.example.assignment3.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.assignment3.BuildConfig;
import com.example.assignment3.model.FirebaseMovie;
import com.example.assignment3.utils.ApiClient;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MovieViewModel extends ViewModel {
    // LiveData for API search results
    private final MutableLiveData<List<FirebaseMovie>> movieData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Integer> currentPage = new MutableLiveData<>(1);
    private final MutableLiveData<Integer> totalPages = new MutableLiveData<>();

    // LiveData for Firebase favorites
    private final MutableLiveData<List<FirebaseMovie>> favorites = new MutableLiveData<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // API Search methods
    public LiveData<List<FirebaseMovie>> getMovieData() {
        return movieData;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Integer> getCurrentPage() {
        return currentPage;
    }

    public LiveData<Integer> getTotalPages() {
        return totalPages;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    // Favorites methods
    public LiveData<List<FirebaseMovie>> getFavorites() {
        return favorites;
    }

    public void searchMovie(String movieTitle, int page) {
        if (movieTitle == null || movieTitle.isEmpty()) {
            errorMessage.postValue("Please enter a movie title");
            return;
        }

        isLoading.postValue(true);
        currentPage.postValue(page);

        try {
            String encodedTitle = URLEncoder.encode(movieTitle, "UTF-8");
            String urlString = String.format(
                    "https://www.omdbapi.com/?s=%s&page=%d&apikey=%s&r=json&maxResults=100",
                    encodedTitle, page, BuildConfig.OMDB_API_KEY
            );

            Log.d("MovieViewModel", "Request URL: " + urlString);

            ApiClient.get(urlString, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    handleApiFailure("API call failed: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        handleApiFailure("API call failed with code: " + response.code());
                        return;
                    }

                    try {
                        String responseData = response.body().string();
                        JSONObject json = new JSONObject(responseData);

                        if (json.has("Error")) {
                            handleApiError(json.getString("Error"));
                            return;
                        }

                        processSearchResults(json);
                    } catch (JSONException e) {
                        handleApiFailure("JSON Parsing error: " + e.getMessage());
                    }
                }
            });
        } catch (UnsupportedEncodingException e) {
            handleApiFailure("URL encoding error: " + e.getMessage());
        }
    }

    private void processSearchResults(JSONObject json) throws JSONException {
        int totalResults = Integer.parseInt(json.getString("totalResults"));
        totalPages.postValue((int) Math.ceil(totalResults / 10.0));

        JSONArray searchResults = json.getJSONArray("Search");
        List<FirebaseMovie> moviesList = new ArrayList<>();

        for (int i = 0; i < Math.min(searchResults.length(), 10); i++) {
            JSONObject movieJson = searchResults.getJSONObject(i);
            fetchMovieDetails(movieJson.getString("imdbID"), moviesList);
        }

        isLoading.postValue(false);
    }

    private void fetchMovieDetails(String imdbID, List<FirebaseMovie> moviesList) {
        String detailsUrl = "https://www.omdbapi.com/?i=" + imdbID + "&apikey=" + BuildConfig.OMDB_API_KEY;

        ApiClient.get(detailsUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("MovieViewModel", "Details API call failed: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e("MovieViewModel", "Details API call failed with code: " + response.code());
                    return;
                }

                try {
                    JSONObject detailsJson = new JSONObject(response.body().string());
                    FirebaseMovie movie = parseMovieDetails(detailsJson);
                    moviesList.add(movie);
                    movieData.postValue(moviesList);
                } catch (JSONException e) {
                    Log.e("MovieViewModel", "JSON Parsing error: " + e.getMessage());
                }
            }
        });
    }

    private FirebaseMovie parseMovieDetails(JSONObject detailsJson) throws JSONException {
        String title = detailsJson.getString("Title");
        String year = detailsJson.getString("Year");
        String studio = detailsJson.optString("Production", "Unknown Studio");
        String plot = detailsJson.optString("Plot", "No plot available");
        String posterURL = detailsJson.optString("Poster", "");

        double rating = 0.0;

        if (detailsJson.has("Ratings")) {
            try {
                JSONObject firstRating = detailsJson.getJSONArray("Ratings").getJSONObject(0);
                String value = firstRating.getString("Value").split("/")[0]; // e.g., "8.5/10"
                rating = Double.parseDouble(value);
            } catch (Exception e) {
                rating = 0.0;
            }
        }

        return new FirebaseMovie(title, studio, plot, rating, posterURL, year);    }

    private void handleApiFailure(String error) {
        Log.e("MovieViewModel", error);
        isLoading.postValue(false);
        errorMessage.postValue("Failed to retrieve data. Please try again.");
    }

    private void handleApiError(String error) {
        Log.e("OMDB_ERROR", error);
        isLoading.postValue(false);
        errorMessage.postValue(error);
        movieData.postValue(new ArrayList<>());
    }

    // Firebase Favorites methods
    public void loadFavorites(String userId) {
        if (userId == null || userId.isEmpty()) {
            return;
        }

        db.collection("users").document(userId).collection("movies")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<FirebaseMovie> movies = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            FirebaseMovie movie = document.toObject(FirebaseMovie.class);
                            movie.setId(document.getId());
                            movies.add(movie);
                        }
                        favorites.postValue(movies);
                    } else {
                        Log.e("Firestore", "Error getting favorites", task.getException());
                    }
                });
    }

    public void addFavorite(String userId, FirebaseMovie movie) {
        db.collection("users").document(userId).collection("movies")
                .add(movie)
                .addOnSuccessListener(documentReference -> {
                    movie.setId(documentReference.getId());
                    updateFavoritesList(movie, true);
                })
                .addOnFailureListener(e ->
                        Log.e("Firestore", "Error adding favorite", e));
    }

    public void updateFavorite(String userId, FirebaseMovie movie) {
        db.collection("users").document(userId).collection("movies")
                .document(movie.getId())
                .set(movie)
                .addOnSuccessListener(aVoid -> updateFavoritesList(movie, false))
                .addOnFailureListener(e ->
                        Log.e("Firestore", "Error updating favorite", e));
    }

    public void deleteFavorite(String userId, String movieId) {
        db.collection("users").document(userId).collection("movies")
                .document(movieId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    List<FirebaseMovie> current = favorites.getValue();
                    if (current != null) {
                        current.removeIf(movie -> movie.getId().equals(movieId));
                        favorites.postValue(current);
                    }
                })
                .addOnFailureListener(e ->
                        Log.e("Firestore", "Error deleting favorite", e));
    }

    private void updateFavoritesList(FirebaseMovie updatedMovie, boolean isNew) {
        List<FirebaseMovie> current = favorites.getValue();
        if (current == null) {
            current = new ArrayList<>();
        }

        if (isNew) {
            current.add(updatedMovie);
        } else {
            for (int i = 0; i < current.size(); i++) {
                if (current.get(i).getId().equals(updatedMovie.getId())) {
                    current.set(i, updatedMovie);
                    break;
                }
            }
        }

        favorites.postValue(current);
    }
}