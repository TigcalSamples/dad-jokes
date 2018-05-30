package com.tigcal.dadjokes.util;

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

public class JokeManager {
    private static final String SERVER_URL = "https://icanhazdadjoke.com/graphql";

    private static ApolloClient apolloClient;

    private JokeManager() {
    }

    public static void getJoke(String jokeQuery, final JokeCallback jokeCallback) {
        getClient().query(GetJoke
                .builder()
                .query(jokeQuery)
                .build())
                .enqueue(new ApolloCall.Callback<GetJoke.Data>() {

                    @Override
                    public void onResponse(@Nonnull Response<GetJoke.Data> response) {
                        GetJoke.Joke joke = response.data().joke();
                            jokeCallback.handleSuccess(joke);
                    }

                    @Override
                    public void onFailure(@Nonnull ApolloException e) {
                        jokeCallback.handleFailure(e);
                    }
                });
    }

    private static ApolloClient getClient() {
        if(apolloClient != null) {
            return apolloClient;
        }

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

        return apolloClient;
    }

    public interface JokeCallback {
        void handleSuccess(GetJoke.Joke joke);
        void handleFailure(Exception exception);
    }
}
