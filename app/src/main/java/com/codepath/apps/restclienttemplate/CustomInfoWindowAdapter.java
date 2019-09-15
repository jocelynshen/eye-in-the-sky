package com.codepath.apps.restclienttemplate;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;


public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private final View mWindow;
    private Context mContext;
    private Handler mHandler;

    public CustomInfoWindowAdapter(Context context) {
        mContext = context;
        mWindow = LayoutInflater.from(context).inflate(R.layout.image_marker, null);

    }

    /**
     * Grabs the post associated with marker
     *
     * @param marker marker clicked on by user
     * @param v      view
     */
//    public void getPostObject(final Marker marker, final View v) {
//        final ImageView image = v.findViewById(R.id.imageMarker);
//        if (marker.getSnippet().length() > 0) {
//            if (marker.getSnippet().startsWith("*")) {
//                snippet.setVisibility(View.VISIBLE);
//                snippet.setText(marker.getSnippet());
//            } else if (marker.getSnippet().startsWith("tornado")){
//                snippet.setVisibility(View.VISIBLE);
//                snippet.setText("Tornado vortex signature: \nIndicates likely presence of a forming tornado");
//            }
//            else {
//                snippet.setVisibility(View.INVISIBLE);
//                image.setVisibility(View.VISIBLE);
//                Glide.with(mContext).load(marker.getSnippet()).placeholder(R.drawable.ic_add_photo).listener(new RequestListener<Drawable>() {
//                    @Override
//                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
//                        return false;
//                    }
//
//                    @Override
//                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
//                        if (marker.getSnippet().equals(v.getTag())) {
//                            return false;
//                        }
//
//                        mHandler.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                v.setTag(marker.getSnippet());
//                                System.out.println(marker.getSnippet());
//                                marker.showInfoWindow();
//                            }
//                        });
//
//                        return false;
//                    }
//                }).into(image);
//            }
//        } else {
//            image.setVisibility(View.INVISIBLE);
//        }
//        String title = marker.getTitle();
//        System.out.println(title);
//        TextView tvTitle = v.findViewById(R.id.eventTitle);
//        if (title != null && title.length() > 0) {
//            tvTitle.setText(title);
//        } else {
//            tvTitle.setVisibility(View.INVISIBLE);
//        }
//    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(final Marker marker) {
        try {
            mHandler = new Handler();

            final String snippet = marker.getSnippet();
            final String title = marker.getTitle();
            final ImageView image = mWindow.findViewById(R.id.imageMarker);
            final TextView snippetView = mWindow.findViewById(R.id.snip);
            final TextView tvTitle = mWindow.findViewById(R.id.eventTitle);
            snippetView.setVisibility(View.INVISIBLE);
            tvTitle.setVisibility(View.INVISIBLE);
            image.setVisibility(View.INVISIBLE);

            if (snippet.length() > 0) {
                if (snippet.startsWith("*")) {
                    snippetView.setVisibility(View.VISIBLE);
                    image.setVisibility(View.INVISIBLE);
                    snippetView.setText(snippet);
                } else if (snippet.startsWith("tornado")){
                    snippetView.setVisibility(View.VISIBLE);
                    image.setVisibility(View.INVISIBLE);
                    snippetView.setText("Tornado vortex signature: \nIndicates likely presence of a forming tornado");
                }
                else {
                    snippetView.setVisibility(View.INVISIBLE);
                    image.setVisibility(View.VISIBLE);
                    Glide.with(mContext).load(marker.getSnippet()).placeholder(R.drawable.ic_add_photo).listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            if (marker.getSnippet().equals(mWindow.getTag())) {
                                return false;
                            }

                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    mWindow.setTag(marker.getSnippet());
                                    System.out.println(marker.getSnippet());
                                    marker.showInfoWindow();
                                }
                            });

                            return false;
                        }
                    }).into(image);
                }
            } else {
                image.setVisibility(View.INVISIBLE);
            }

            if (title != null && title.length() > 0) {
                tvTitle.setVisibility(View.VISIBLE);
                tvTitle.setText(title);
            } else {
                tvTitle.setVisibility(View.INVISIBLE);
            }
        } catch (Exception e) {
        }
        return mWindow;
    }
}
