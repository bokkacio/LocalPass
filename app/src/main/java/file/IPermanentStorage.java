package file;

import java.io.BufferedReader;

public interface IPermanentStorage {
    String savePasswordsToFile();
    String importPasswordsFromFile(BufferedReader fileData);
}