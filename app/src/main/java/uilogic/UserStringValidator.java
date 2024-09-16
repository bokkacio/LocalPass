package uilogic;

import ru.internal.localpass.R;

public class UserStringValidator {
    private static int MINIMUM_PASSWORD_LENGTH = 6;

    public static ActionResult isPasswordMatchCriteria(String password, String passwordRepeat)
    {
        if(password.isEmpty() || passwordRepeat.isEmpty())
            return new ActionResult(false, R.string.empty_password);
        else if (!password.equals(passwordRepeat))
            return new ActionResult(false, R.string.not_equal_passwords);
        else if (password.length() < MINIMUM_PASSWORD_LENGTH)
            return new ActionResult(false, R.string.too_small_password);
        else
            return new ActionResult(true, ActionResult.DEFAULT_RESULT_CODE);
    }
}

