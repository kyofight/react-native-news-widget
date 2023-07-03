package com.newswidget;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.widget.RemoteViews;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import android.util.Log;

import com.squareup.picasso.Picasso;

public class WidgetService extends Service {
    private final static String NON_THIN = "[^iIl1\\.,']";
    ArrayList<String> descriptions;
    ArrayList<String> links;
    ArrayList<String> titles;
    ArrayList<String> thumbnails;
    int maxRows = 3;
    Intent intent;

    public WidgetService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        descriptions = new ArrayList<>();
        links = new ArrayList<>();
        titles = new ArrayList<>();
        thumbnails = new ArrayList<>();

        this.intent = intent;

        Log.d("Debug", "##### onStartCommand " + startId);


        RemoteViews views = new RemoteViews(intent.getStringExtra("packageName"), R.layout.widget_medium);
        for (int i = 1; i <= maxRows; i++) {

            int titleId = getResources().getIdentifier(
                    "tvTitle" + i,
                    "id",
                    intent.getStringExtra("packageName"));
            int descId = getResources().getIdentifier(
                    "tvDesc" + i,
                    "id",
                    intent.getStringExtra("packageName"));
            views.setTextViewText(titleId, "Busy retrieving data...");
//            views.setTextViewText(descId, "Busy retrieving data...");

        }

        AppWidgetManager.getInstance(getApplicationContext()).updateAppWidget(intent.getIntExtra("appWidgetId", 0), views);

        new GetStoriesInBackground().execute();

        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private InputStream getInputStream(URL url) {
        try {
            return url.openConnection().getInputStream();
        } catch (IOException e) {
            return null;
        }
    }

    private static int textWidth(String str) {
        return (int) (str.length() - str.replaceAll(NON_THIN, "").length() / 2);
    }

    public static String ellipsize(String text, int max) {

        if (textWidth(text) <= max)
            return text;

        // Start by chopping off at the word before max
        // This is an over-approximation due to thin-characters...
        int end = text.lastIndexOf(' ', max - 3);

        // Just one long word. Chop it off.
        if (end == -1)
            return text.substring(0, max-3) + "...";

        // Step forward as long as textWidth allows.
        int newEnd = end;
        do {
            end = newEnd;
            newEnd = text.indexOf(' ', end + 1);

            // No more spaces.
            if (newEnd == -1)
                newEnd = text.length();

        } while (textWidth(text.substring(0, newEnd) + "...") < max);

        return text.substring(0, end) + "...";
    }


    public class GetStoriesInBackground extends AsyncTask<Integer, Integer, String> {

        @Override
        protected String doInBackground(Integer... params) {

            try {

                Log.d("Debug", "Before GetStoriesInBackground");
                URL url = new URL("https://www.thestar.com/content/thestar/feed.RSSManagerServlet.articles.topstories.rss");

                Log.d("Debug", "GetStoriesInBackground");
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(false);
                XmlPullParser xpp = factory.newPullParser();


                xpp.setInput(getInputStream(url), "UTF_8");


                Log.d("Debug", "after getInputStream");
                boolean insideItem = false;

                // Returns the type of current event: START_TAG, END_TAG, etc..
                int eventType = xpp.getEventType();
                Log.d("Debug", "xpp.getEventType() #1 " + eventType);
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    Log.d("Debug", "xpp.getName() #1 " + xpp.getName());
                    if (eventType == XmlPullParser.START_TAG) {

                        Log.d("Debug", "xpp.getName() #2" + xpp.getName());
                        if (xpp.getName().equalsIgnoreCase("item")) {
                            insideItem = true;
                        } else if (xpp.getName().equalsIgnoreCase("title")) {
                            if (insideItem) {
                                titles.add(ellipsize(xpp.nextText(), 80));
                            }
                        } else if (xpp.getName().equalsIgnoreCase("link")) {
                            if (insideItem)
                                links.add(xpp.nextText());
                        } else if (xpp.getName().equalsIgnoreCase("pubDate")) {
                            if (insideItem)
                                descriptions.add(xpp.nextText());
                        } else if (xpp.getName().equalsIgnoreCase("media:thumbnail")) {
                            Log.d("Debug", "media:thumbnail ");
                            if (insideItem)
                                thumbnails.add(xpp.getAttributeValue(null, "url")); //extract the link of article
                        }
                    } else if (eventType == XmlPullParser.END_TAG && xpp.getName().equalsIgnoreCase("item")) {
                        insideItem = false;
                    }

                    eventType = xpp.next();
                }

                Log.d("Debug", "after fetch " + thumbnails);

            } catch (MalformedURLException e) {
                e.printStackTrace();
                Log.d("Debug", "MalformedURLException");
            } catch (XmlPullParserException e) {
                e.printStackTrace();
                Log.d("Debug", "XmlPullParserException");
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("Debug", "IOException");
            }

            return null;

        }


        @Override
        protected void onPostExecute(String result) {

            Log.d("Debug", "onPostExecute");
            int widgetId = intent.getIntExtra("appWidgetId", 0);
            String packageName = intent.getStringExtra("packageName");
            RemoteViews views = new RemoteViews(intent.getStringExtra("packageName"), R.layout.widget_medium);
            Context context = getApplicationContext();

            for (int i = 1; i <= maxRows; i++) {
                int titleId = getResources().getIdentifier(
                        "tvTitle" + i,
                        "id",
                        packageName);
                int descId = getResources().getIdentifier(
                        "tvDesc" + i,
                        "id",
                        packageName);
                views.setTextViewText(titleId, titles.get(i));
                views.setTextViewText(descId, descriptions.get(i));


//            Target target = new Target() {
//                @Override
//                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
//                    Log.d("Debug", "onBitmapLoaded " + from);
//                    views.setImageViewBitmap(R.id.imageView1, bitmap);
//                    AppWidgetManager.getInstance(context).updateAppWidget(intent.getIntExtra("appWidgetId", 0), views);
//                }
//
//                @Override
//                public void onBitmapFailed(Exception e, Drawable errorDrawable) {
//                    Log.d("Debug", "onBitmapFailed " + e);
//                }
//
//                @Override
//                public void onPrepareLoad(Drawable placeHolderDrawable) {
//
//                }
//            };

                Uri urib = Uri.parse(thumbnails.get(i));
                Log.d("Debug", "thumbnails " + urib);
                int widgetIds[] = {widgetId};
                int imageId = getResources().getIdentifier(
                        "imageView" + i,
                        "id",
                        packageName);
                Picasso.get().load(urib).into(views, imageId, widgetIds);

                Uri uri = Uri.parse(links.get(i));
                Intent linkIntent = new Intent(Intent.ACTION_VIEW, uri);

                int newsId = getResources().getIdentifier(
                        "news" + i,
                        "id",
                        packageName);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, linkIntent, PendingIntent.FLAG_MUTABLE);
                views.setOnClickPendingIntent(newsId, pendingIntent);
//
//            PendingIntent pendingIntent1Sync = PendingIntent.getService(getApplicationContext(), 0, intent, PendingIntent.FLAG_MUTABLE);
//            views.setOnClickPendingIntent(R.id.ivSync, pendingIntent1Sync);
            }

            AppWidgetManager.getInstance(context).updateAppWidget(widgetId, views);
        }


    }
}

