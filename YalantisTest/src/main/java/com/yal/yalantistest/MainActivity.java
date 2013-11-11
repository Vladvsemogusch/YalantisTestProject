package com.yal.yalantistest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.util.LruCache;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;

public class MainActivity extends FragmentActivity {
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private String response;
    private JSONArray mJsonArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        new GetAsyncTask(this).execute("http://apitest.yalantis.com/test/");
        DrawerItemClickListener dr = new DrawerItemClickListener();
        mDrawerList.setOnItemClickListener(dr);
        if (savedInstanceState == null)
            dr.selectItem(1, 1L);

    }


    private class DrawerItemClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectItem(position, id);
        }

        private void selectItem(int position, Long id) {
            Fragment fragment = new DetailFragment();
            Bundle args = new Bundle();
            args.putLong("id", id);
            fragment.setArguments(args);
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, fragment)
                    .commit();
            mDrawerList.setItemChecked(position, true);
            mDrawerLayout.closeDrawer(mDrawerList);
        }
    }

    private class GetAsyncTask extends AsyncTask<String, Void, String> {
        private final Context context;

        public GetAsyncTask(Context context) {
            this.context = context;
        }

        protected String doInBackground(String... url) {
            try {
                HttpClient client = new DefaultHttpClient();
                HttpGet get = new HttpGet(url[0]);
                HttpResponse responseGet = client.execute(get);
                HttpEntity resEntityGet = responseGet.getEntity();
                if (resEntityGet != null) {
                    response = EntityUtils.toString(resEntityGet);
                } else throw new Exception();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return response;
        }

        protected void onPostExecute(String result) {
            try {
                JSONObject json = new JSONObject(result);
                mJsonArray = json.getJSONArray("items");
                MyAdapter mAdapter = new MyAdapter(context, mJsonArray);

                mDrawerList.setAdapter(mAdapter);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}


class MyAdapter extends BaseAdapter {
    private final Context mContext;
    private final JSONArray mJsonArray;
    private View rowView;
    private JSONObject j;
    private URL picUrl;
    private JSONObject jObject;
    private long id;
    private LruCache mMemoryCache;
    ViewHolder holder;

    public MyAdapter(Context context, JSONArray jsonArray) {
        mContext = context;
        mJsonArray = jsonArray;
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize);
    }

    public void addBitmapToCache(String key, Bitmap bitmap) {
        if (getBitmapFromCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromCache(String key) {
        return (Bitmap) mMemoryCache.get(key);
    }

    public int getCount() {
        return mJsonArray.length();
    }

    public Object getItem(int position) {
        try {
            j = mJsonArray.getJSONObject(position);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return j;
    }


    public long getItemId(int position) {
        try {
            id = mJsonArray.getJSONObject(position).getInt("id");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return id;
    }

    static class ViewHolder {
        TextView rowTitle;
        TextView rowBody;
        ImageView rowImage;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        try {
            rowView = convertView;
            if (rowView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                rowView = inflater.inflate(R.layout.row_layout, parent, false);
                holder = new ViewHolder();
                holder.rowTitle = (TextView) rowView.findViewById(R.id.row_title);
                holder.rowBody = (TextView) rowView.findViewById(R.id.row_body);
                holder.rowImage = (ImageView) rowView.findViewById(R.id.row_image);
                rowView.setTag(holder);
            } else {
                holder = (ViewHolder) rowView.getTag();
            }
            jObject = mJsonArray.getJSONObject(position);
            picUrl = new URL(jObject.getString("picture"));
            holder.rowImage.setTag(position);
            holder.rowImage.setImageResource(R.drawable.loading);
            new ImageAsyncTask(holder.rowImage).execute(picUrl);
            holder.rowTitle.setText(jObject.getString("title"));
            holder.rowBody.setText(jObject.getString("body"));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return rowView;
    }

    private class ImageAsyncTask extends AsyncTask<URL, Void, Bitmap> {
        ImageView imageView;
        int imageTag;

        public ImageAsyncTask(ImageView imageView) {
            this.imageView = imageView;
            this.imageTag = (Integer) imageView.getTag();
        }

        protected Bitmap doInBackground(URL... url) {
            try {
                Bitmap bmp = getBitmapFromCache(url[0].toString());
                if (bmp == null) {
                    InputStream is = url[0].openConnection().getInputStream();
                    bmp = BitmapFactory.decodeStream(is);
                    addBitmapToCache(url[0].toString(), bmp);
                }

                return bmp;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }


        protected void onPostExecute(Bitmap bmp) {
            try {
                if (bmp == null)
                    imageView.setImageResource(R.drawable.loading);
                else if (imageTag == imageView.getTag()) {
                    imageView.setImageBitmap(bmp);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}