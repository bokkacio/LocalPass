package db;

import android.database.Cursor;

public interface ISecureCryptStorage {
    Cursor getGroupData();
    Cursor getElementData(long groupID);
    void insertElement(Long elementGroupId, String elementTitle, String elementValue);
    void insertGroup(String groupValue);
    long insertGroupResult(String groupValue);
    void deleteGroup(Long groupId);
    void deleteElement(Long elementId);
    void open();
    void close();

}