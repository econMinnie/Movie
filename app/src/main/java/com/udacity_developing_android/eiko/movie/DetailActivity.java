package com.udacity_developing_android.eiko.movie;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by eiko on 8/12/2017.
 */

public class DetailActivity extends Activity {
    ContentValues contentValues = new ContentValues();
    boolean favoriteStatus = false;
    private ImageView imageview;
    private TextView tv_title, tv_releasedate, tv_rate, tv_summery;
    private ToggleButton favoriteButton;
    private List<String> trailerListkey = new ArrayList<>();
    private List<String> trailerName = new ArrayList<>();
    private List<String> reviewList = new ArrayList<>();
    private String API_KEY = "API";
    private String URL_BASE = "http://api.themoviedb.org/3/movie/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detailview);

        imageview = (ImageView) findViewById(R.id.poster);
        tv_title = (TextView) findViewById(R.id.title);
        tv_releasedate = (TextView) findViewById(R.id.date);
        tv_rate = (TextView) findViewById(R.id.rating);
        tv_summery = (TextView) findViewById(R.id.summery);
        favoriteButton = (ToggleButton) findViewById(R.id.img_button);

        favoriteStatus = favorite();

        tv_title.setText(getIntent().getExtras().getString("title"));
        tv_releasedate.setText(getIntent().getExtras().getString("release_date"));
        tv_rate.setText(getIntent().getExtras().getString("vote_average"));
        tv_summery.setText(getIntent().getExtras().getString("overview"));

        favoriteButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    saveFavorite();
                    favoriteStatus = true;
                    Toast.makeText(DetailActivity.this,
                            "Saved to Favorite", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(DetailActivity.this,
                            "unckecked Favorite", Toast.LENGTH_LONG).show();
//                    deleteFavorite();
                }
            }
        });

        String image = getIntent().getStringExtra("poster_path");
        Picasso.with(this).load(image).into(imageview);

        this.excuteTrailer();
        this.excuteReviews();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public void saveFavorite() {
        int theid = getIntent().getExtras().getInt("id");
        String idmovie = String.valueOf(theid);
        String mTitle = getIntent().getExtras().getString("title");
        String mPoster = getIntent().getExtras().getString("poster_path");
        String mOverview = getIntent().getExtras().getString("overview");
        String mReleaseDate = getIntent().getExtras().getString("release_date");
        String mRating = getIntent().getExtras().getString("vote_average");

        contentValues.put(Contract.Entry.COLUMN_MOVIE_ID, idmovie);
        contentValues.put(Contract.Entry.COLUMN_TITLE, mTitle);
        contentValues.put(Contract.Entry.COLUMN_POSTER, mPoster);
        contentValues.put(Contract.Entry.COLUMN_OVERVIEW, mOverview);
        contentValues.put(Contract.Entry.COLUMN_RATING, mRating);
        contentValues.put(Contract.Entry.COLUMN_RELEASEDATE, mReleaseDate);
        contentValues.put(Contract.Entry.COLUMN_FAVORITE_OR_NOT, "Y");

        Log.v("checking", contentValues.toString());
        Log.v("insert id", idmovie);
        Log.v("savefav()", String.valueOf(favoriteStatus));
        if (favoriteStatus) {
            String selectionExists = Contract.Entry.COLUMN_MOVIE_ID + "=?";
            String[] selectionArgsExists = {idmovie.toString()};
            getContentResolver().update(Contract.Entry.CONTENT_URI,
                    contentValues, selectionExists, selectionArgsExists);
        } else {
            Uri savefavUri = getContentResolver().insert(
                    Contract.Entry.CONTENT_URI, contentValues);
            Log.v("Detail,saveFavorite()", savefavUri.toString());
        }
    }

    public boolean favorite() {
        Log.v("Checking fav status", String.valueOf(favoriteStatus));
        Cursor cursor = null;
        int id = getIntent().getExtras().getInt("id");
        String selection = Contract.Entry.COLUMN_MOVIE_ID + "=?";
        String[] projection = {Contract.Entry.COLUMN_FAVORITE_OR_NOT};
        String[] selectionArgs = {String.valueOf(id)};
        cursor = getContentResolver().query(Contract.Entry.CONTENT_URI,
                projection, selection, selectionArgs, null);
        Log.v("fav()", String.valueOf(cursor.getCount()));
        Log.v("fav()", String.valueOf(favoriteStatus));
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            Log.v("fav()", String.valueOf(cursor.getString(cursor.getColumnIndex
                    ("favoriteornot"))));

            if (cursor.getString(cursor.getColumnIndex
                    ("favoriteornot")).equals("Y")) {
                Log.v("fav()", "checking.");
                favoriteButton.setChecked(true);
                cursor.close();
                return true;
            }
        }
        cursor.close();
        favoriteButton.setChecked(false);
        return false;
    }

    private void excuteTrailer() {
        FetchTrailer fetchTrailer = new FetchTrailer();
        fetchTrailer.execute();
    }

    private void excuteReviews() {
        FetchReview fetchreview = new FetchReview();
        fetchreview.execute();
    }

    private class FetchTrailer extends AsyncTask<String, Void, String> {
        int mid = getIntent().getExtras().getInt("id");
        String idString = String.valueOf(mid);

        private void getJsonData(String json) throws JSONException {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject.getJSONArray("results");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject object = jsonArray.getJSONObject(i);
                if (object.getString("site").contentEquals("YouTube")) {
                    trailerListkey.add(i, object.getString("key"));
                    trailerName.add(i, object.getString("name"));
                }
            }
        }

        @Override
        protected String doInBackground(String... params) {

            HttpURLConnection httpUrlConnection = null;
            BufferedReader bufferReader = null;
            String dataString = null;
            try {
                final String URL_BASE = "http://api.themoviedb.org/3/movie/"
                        + idString + "/videos";
                Uri uri = Uri.parse(URL_BASE).buildUpon().appendQueryParameter(
                        "api_key", API_KEY).build();
                URL url = new URL(uri.toString());
                Log.i("Detail:doInbackground", " trailer url " + url);

                httpUrlConnection = (HttpURLConnection) url.openConnection();
                httpUrlConnection.setRequestMethod("GET");
                httpUrlConnection.connect();
                InputStream inputStream = httpUrlConnection.getInputStream();
                StringBuilder stringBuilder = new StringBuilder();
                if (inputStream == null) {
                    return null;
                }
                bufferReader = new BufferedReader(new InputStreamReader(
                        inputStream));
                String string;
                while ((string = bufferReader.readLine()) != null) {
                    stringBuilder.append(string).append("\n");
                }
                dataString = stringBuilder.toString();
                getJsonData(dataString);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            final ArrayList<Trailer> trailerArrayList = new ArrayList<>();
            final String youtubeUrl = "https://www.youtube.com/watch?v=";
            for (int i = 0; i < trailerListkey.size(); i++) {
                Trailer trailer = new Trailer();
                trailer.key = trailerListkey.get(i);
                trailer.name = trailerName.get(i);
                trailerArrayList.add(trailer);
            }
            Log.i("Detail onPostExecute ", "trailer url" +
                    youtubeUrl + trailerListkey);
            Log.i("Detail onPostExecute ", "trailer name" + trailerName);
            RecyclerView recyclerView =
                    (RecyclerView) findViewById(R.id.trailerrecyclerview);
            final TrailerAdapter trailerAdapter = new TrailerAdapter(
//                    getApplicationContext() <-- this makes crush
                    DetailActivity.this, trailerArrayList);
            recyclerView.setAdapter(trailerAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(
                    DetailActivity.this));
        }
    }

    private class FetchReview extends AsyncTask<String, Void, String> {
        int mIdReview = getIntent().getExtras().getInt("id");
        String stringIdReview = String.valueOf(mIdReview);

        private void getJsonDataRreview(String json) throws JSONException {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject.getJSONArray("results");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject object = jsonArray.getJSONObject(i);
                reviewList.add(i, object.getString("content"));
                Log.i("In Review", reviewList.get(i));
            }
        }

        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection httpURLConnection;
            BufferedReader bufferedReader;
            String dataString;
            Log.v("Review ID ", stringIdReview);
            try {
                String URL_BASE_REVIEW = URL_BASE + stringIdReview + "/reviews";
                Uri uri = Uri.parse(URL_BASE_REVIEW).buildUpon().appendQueryParameter(
                        "api_key", API_KEY).build();
                URL url = new URL(uri.toString());
                Log.i("Review URL", String.valueOf(url));
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.connect();
                InputStream inputStream = httpURLConnection.getInputStream();
                StringBuilder stringBuilder = new StringBuilder();
                if (inputStream == null) {
                    return null;
                }
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String string;
                while ((string = bufferedReader.readLine()) != null) {
                    stringBuilder.append(string).append("\n");
                }
                dataString = stringBuilder.toString();
                getJsonDataRreview(dataString);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            final ArrayList<Review> reviewsArrayList = new ArrayList<>();
            for (int i = 0; i < reviewList.size(); i++) {
                Review review = new Review();
                review.content = reviewList.get(i);
                reviewsArrayList.add(review);
            }
            RecyclerView recyclerView = (RecyclerView) findViewById(R.id.review_recyclerview);
            final ReviewAdapter reviewAdapter = new ReviewAdapter(
                    getApplicationContext(), reviewsArrayList);
            recyclerView.setAdapter(reviewAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(
                    DetailActivity.this));
        }
    }
}
