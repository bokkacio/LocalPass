package encryption;

public interface IScrambler {
    String encrypt(String secretPhrase);
    String decrypt(String encryptedPhrase);
}
