package file;

import android.database.Cursor;
import android.os.Environment;
import android.text.format.Time;
import db.ISecureCryptStorage;
import db.SecureCryptStorage;
import encryption.IScrambler;

import java.io.*;

public class PermanentStorage implements IPermanentStorage {

    public static final String FilePrefix="LocalPass_";
    private final File _currentDirectory;
    private final ISecureCryptStorage _secureStorage;
    private final IScrambler _scrambler;

    public PermanentStorage(File directory, ISecureCryptStorage secureStorage, IScrambler scrambler)
    {
        _currentDirectory = directory;
        _secureStorage = secureStorage;
        _scrambler = scrambler;
    }

    @Override
    public String savePasswordsToFile()
    {
        Time currentTime = new Time();
        currentTime.setToNow();
        String fileName = FilePrefix + currentTime.year + currentTime.month + currentTime.monthDay + currentTime.hour + currentTime.minute + currentTime.second + ".txt";
        String stringToWrite = getPasswordsFromDb();
        String result;

        File file = new File(_currentDirectory, fileName);
        FileOutputStream fos;

        byte[] data = stringToWrite.getBytes();
        try {
            fos = new FileOutputStream(file);
            fos.write(data);
            fos.flush();
            fos.close();
            result = String.format("File %s was saved successfully to %s", fileName, String.valueOf(_currentDirectory));
        } catch (FileNotFoundException e) {
            result = e.toString();
        } catch (IOException e) {
            result = e.toString();
        }

        return result;
    }

    @Override
    public String importPasswordsFromFile(BufferedReader fileData)
    {
        String result = "Can't find file for export.";
        try {
            String line;
            String passValue;
            long groupId = -1;
            while ((line = fileData.readLine()) != null) {
                if(!line.contains("pass:~%")) //group
                    groupId = _secureStorage.insertGroupResult(line.replace(System.getProperty("line.separator"), ""));
                else //child
                {
                    passValue = line.replace(System.getProperty("line.separator"), "");
                    passValue = passValue.replace("pass:~%", "");
                    String[] values = passValue.split("  :  ");
                    if(values.length == 2)
                        _secureStorage.insertElement(groupId, values[0], values[1]);
                }
            }

            result = "Import from selected text file was finished.";
        }
        catch (IOException e) {
            result = e.getMessage();
        }

        return result;
    }

    private String getPasswordsFromDb()
    {
        StringBuilder result = new StringBuilder();
        Cursor cr = _secureStorage.getGroupData();

        //Groups
        if (cr.moveToFirst()) {
            int idGroupColIndex = cr.getColumnIndex(SecureCryptStorage.ID_COLUMN_NAME);
            int groupColIndex = cr.getColumnIndex(SecureCryptStorage.GROUP_COLUMN_NAME);
            do {
                String groupValue = _scrambler.decrypt(cr.getString(groupColIndex));
                result.append(groupValue);
                result.append(System.getProperty("line.separator"));

                Cursor cp = _secureStorage.getElementData(cr.getInt(idGroupColIndex));
                int elementColIndex = cp.getColumnIndex(SecureCryptStorage.ELEMENT_COLUMN_VALUE);

                //Elements
                if(cp.moveToFirst())
                {
                    String pass;
                    do {
                        pass = _scrambler.decrypt(cp.getString(elementColIndex));
                        result.append("pass:~%" + pass);
                        result.append(System.getProperty("line.separator"));
                    }
                    while (cp.moveToNext());
                }
            } while (cr.moveToNext());
        }
        return result.toString();
    }
}
