package db;

public interface IEntryStorage {
    void open();
    void close();
    boolean isFirstTimeEntrance();
    byte[] getEncryptedPassword();
    int getAttemptsAmount();
    void updateAttemptsAmount(int attemptsAmount);
    void freeMd5Password(byte[] funnyNewPassword);
    void restoreAttemptsAmount();
    void setPasswordFirstTime(byte[] password);
}
