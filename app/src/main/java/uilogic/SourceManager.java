package uilogic;

import android.content.res.Resources;

public final class SourceManager {
    public static String getSourceString(Resources source, int sourceId){
        return source.getString(sourceId);
    }
}
