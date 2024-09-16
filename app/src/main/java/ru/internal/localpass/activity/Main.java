package ru.internal.localpass.activity;

import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Environment;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import db.ISecureCryptStorage;
import db.SecureCryptStorage;
import encryption.Scrambler;
import file.IPermanentStorage;
import file.PermanentStorage;
import uilogic.SourceManager;
import uimodifier.DecryptViewBinder;
import android.Manifest;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

import ru.internal.localpass.R;

public class Main extends AppCompatActivity {
    private ExpandableListView _passListView;
    private TextView _lblTitle;
    private ISecureCryptStorage _db;
    private SimpleCursorTreeAdapter _sctAdapter;
    private Scrambler _localScrambler;
    private IPermanentStorage _fileHelper;
    private Resources _source;

    private String _dbPassword = "";

    private static final String PASS_PREFIX = "afDGhb,ea";
    private static final String PASS_POSTFIX = "eh;jPJrfv";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        _lblTitle = (TextView) findViewById(R.id.mainActivityTitle);
        _lblTitle.setText("The list of your passwords");

        Intent entireIntent = getIntent();
        setSupportActionBar(findViewById(R.id.toolbar));


        //not from Entrance activity
        if(!entireIntent.hasExtra(ActivityVariable.SAVED_USER_PASSWORD))
            finish();
        else
        {
            _dbPassword = PASS_PREFIX + entireIntent.getStringExtra(ActivityVariable.SAVED_USER_PASSWORD) + PASS_POSTFIX;
            _source = getResources();
            initPasswordList();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        menu.add(0, EventCodes.CREATE_GROUP, 0, SourceManager.getSourceString(_source, R.string.add_group_menu));
        menu.add(0, EventCodes.EXPORT_PASSWORDS, 0, SourceManager.getSourceString(_source, R.string.export_to_file_menu));
        menu.add(0, EventCodes.IMPORT_PASSWORDS, 0, SourceManager.getSourceString(_source, R.string.import_from_file_menu));
        menu.add(0, EventCodes.CLOSE_APP, 0, SourceManager.getSourceString(_source, R.string.menu_exit));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case EventCodes.CREATE_GROUP:
            {
                Intent intent = new Intent(this, Add.class);
                intent.putExtra(ActivityVariable.GROUP_ID, ActivityVariable.DEFAULT_ID);
                startActivityForResult(intent, EventCodes.CREATE_GROUP);
                break;
            }
            case EventCodes.EXPORT_PASSWORDS:
            {
                String exportResult = _fileHelper.savePasswordsToFile();
                Toast.makeText(this, exportResult, Toast.LENGTH_LONG).show();
                break;
            }
            case EventCodes.IMPORT_PASSWORDS:
            {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("text/*");
                startActivityForResult(intent, EventCodes.PICK_PASSWORD_FILE);
                break;
            }
            case EventCodes.CLOSE_APP:
            {
                setResult(EventCodes.CLOSE_APP);
                _db.close();
                finish();
                break;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        ExpandableListView.ExpandableListContextMenuInfo info =
                (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;

        int type =
                ExpandableListView.getPackedPositionType(info.packedPosition);

        if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD)
        {
            menu.add(0, EventCodes.REMOVE_ELEMENT, 0, SourceManager.getSourceString(_source, R.string.remove_element_menu));
        }
        else if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP)
        {
            menu.add(0, EventCodes.CREATE_ELEMENT, 0, SourceManager.getSourceString(_source, R.string.add_element_menu));
            menu.add(0, EventCodes.REMOVE_GROUP, 0, SourceManager.getSourceString(_source, R.string.remove_group_menu));
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        ExpandableListView.ExpandableListContextMenuInfo info =
                (ExpandableListView.ExpandableListContextMenuInfo) item.getMenuInfo();

        int type = ExpandableListView.getPackedPositionType(info.packedPosition);

        if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD && item.getItemId() == EventCodes.REMOVE_ELEMENT) {
            Intent intent = new Intent(this, Delete.class);
            intent.putExtra(ActivityVariable.ELEMENT_ID, info.id);
            startActivityForResult(intent, EventCodes.REMOVE_ELEMENT);
        }
        else if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP && item.getItemId() == EventCodes.CREATE_ELEMENT) {
            Intent intent = new Intent(this, Add.class);
            intent.putExtra(ActivityVariable.GROUP_ID, info.id);
            intent.putExtra(ActivityVariable.IS_ELEMENT, true);
            startActivityForResult(intent, EventCodes.CREATE_ELEMENT);
        }
        else if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP && item.getItemId() == EventCodes.REMOVE_GROUP) {
            Intent intent = new Intent(this, Delete.class);
            intent.putExtra(ActivityVariable.GROUP_ID, info.id);
            startActivityForResult(intent, EventCodes.REMOVE_GROUP);
        }

        return super.onContextItemSelected(item);
    }

    private void initPasswordList()
    {
        _localScrambler = new Scrambler(_dbPassword);
        _db = new SecureCryptStorage(this, _localScrambler);
        _fileHelper = new PermanentStorage(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), _db, _localScrambler);

        _db.open();

        Cursor cursor = _db.getGroupData();
        // сопоставление данных и View для групп
        String[] groupFrom = { SecureCryptStorage.GROUP_COLUMN_NAME };
        int[] groupTo = { android.R.id.text1 };

        // сопоставление данных и View для элементов
        String[] childFrom = { SecureCryptStorage.ELEMENT_COLUMN_VALUE };
        int[] childTo = { android.R.id.text1 };

        // создаем адаптер и настраиваем список
        _sctAdapter = new MyAdapter(this, cursor,
                android.R.layout.simple_expandable_list_item_1, groupFrom,
                groupTo, android.R.layout.simple_list_item_1, childFrom,
                childTo);

        _sctAdapter.setViewBinder(new DecryptViewBinder(_dbPassword));
        _passListView = (ExpandableListView) findViewById(R.id.mainList);
        _passListView.setAdapter(_sctAdapter);

        registerForContextMenu(_passListView);
        return;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Exit menu  was pressed
        if (data == null && resultCode == EventCodes.CLOSE_APP)
        {
            setResult(EventCodes.CLOSE_APP);
            _db.close();
            finish();
            return;
        }

        //Null data
        if (data == null) return;

        boolean isRemove = data.getBooleanExtra(ActivityVariable.IS_REMOVE, false);
        boolean isInsert = data.getBooleanExtra(ActivityVariable.IS_INSERT, false);
        long groupId = data.getLongExtra(ActivityVariable.GROUP_ID, ActivityVariable.DEFAULT_ID);
        long elementId = data.getLongExtra(ActivityVariable.ELEMENT_ID, ActivityVariable.DEFAULT_ID);
        String elementTitle = data.getStringExtra(ActivityVariable.ELEMENT_TITLE);
        String elementValue = data.getStringExtra(ActivityVariable.ELEMENT_VALUE);

        if(resultCode == RESULT_OK && isRemove)
        {
            if(elementId != ActivityVariable.DEFAULT_ID)
            {
                //remove element
                _db.deleteElement(elementId);
                _sctAdapter.notifyDataSetChanged();
            }
            else if(groupId != ActivityVariable.DEFAULT_ID && elementId == ActivityVariable.DEFAULT_ID)
            {
                //remove group
                _db.deleteGroup(groupId);
                initPasswordList();
            }
        }
        else if(resultCode == RESULT_OK && isInsert)
        {
            if(elementValue != "" && groupId == ActivityVariable.DEFAULT_ID)
            {
                //insert group
                _db.insertGroup(elementValue);
                initPasswordList();
            }
            else if(elementValue != "" && groupId != ActivityVariable.DEFAULT_ID)
            {
                //insert element
                _db.insertElement(groupId, elementTitle, elementValue);
                _sctAdapter.notifyDataSetChanged();
            }
        }
        else if(requestCode == EventCodes.PICK_PASSWORD_FILE && resultCode == RESULT_OK)
        {
            Uri uri = data.getData();
            try {
                StringBuilder stringBuilder = new StringBuilder();
                try (InputStream inputStream =
                             this.getContentResolver().openInputStream(uri);
                     BufferedReader reader = new BufferedReader(
                             new InputStreamReader(Objects.requireNonNull(inputStream)))) {
                    _fileHelper.importPasswordsFromFile(reader);
                }
            }
            catch (Exception ex)
            {
            }
            initPasswordList();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    class MyAdapter extends SimpleCursorTreeAdapter {

        public MyAdapter(Context context, Cursor cursor, int groupLayout,
                         String[] groupFrom, int[] groupTo, int childLayout,
                         String[] childFrom, int[] childTo) {
            super(context, cursor, groupLayout, groupFrom, groupTo,
                    childLayout, childFrom, childTo);
        }

        protected Cursor getChildrenCursor(Cursor groupCursor) {
            // получаем курсор по элементам для конкретной группы
            int idColumn = groupCursor.getColumnIndex(SecureCryptStorage.ID_COLUMN_NAME);
            return _db.getElementData(groupCursor.getInt(idColumn));
        }
    }

    protected void onDestroy() {
        _db.close();
        super.onDestroy();
    }
}