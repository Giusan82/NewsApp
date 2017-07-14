package com.example.android.newsfeedapp;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import java.util.ArrayList;
import java.util.Locale;

public class NewsListAdapter extends ArrayAdapter<NewsList> {
    public static final String LOG_TAG = NewsListAdapter.class.getName();
    //Constructor of ListAdapter
    public NewsListAdapter(Activity context, ArrayList<NewsList> items) {
        // The second argument is used when the ArrayAdapter is populating and it can be any value. So it is used 0.
        super(context, 0, items);
    }
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listView = convertView;
        if (listView == null) {
            listView = LayoutInflater.from(getContext()).inflate(R.layout.list, parent, false);
        }
        //this gets the item position
        if (position < getCount()) {
            NewsList currentItem = getItem(position);
            //this displays the title of news in the list
            TextView tw_title = (TextView) listView.findViewById(R.id.title_text_view);
            tw_title.setText(currentItem.getWebTitle());
            //this displays the authors name
            TextView tw_authors = (TextView) listView.findViewById(R.id.author_text_view);
            tw_authors.setText(currentItem.getAuthor());
            //this displays the date of publication
            TextView tw_date = (TextView) listView.findViewById(R.id.publishedDate);
            tw_date.setText(formatDate(currentItem.getWebPublicationDate()));
            //this displays the section
            TextView tw_section = (TextView) listView.findViewById((R.id.section_text_view));
            tw_section.setText(currentItem.getSectionName());
            //this display the description
            TextView tw_desc = (TextView) listView.findViewById(R.id.desc_text_view);
            tw_desc.setText(Html.fromHtml(currentItem.getTrailText()));
        }
        return listView;
    }
    public String formatDate(String date){
        String newFormatData = "";
        if (date.length() >= 10) {
            // Splits the string after 10 char, because the date obtained from server is like this "2017-07-15T21:30:35Z", so this method will give 2017-07-15
            CharSequence splittedDate = date.subSequence(0, 10);
            try{
                Date formatDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(splittedDate.toString());
                newFormatData = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(formatDate);
            }catch (ParseException e){
                Log.e(LOG_TAG, e.getMessage());
            }
        }else{
            newFormatData = date;
        }

        return newFormatData;
    }
}
