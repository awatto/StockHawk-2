package com.sam_chordas.android.stockhawk.data;

import android.net.Uri;

import net.simonvt.schematic.annotation.ContentProvider;
import net.simonvt.schematic.annotation.ContentUri;
import net.simonvt.schematic.annotation.InexactContentUri;
import net.simonvt.schematic.annotation.TableEndpoint;

/**
 * Created by sam_chordas on 10/5/15.
 */
@ContentProvider(authority = StockHawkProvider.AUTHORITY, database = StockHawkDatabase.class)
public class StockHawkProvider {
    public static final String AUTHORITY = "com.sam_chordas.android.stockhawk.data.StockHawkProvider";

    static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    interface Path {
        String QUOTES = "quotes";
        String HISTORICAL_DATA = "historical_data";
    }

    private static Uri buildUri(String... paths) {
        Uri.Builder builder = BASE_CONTENT_URI.buildUpon();
        for (String path : paths) {
            builder.appendPath(path);
        }
        return builder.build();
    }

    @TableEndpoint(table = StockHawkDatabase.QUOTES)
    public static class Quotes {
        @ContentUri(
                path = Path.QUOTES,
                type = "vnd.android.cursor.dir/quote"
        )
        public static final Uri CONTENT_URI = buildUri(Path.QUOTES);

        @InexactContentUri(
                name = "QUOTE_ID",
                path = Path.QUOTES + "/*",
                type = "vnd.android.cursor.item/quote",
                whereColumn = StockHawkContract.QoutesColumns.SYMBOL,
                pathSegment = 1
        )
        public static Uri withSymbol(String symbol) {
            return buildUri(Path.QUOTES, symbol);
        }
    }

    @TableEndpoint(table = StockHawkDatabase.HISTORICAL_DATA)
    public static class HistoricalData {
        @ContentUri(
                path = Path.HISTORICAL_DATA,
                type = "vnd.android.cursor.dir/historical_data"
        )
        public static final Uri CONTENT_URI = buildUri(Path.HISTORICAL_DATA);

        @InexactContentUri(
                name = "HISTORICAL_DATA_ID",
                path = Path.HISTORICAL_DATA + "/*",
                type = "vnd.android.cursor.item/historical_data",
                whereColumn = StockHawkContract.HistoricalDataColumns.SYMBOL,
                pathSegment = 1
        )
        public static Uri withSymbol(String symbol) {
            return buildUri(Path.HISTORICAL_DATA, symbol);
        }
    }

}
