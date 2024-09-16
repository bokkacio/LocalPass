package db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import encryption.IScrambler;

public class SecureCryptStorage implements ISecureCryptStorage{
    private static final String DB_NAME = "dbSafe";
    private static final int DB_VERSION = 1;

    public static final String ID_COLUMN_NAME = "_id";

    //Groups
    private static final String GROUP_TABLE_TITLE = "groups";
    public static final String GROUP_COLUMN_NAME = "group_title";
    private static final String GROUP_TABLE_SQL = "create table "
            + GROUP_TABLE_TITLE + "(" + ID_COLUMN_NAME
            + " integer primary key autoincrement, " + GROUP_COLUMN_NAME + " text" + ");";

    //Elements
    private static final String ELEMENT_TABLE_TITLE = "elements";
    public static final String ELEMENT_COLUMN_VALUE= "element_value";
    private static final String ELEMENT_COLUMN_GROUP = "group_id";
    private static final String ELEMENT_TABLE_SQL = "create table "
            + ELEMENT_TABLE_TITLE + "( " + ID_COLUMN_NAME
            + " integer primary key autoincrement, " + ELEMENT_COLUMN_VALUE +  " text, " + ELEMENT_COLUMN_GROUP + " integer" + ");";

    private final Context _activityContext;
    private final IScrambler _scrambler;
    private ContextSqlLiteHelper _sqlLiteHelper;
    private SQLiteDatabase _db;

    public SecureCryptStorage(Context context, IScrambler scrambler){
        _activityContext = context;
        _scrambler = scrambler;
    }


    @Override
    public void open() {
        String[] scripts = {GROUP_TABLE_SQL, ELEMENT_TABLE_SQL};
        _sqlLiteHelper = new ContextSqlLiteHelper(_activityContext, DB_NAME, DB_VERSION, scripts);
        _db = _sqlLiteHelper.getWritableDatabase();
    }

    @Override
    public void close(){
        if (_sqlLiteHelper != null)
            _sqlLiteHelper.close();
    }

    @Override
    public Cursor getGroupData() {
        return _db.query(GROUP_TABLE_TITLE, null, null, null, null, null, null);
    }

    @Override
    public Cursor getElementData(long groupID) {
        return _db.query(ELEMENT_TABLE_TITLE, null, ELEMENT_COLUMN_GROUP + " = "
                + groupID, null, null, null, null);
    }

    @Override
    public void insertElement(Long elementGroupId, String elementTitle, String elementValue)
    {
        ContentValues cv = new ContentValues();
        cv.put(ELEMENT_COLUMN_GROUP, elementGroupId);
        String encryptedValue = _scrambler.encrypt(elementTitle + "  :  " + elementValue);
        cv.put(ELEMENT_COLUMN_VALUE, encryptedValue);
        _db.insert(ELEMENT_TABLE_TITLE, null, cv);
    }

    @Override
    public void insertGroup(String groupValue)
    {
        ContentValues cv = new ContentValues();
        String encryptedValue = _scrambler.encrypt(groupValue);
        cv.put(GROUP_COLUMN_NAME, encryptedValue);
        _db.insert(GROUP_TABLE_TITLE, null, cv);
    }

    @Override
    public long insertGroupResult(String groupValue)
    {
        ContentValues cv = new ContentValues();
        String encryptedValue = _scrambler.encrypt(groupValue);
        cv.put(GROUP_COLUMN_NAME, encryptedValue);
        return _db.insert(GROUP_TABLE_TITLE, null, cv);
    }

    @Override
    public void deleteGroup(Long groupId)
    {
        _db.delete(ELEMENT_TABLE_TITLE, ELEMENT_COLUMN_GROUP + " = " + groupId, null);
        _db.delete(GROUP_TABLE_TITLE, ID_COLUMN_NAME + " = " + groupId, null);
    }

    @Override
    public void deleteElement(Long elementId)
    {
        _db.delete(ELEMENT_TABLE_TITLE, ID_COLUMN_NAME + " = " + elementId, null);
    }
}