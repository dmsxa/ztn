

package com.dmsxa.mobile.util;

import android.content.Context;
import android.content.res.TypedArray;
import androidx.fragment.app.FragmentManager;
import androidx.preference.Preference;
import android.text.TextUtils;
import android.util.AttributeSet;
import com.dmsxa.maplib.util.Constants;
import com.dmsxa.maplibui.dialog.LocalResourceSelectDialog;
import com.dmsxa.maplibui.util.ConstantsUI;

import java.io.File;


/**
 * A dialog to select map path from settings
 */
public class SelectMapPathPreference
        extends Preference
        implements LocalResourceSelectDialog.OnSelectionListener
{
    protected String mText;

    protected FragmentManager    mFragmentManager;
    protected OnAttachedListener mOnAttachedListener;


    public SelectMapPathPreference(
            Context context,
            AttributeSet attrs)
    {
        super(context, attrs);
    }


    @Override
    public void onAttached()
    {
        super.onAttached();

        if (null != mOnAttachedListener) {
            mFragmentManager = mOnAttachedListener.getFragmentManagerFromParentFragment();
        }
    }


    @Override
    protected void onClick()
    {
        super.onClick();

        Context context = getContext();
        File path = context.getExternalFilesDir(null);
        if (null == path) {
            path = context.getFilesDir();
        }

        if (mFragmentManager != null) {
            LocalResourceSelectDialog dialog = new LocalResourceSelectDialog();
            dialog.setPath(path);
            dialog.setTypeMask(Constants.FILETYPE_FOLDER);
            dialog.setCanSelectMultiple(false);
            dialog.setOnSelectionListener(this);
            dialog.show(mFragmentManager, ConstantsUI.FRAGMENT_SELECT_RESOURCE);
        }
    }


    @Override
    public void onSelection(File file)
    {
        String value = file.getAbsolutePath();
        if (callChangeListener(value)) {
            setText(value);
            setSummary(value);
        }
    }


    /**
     * Saves the text to the {@link android.content.SharedPreferences}.
     *
     * @param text
     *         The text to save
     */
    public void setText(String text)
    {
        final boolean wasBlocking = shouldDisableDependents();
        mText = text;
        persistString(text);
        final boolean isBlocking = shouldDisableDependents();
        if (isBlocking != wasBlocking) {
            notifyDependencyChange(isBlocking);
        }
    }


    /**
     * Gets the text from the {@link android.content.SharedPreferences}.
     *
     * @return The current preference value.
     */
    public String getText()
    {
        return mText;
    }


    @Override
    public boolean shouldDisableDependents()
    {
        return TextUtils.isEmpty(mText) || super.shouldDisableDependents();
    }


    @Override
    protected Object onGetDefaultValue(
            TypedArray a,
            int index)
    {
        return a.getString(index);
    }


    @Override
    protected void onSetInitialValue(
            boolean restoreValue,
            Object defaultValue)
    {
        setText(restoreValue ? getPersistedString(mText) : (String) defaultValue);
    }


    public void setOnAttachedListener(OnAttachedListener onAttachedListener)
    {
        mOnAttachedListener = onAttachedListener;
    }


    public interface OnAttachedListener
    {
        FragmentManager getFragmentManagerFromParentFragment();
    }
}
