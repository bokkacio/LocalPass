package ru.internal.localpass.activity;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import ru.internal.localpass.R;
import db.EntryStorage;
import db.IEntryStorage;
import encryption.Md5Helper;
import uilogic.ActionResult;
import uilogic.SourceManager;
import uilogic.UserStringValidator;

public class Entrance extends AppCompatActivity implements View.OnClickListener{

    private TextView _lblInfo;
    private TextView _lblAttemptsLeft;
    private Button _btnEntry;
    private EditText _txtPassword;
    private EditText _txtPasswordRepeat;

    private IEntryStorage _db;
    private Resources _source;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entrance);

        initUiElements();

        _db = new EntryStorage(this);
        _db.open();

        setEntranceAppearance();
    }

    //init widgets
    private void initUiElements(){
        _lblInfo = (TextView) findViewById(R.id.lblInfo);
        _lblAttemptsLeft = (TextView) findViewById(R.id.lblAttemptsLeft);
        _txtPassword = (EditText)  findViewById(R.id.txtPassword);
        _txtPasswordRepeat = (EditText)  findViewById(R.id.txtPasswordRepeat);
        _btnEntry = (Button)  findViewById(R.id.btnEntry);

        _btnEntry.setOnClickListener(this);
        _source = getResources();
    }

    //create password or enter
    private void setEntranceAppearance(){
        if(!_db.isFirstTimeEntrance()){
            _txtPasswordRepeat.setVisibility(View.INVISIBLE);
            _lblInfo.setText(SourceManager.getSourceString(_source, R.string.enter_password));
        }
        else{
            _txtPasswordRepeat.setVisibility(View.VISIBLE);
            _lblInfo.setText(SourceManager.getSourceString(_source, R.string.first_time_instruction));
        }
    }

    private void setPasswordFirstTime()
    {
        String userPass = _txtPassword.getText().toString();
        byte encryptedPass[] = Md5Helper.getMd5(userPass);
        _db.setPasswordFirstTime(encryptedPass);
    }

    private void startMainActivity(String userPassword)
    {
        _db.close();
        Intent intent = new Intent(this, Main.class);
        intent.putExtra(ActivityVariable.SAVED_USER_PASSWORD, userPassword);
        startActivityForResult(intent, EventCodes.IMPORT_PASSWORDS);
    }

    @Override
    public void onClick(View v) {
        String userPass = _txtPassword.getText().toString();
        String userPassRepeat = _txtPasswordRepeat.getText().toString();
        int zeroAmount = 0;

        if(!_db.isFirstTimeEntrance())
        {
            byte encryptedPass[] = Md5Helper.getMd5(userPass);
            byte encryptedPassFromDb[] = _db.getEncryptedPassword();
            int attemptsAmount = _db.getAttemptsAmount();
            boolean isPasswordEquals = Md5Helper.compareByteArrays(encryptedPass, encryptedPassFromDb);

            if(!isPasswordEquals)
            {
                attemptsAmount--;

                //Change password to funny and set thousand attempts
                if(attemptsAmount == zeroAmount)
                {
                    String funnyPass = "HO-HO-HO";
                    byte[] bytesFunnyPass = Md5Helper.getMd5(funnyPass);
                    _db.freeMd5Password(bytesFunnyPass);
                    _db.close();
                    finish();
                    return;
                }

                _db.updateAttemptsAmount(attemptsAmount);
                _lblAttemptsLeft.setText(String.format(SourceManager.getSourceString(_source, R.string.password_attempts_left), attemptsAmount));
                _lblInfo.setText(SourceManager.getSourceString(_source, R.string.wrong_password));
                _txtPassword.setText("");
            }
            else
            {
                _db.restoreAttemptsAmount();
                startMainActivity(userPass);
            }
        }
        else{
            ActionResult matchPasswordsResult = UserStringValidator.isPasswordMatchCriteria(userPass, userPassRepeat);
            if(matchPasswordsResult.isValid()){
                setPasswordFirstTime();
                startMainActivity(userPass);
            }
            else{
                _txtPassword.setText("");
                _txtPasswordRepeat.setText("");
                _lblInfo.setText(SourceManager.getSourceString(_source, matchPasswordsResult.getResultCode()));
            }
        }
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
                _db.close();
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Close app in any way
        super.onActivityResult(requestCode,resultCode,data);
        finish();
    }
}