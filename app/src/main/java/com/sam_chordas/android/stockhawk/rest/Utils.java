package com.sam_chordas.android.stockhawk.rest;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.sam_chordas.android.stockhawk.data.StockHawkContract;
import com.sam_chordas.android.stockhawk.data.StockHawkProvider;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by sam_chordas on 10/8/15.
 */
public class Utils {

    private static final String LOG_TAG = "stockhawk " + Utils.class.getSimpleName();

    public static boolean showPercent = true;

    /**
     * The formatter used for fetching data from the HistoricalData table from YQL
     */
    public static final SimpleDateFormat YQL_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public static ArrayList quoteJsonToContentVals(String JSON) {
        ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
        JSONObject jsonObject = null;
        JSONArray resultsArray = null;
        try {
            jsonObject = new JSONObject(JSON);
            if (jsonObject != null && jsonObject.length() != 0) {
                jsonObject = jsonObject.getJSONObject("query");
                int count = Integer.parseInt(jsonObject.getString("count"));
                if (count == 1) {
                    jsonObject = jsonObject.getJSONObject("results")
                            .getJSONObject("quote");
                    batchOperations.add(buildBatchOperation(jsonObject));
                } else {
                    resultsArray = jsonObject.getJSONObject("results").getJSONArray("quote");

                    if (resultsArray != null && resultsArray.length() != 0) {
                        for (int i = 0; i < resultsArray.length(); i++) {
                            jsonObject = resultsArray.getJSONObject(i);
                            batchOperations.add(buildBatchOperation(jsonObject));
                        }
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "String to JSON failed: " + e);
        }
        return batchOperations;
    }

    public static ArrayList historicalDataJsonToContentVals(String json) {
        ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
        JSONObject jsonObject = null;
        JSONArray resultsArray = null;
        try {
            jsonObject = new JSONObject(json);
            if (jsonObject != null && jsonObject.length() != 0) {
                jsonObject = jsonObject.getJSONObject("query");
                int count = Integer.parseInt(jsonObject.getString("count"));
                if (count == 1) {
                    jsonObject = jsonObject.getJSONObject("results")
                            .getJSONObject("quote");
                    batchOperations.add(buildBatchOperation(jsonObject));
                } else {
                    resultsArray = jsonObject.getJSONObject("results").getJSONArray("quote");

                    if (resultsArray != null && resultsArray.length() != 0) {
                        for (int i = 0; i < resultsArray.length(); i++) {
                            jsonObject = resultsArray.getJSONObject(i);
                            batchOperations.add(buildBatchOperationHistorical(jsonObject));
                        }
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "String to JSON failed: " + e);
        }
        return batchOperations;
    }

    public static String truncateBidPrice(String bidPrice) {
        bidPrice = String.format("%.2f", Float.parseFloat(bidPrice));
        return bidPrice;
    }

    public static String truncateChange(String change, boolean isPercentChange) {
        String weight = change.substring(0, 1);
        String ampersand = "";
        if (isPercentChange) {
            ampersand = change.substring(change.length() - 1, change.length());
            change = change.substring(0, change.length() - 1);
        }
        change = change.substring(1, change.length());
        double round = (double) Math.round(Double.parseDouble(change) * 100) / 100;
        change = String.format("%.2f", round);
        StringBuffer changeBuffer = new StringBuffer(change);
        changeBuffer.insert(0, weight);
        changeBuffer.append(ampersand);
        change = changeBuffer.toString();
        return change;
    }

    public static ContentProviderOperation buildBatchOperation(JSONObject jsonObject) {
        try {
            if (jsonObject.getString("Name").equals("null")) {
                throw new UnsupportedOperationException("Symbol does not map to a name on yql: "
                        + jsonObject.getString("symbol"));
            }
        } catch (JSONException j) {
            j.printStackTrace();
            Log.e(LOG_TAG, "Unable to verify symbol.  App may crash", j);
        }

        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
                StockHawkProvider.Quotes.CONTENT_URI);
        try {
            String change = jsonObject.getString("Change");
            builder.withValue(StockHawkContract.QoutesColumns.SYMBOL, jsonObject.getString("symbol"));
            builder.withValue(StockHawkContract.QoutesColumns.BIDPRICE, truncateBidPrice(jsonObject.getString("Bid")));
            builder.withValue(StockHawkContract.QoutesColumns.PERCENT_CHANGE, truncateChange(
                    jsonObject.getString("ChangeinPercent"), true));
            builder.withValue(StockHawkContract.QoutesColumns.CHANGE, truncateChange(change, false));
            builder.withValue(StockHawkContract.QoutesColumns.ISCURRENT, 1);
            if (change.charAt(0) == '-') {
                builder.withValue(StockHawkContract.QoutesColumns.ISUP, 0);
            } else {
                builder.withValue(StockHawkContract.QoutesColumns.ISUP, 1);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return builder.build();
    }

    public static ContentProviderOperation buildBatchOperationHistorical(JSONObject jsonObject) {
        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
                StockHawkProvider.HistoricalData.CONTENT_URI);
        try {
            builder.withValue(StockHawkContract.HistoricalDataColumns.SYMBOL, jsonObject.getString("Symbol"));
            builder.withValue(StockHawkContract.HistoricalDataColumns.Date, jsonObject.getString("Date"));
            builder.withValue(StockHawkContract.HistoricalDataColumns.OPEN, jsonObject.getString("Open"));
            builder.withValue(StockHawkContract.HistoricalDataColumns.CLOSE, jsonObject.getString("Close"));
            builder.withValue(StockHawkContract.HistoricalDataColumns.HIGH, jsonObject.getString("High"));
            builder.withValue(StockHawkContract.HistoricalDataColumns.LOW, jsonObject.getString("Low"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return builder.build();
    }

    /**
     * Helper method that clears a table
     *
     * @param uri
     * @param context
     */
    public static void clearTable(Uri uri, Context context) {
        context.getContentResolver().delete(uri, null, null);
    }

    public static String getDate() {
        String newDate = "";

        //GregorianCalendar cal = new GregorianCalendar();

        Date date = new Date();//cal.getTime();


        return YQL_DATE_FORMAT.format(date);

//        cal.add(GregorianCalendar.MONTH, -6);
//        time = cal.getTime();
//        newDate = format.format(time);
//        Log.e(LOG_TAG, "New date: " + newDate);

    }

    /**
     * @param numberOfMonthsAgo
     * @return
     */
    public static String getPastDate(int numberOfMonthsAgo) {
        GregorianCalendar cal = new GregorianCalendar();
        cal.add(GregorianCalendar.MONTH, numberOfMonthsAgo * (-1));

        Date date = cal.getTime();
        return YQL_DATE_FORMAT.format(date);
    }

}
