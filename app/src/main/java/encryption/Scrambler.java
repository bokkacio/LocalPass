package encryption;

import android.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Scrambler implements IScrambler{
    private String _keyPhrase = null;
    private final String ALGORITHM = "Blowfish";
    private final String MODE = "Blowfish/CBC/PKCS5Padding";
    private final String IV = "xqigmevo";

    public Scrambler(String keyPhrase)
    {
        _keyPhrase = keyPhrase;
    }

    @Override
    public String encrypt(String secretPhrase)
    {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(_keyPhrase.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(MODE);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, new IvParameterSpec(IV.getBytes()));
            byte[] values = cipher.doFinal(secretPhrase.getBytes());
            return Base64.encodeToString(values, Base64.DEFAULT);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String decrypt(String encryptedPhrase) {
        try {
            byte[] values = Base64.decode(encryptedPhrase, Base64.DEFAULT);
            SecretKeySpec secretKeySpec = new SecretKeySpec(_keyPhrase.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(MODE);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(IV.getBytes()));
            return new String(cipher.doFinal(values));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}