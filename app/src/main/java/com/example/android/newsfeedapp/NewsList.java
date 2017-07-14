package com.example.android.newsfeedapp;


public class NewsList {
    private String mWebTitle;
    private String mSectionName;
    private String mWebPublicationDate;
    private String mWebUrl;
    private String mAuthor;
    private String mTrailText;

    /**
     * Constructor
     *
     * @param webtitle           title of news
     * @param sectionName        name of section
     * @param webPublicationDate publication date
     * @param weburl             url of webpage
     * @param author             name of author
     * @param trailText          short description
     */
    public NewsList(String webtitle, String sectionName, String webPublicationDate, String weburl, String author, String trailText) {
        mWebTitle = webtitle;
        mSectionName = sectionName;
        mWebPublicationDate = webPublicationDate;
        mWebUrl = weburl;
        mAuthor = author;
        mTrailText = trailText;
    }

    //getter
    public String getWebTitle() {
        return mWebTitle;
    }

    public String getSectionName() {
        return mSectionName;
    }

    public String getWebPublicationDate() {
        return mWebPublicationDate;
    }

    public String getWebUrl() {
        return mWebUrl;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public String getTrailText() {
        return mTrailText;
    }

    //auto-generated

    @Override
    public String toString() {
        return "NewsList{" +
                "mWebTitle='" + mWebTitle + '\'' +
                ", mSectionName='" + mSectionName + '\'' +
                ", mWebPublicationDate='" + mWebPublicationDate + '\'' +
                ", mWebUrl='" + mWebUrl + '\'' +
                ", mAuthor='" + mAuthor + '\'' +
                ", mTrailText='" + mTrailText + '\'' +
                '}';
    }
}
