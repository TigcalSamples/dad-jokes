package com.tigcal.dadjokes;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.tigcal.dadjokes.graphql.GetJoke;

import java.io.IOException;

import javax.annotation.Nonnull;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final String SERVER_URL = "https://icanhazdadjoke.com/graphql";
    private static final String EMPTY_STRING = "";

    private ProgressBar progressBar;
    private TextView jokeTextView;
    private Button jokeButton;
    private SearchView searchView;

    private ApolloClient apolloClient;

    private String jokeQuery = EMPTY_STRING;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public okhttp3.Response intercept(Chain chain) throws IOException {
                        Request originalRequest = chain.request();
                        Request.Builder builder = originalRequest.newBuilder().method(originalRequest.method(), originalRequest.body());
                        builder.addHeader("User-Agent", "Dad Jokes (https://github.com/jomartigcal/dad-jokes)");
                        return chain.proceed(builder.build());
                    }
                })
                .build();
        apolloClient = ApolloClient.builder()
                .serverUrl(SERVER_URL)
                .okHttpClient(okHttpClient)
                .build();

        progressBar = findViewById(R.id.progress_bar);
        jokeTextView = findViewById(R.id.joke_text_view);

        jokeButton = findViewById(R.id.joke_button);
        jokeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getJoke();
            }
        });

        displaySavedJoke();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        final MenuItem myActionMenuItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) myActionMenuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                jokeQuery = query;
                getJoke();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (EMPTY_STRING.equals(s)) {
                    jokeQuery = EMPTY_STRING;
                }
                return false;
            }
        });

        return true;
    }

    private void displaySavedJoke() {
        String jokePref = getString(R.string.joke);

        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        if (preferences.contains(jokePref)) {
            displayJoke(preferences.getString(jokePref, getString(R.string.joke_tbd)));
        }
    }

    private void getJoke() {
        progressBar.setVisibility(View.VISIBLE);
        jokeTextView.setVisibility(View.INVISIBLE);

        apolloClient.query(GetJoke
                .builder()
                .query(jokeQuery)
                .build())
                .enqueue(new ApolloCall.Callback<GetJoke.Data>() {

                    @Override
                    public void onResponse(@Nonnull Response<GetJoke.Data> response) {
                        GetJoke.Joke joke = response.data().joke();
                        if (joke != null) {
                            Log.d(TAG, joke.toString());
                            displayJoke(joke.joke());
                            saveJoke(joke.joke());
                        } else {
                            displayErrorMessage();
                        }

                    }

                    @Override
                    public void onFailure(@Nonnull ApolloException e) {
                        Log.e(TAG, "Get Joke Error: " + e.getMessage());
                        e.printStackTrace();
                        displayErrorMessage();
                    }
                });
    }

    private void displayJoke(final String joke) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.INVISIBLE);
                jokeTextView.setVisibility(View.VISIBLE);

                jokeTextView.setText(joke);
            }
        });
    }

    private void saveJoke(String joke) {
        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        preferences.edit()
                .putString(getString(R.string.joke), joke)
                .apply();
    }

    private void displayErrorMessage() {
        progressBar.setVisibility(View.INVISIBLE);

        Snackbar.make(jokeButton, "There was an error getting jokes. Please try again.",
                Snackbar.LENGTH_SHORT).show();
    }
}
