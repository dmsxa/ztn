

package com.dmsxa.mobile.util;

import android.content.Context;
import androidx.preference.EditTextPreference;
import android.util.AttributeSet;

public class IntEditTextPreference extends EditTextPreference
{
	public IntEditTextPreference(Context context) {
		super(context);
	}

	public IntEditTextPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public IntEditTextPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

    public IntEditTextPreference(
            Context context,
            AttributeSet attrs,
            int defStyleAttr,
            int defStyleRes)
    {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
	public String getPersistedString(String defaultReturnValue) {
		int def = -1;
		try {
			def = Integer.parseInt(defaultReturnValue);
		} catch (Exception ignored) {}
		return String.valueOf(getPersistedInt(def));
	}

	@Override
	public boolean persistString(String value) {
		return persistInt(Integer.valueOf(value));
	}
}
