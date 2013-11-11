package com.yal.yalantistest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.net.URL;


public class DetailFragment extends Fragment {
    private View root;
    private TextView detailTitle;
    private TextView detailBody;
    private TextView detailAddress;
    private ImageView detailImage;


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Long id = getArguments().getLong("id");

        detailTitle = (TextView) root.findViewById(R.id.detail_title);
        detailBody = (TextView) root.findViewById(R.id.detail_body);
        detailAddress = (TextView) root.findViewById(R.id.detail_address);
        detailImage = (ImageView) root.findViewById(R.id.detail_image);
        if (savedInstanceState != null) {
            detailTitle.setText(savedInstanceState.getString("title"));
            detailBody.setText(savedInstanceState.getString("body"));
            detailAddress.setText(savedInstanceState.getString("address"));
        }
        new GetDataAsyncTask().execute("http://apitest.yalantis.com/test/?id=" + id);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.detail_layout, container, false);
        return root;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("title", detailTitle.getText().toString());
        outState.putString("body", detailBody.getText().toString());
        outState.putString("address", detailAddress.getText().toString());

    }

    private class GetDataAsyncTask extends AsyncTask<String, Void, JSONObject> {
        Bitmap bmp;
        String imageString;


        protected JSONObject doInBackground(String... url) {
            try {
                HttpClient client = new DefaultHttpClient();
                HttpGet get = new HttpGet(url[0]);
                HttpResponse responseGet = client.execute(get);
                HttpEntity resEntityGet = responseGet.getEntity();
                String response;
                if (resEntityGet != null) {
                    response = EntityUtils.toString(resEntityGet);
                } else throw new Exception();
                JSONObject json = new JSONObject(response);
                JSONObject mJsonObject = json.getJSONObject("data");
                imageString = mJsonObject.getString("picture");
                URL imgUrl = new URL(imageString);
                bmp = BitmapFactory.decodeStream(imgUrl.openConnection().getInputStream());
                return mJsonObject;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }


        protected void onPostExecute(JSONObject mJsonObject) {
            try {
                detailTitle.setText(mJsonObject.getString("title"));
                detailBody.setText(mJsonObject.getString("body"));
                detailAddress.setText(mJsonObject.getString("adress"));
                detailImage.setImageBitmap(bmp);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}


