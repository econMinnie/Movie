package com.udacity_developing_android.eiko.movie;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

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

public class MainActivity extends AppCompatActivity {

    ImageAdapter mImageAdapter;
    ArrayList<Poster> mGridImage;
    private GridView gridview;
    String SORT_CHOSEN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gridview);

        gridview = (GridView) findViewById(R.id.gridview);
        mGridImage = new ArrayList<>();
        mImageAdapter = new ImageAdapter(this,
                R.layout.image_grid, mGridImage);
        gridview.setAdapter(mImageAdapter);
        new AsyncHttpTask().execute();
        gridview.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Poster current = mImageAdapter.getItem(position);
//                String item = mGridImage.get(position).getTitle();

                Intent intent = new Intent(MainActivity.this,
                        DetailActivity.class);
                intent.putExtra("poster_path", current.getImage());
                intent.putExtra("title", current.getTitle());
                intent.putExtra("release_date", current.getReleaseDate());
                intent.putExtra("vote_average", current.getVoteAverage());
                intent.putExtra("overview", current.getOverview());
                startActivity(intent);
                Log.v("MainActivity","poster clicked");
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflator = getMenuInflater();
        menuInflator.inflate(R.menu.sort_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.sort_popular:
                SORT_CHOSEN = "popular";
                new AsyncHttpTask().execute();
                return true;
            case R.id.sort_top_rated:
                SORT_CHOSEN = "top_rated";
                new AsyncHttpTask().execute();
                return true;
//            default:
//                super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    public class AsyncHttpTask extends AsyncTask<Void,
            Void, Poster[]> {
        public String SCHEME = "http";
        public String URL_BASE = "api.themoviedb.org";
        public String PATH_ONE = "3";
        public String PATH_TWO = "movie";
        public String IMAGE_PATH = "http://image.tmdb.org/t/p/w185";
        public String API_KEY = "API key";
        String SORT_POPULAR = "popular";
        String SORT_TOPRATED = "top_rated";

        @Override
        protected Poster[] doInBackground(Void... params) {
            HttpURLConnection urlConnect = null;
            BufferedReader buffReader = null;
            String result = null;
            Uri uri = null;
            try {
                if (SORT_CHOSEN == null) {
                     uri = new Uri.Builder()
                            .scheme(SCHEME)
                            .authority(URL_BASE)
                            .appendPath(PATH_ONE)
                            .appendPath(PATH_TWO)
                            .appendPath(SORT_POPULAR)
                            .appendQueryParameter("api_key", API_KEY)
                            .build();
                }else {
                    uri = new Uri.Builder()
                        .scheme(SCHEME)
                        .authority(URL_BASE)
                        .appendPath(PATH_ONE)
                        .appendPath(PATH_TWO)
                        .appendPath(SORT_CHOSEN)
                        .appendQueryParameter("api_key", API_KEY)
                        .build();
                }

                String makeUrl = uri.toString();
                Log.v("MainActivity", makeUrl);
                URL url = new URL(makeUrl);
                urlConnect = (HttpURLConnection) url.openConnection();
                urlConnect.setRequestMethod("GET");
                urlConnect.connect();
                InputStream is = urlConnect.getInputStream();
                StringBuffer sb = new StringBuffer();
                buffReader = new BufferedReader(
                        new InputStreamReader(is));
                String line;
                while ((line = buffReader.readLine()) != null) {
                    sb.append(line + "\n");
                    Log.v("line ", line);
                }
                if (sb.length() == 0) {
                    return null;
                }
                result = sb.toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                return catchingData(result);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.v("catchingData", "JSON exception", e);
                return null;
            }
        }

        private Poster[] catchingData(String result) throws
                JSONException {
            String RESULTS = "results";
            String POSTER_PATH = "poster_path";
            String OVERVIEW = "overview";
            String TITLE = "title";
            String RELEASE_DATE = "release_date";
            String VOTE_AVERAGE = "vote_average";
            String ID = "id";

            Poster item;

            JSONObject fetchJSON = new JSONObject(result);
            JSONArray jsonArray = fetchJSON.getJSONArray(RESULTS);
            Poster[] movieArray = new Poster[jsonArray.length()];
            for (int i = 0; i < movieArray.length; ++i) {
                JSONObject getJSon = jsonArray.getJSONObject(i);
                String title = getJSon.getString(TITLE);
                String releasedate = getJSon.getString(RELEASE_DATE);
                String rate = getJSon.getString(VOTE_AVERAGE);
                String overview = getJSon.getString(OVERVIEW);
                int id = getJSon.getInt(ID);
//                item.setTitle(title);
//                Uri imagepath = new Uri.Builder().scheme(SCHEME)
//                        .authority(IMAGE)
//                        .appendEncodedPath(getJSon.getString(POSTER_PATH))
//                        .build();
                String poster_path = getJSon.getString(POSTER_PATH);
                String poster_url = IMAGE_PATH + poster_path;
                Log.v("MainActivity", poster_url);
                movieArray[i] = new Poster(title,
                        releasedate, rate, overview,
                        poster_url, id);
            }
            Log.v("Mainactivity", String.valueOf(movieArray));
            return movieArray;
        }

        @Override
        protected void onPostExecute(Poster[] posters) {
//            if (posters != null){
//                mImageAdapter = new ImageAdapter(getAc(),
//                        Arrays.asList(posters));
//            }
//            gridview.setAdapter(mImageAdapter);
//            mImageAdapter.setGridData(posters);
//            mImageAdapter.notifyDataSetChanged();
            if (posters != null) {
                for (int p = 0; p < posters.length; ++p) {
                    mGridImage.add(p, posters[p]);
                }
            }
            mImageAdapter.notifyDataSetChanged();
        }
    }
}
