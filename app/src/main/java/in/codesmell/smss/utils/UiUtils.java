package in.codesmell.smss.utils;

import android.content.Context;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * Created by Sreejith on 16/6/16.
 */
public class UiUtils {
    public static void showKeyboard(EditText editText, boolean requestFocus) {
        if (requestFocus) {
            editText.requestFocus();
            editText.setText(editText.getText());
            editText.setSelection(editText.getText().length());
            editText.setCursorVisible(true);
        }
        InputMethodManager imm = (InputMethodManager) editText.getContext().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, 0);
    }
}
