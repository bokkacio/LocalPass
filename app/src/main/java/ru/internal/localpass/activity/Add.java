package ru.internal.localpass.activity;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.content.res.Resources;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import ru.internal.localpass.R;
import uilogic.SourceManager;

public class Add extends AppCompatActivity implements View.OnClickListener{

    private EditText txtElementTitle;
    private EditText txtElementValue;
    private TextView lblTitle;
    private TextView lblValue;
    private TextView lblErrorTitle;
    private Button btnElementAdd;

    private Resources _source;
    private long _groupId;
    private boolean _isElement = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add);

        initUiElements();

        Intent intent = getIntent();
        _groupId = intent.getLongExtra(ActivityVariable.GROUP_ID, ActivityVariable.DEFAULT_ID);
        _isElement = intent.getBooleanExtra(ActivityVariable.IS_ELEMENT, false);

        if(_isElement)
        {
            lblTitle.setText(SourceManager.getSourceString(_source, R.string.login_label));
            lblValue.setText(SourceManager.getSourceString(_source, R.string.password_label));
            txtElementTitle.setVisibility(View.VISIBLE);
            lblTitle.setVisibility(View.VISIBLE);
        }
        else
        {
            lblValue.setText(SourceManager.getSourceString(_source, R.string.group_label));
            txtElementTitle.setVisibility(View.INVISIBLE);
            lblTitle.setVisibility(View.INVISIBLE);
        }
    }

    private void initUiElements(){
        lblErrorTitle = (TextView) findViewById(R.id.lblErrorTitle);
        lblTitle = (TextView) findViewById(R.id.lblTitle);
        lblValue = (TextView) findViewById(R.id.lblValue);
        txtElementValue = (EditText)  findViewById(R.id.txtElementValue);
        txtElementTitle = (EditText)  findViewById(R.id.txtElementTitle);
        btnElementAdd = (Button) findViewById(R.id.btnProcessAdding);
        btnElementAdd.setOnClickListener(this);

        _source = getResources();
    }

    @Override
    public void onClick(View v) {
        String elementTitle = txtElementTitle.getText().toString();
        String elementValue = txtElementValue.getText().toString();

        boolean elementTitleFilled =  elementTitle != null && !elementTitle.isEmpty();
        boolean elementValueFilled =  elementValue != null && !elementValue.isEmpty();

        if((elementValueFilled && !_isElement) ||
                (elementTitleFilled && elementValueFilled))
        {
            Intent intent = new Intent(this, Main.class);
            intent.putExtra(ActivityVariable.ELEMENT_VALUE, elementValue);
            intent.putExtra(ActivityVariable.ELEMENT_TITLE, elementTitle);
            intent.putExtra(ActivityVariable.GROUP_ID, _groupId);
            intent.putExtra(ActivityVariable.IS_INSERT, true);
            setResult(RESULT_OK, intent);
            lblErrorTitle.setText("");
            finish();
        }
        else
            lblErrorTitle.setText(_isElement ? SourceManager.getSourceString(_source, R.string.empty_login_or_pass) :  SourceManager.getSourceString(_source, R.string.empty_group));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, EventCodes.CLOSE_APP, 0, SourceManager.getSourceString(_source, R.string.menu_exit));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case EventCodes.CLOSE_APP:
                setResult(EventCodes.CLOSE_APP);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}