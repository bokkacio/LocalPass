package uimodifier;

import android.database.Cursor;
import android.view.View;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.TextView;
import encryption.IScrambler;
import encryption.Scrambler;

public class DecryptViewBinder implements SimpleCursorTreeAdapter.ViewBinder {
    private IScrambler _scrambler = null;

    public DecryptViewBinder(String password)
    {
        _scrambler = new Scrambler(password);
    }

    public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
        if(view instanceof TextView) {
            TextView tv = (TextView) view;
            String encryptedText = cursor.getString(columnIndex);
            tv.setText(_scrambler.decrypt(encryptedText));
            return true;
        }
        return false;
    }
}