
package com.dmsxa.mobile.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;

import com.dmsxa.maplibui.dialog.NGDialog;
import com.dmsxa.mobile.R;

public class NewFieldDialog extends NGDialog {
    private OnFieldChooseListener mListener;

    public interface OnFieldChooseListener {
        void OnFieldChosen(String name, int type);
    }

    public NewFieldDialog setOnFieldChooseListener(OnFieldChooseListener listener) {
        mListener = listener;
        return this;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        View view = View.inflate(mContext, R.layout.dialog_new_field, null);
        final EditText name = (EditText) view.findViewById(R.id.et_field_name);
        final Spinner type = (Spinner) view.findViewById(R.id.sp_field_type);

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(mTitle).setView(view).setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (mListener != null) {
                        int fieldType = getResources().getIntArray(R.array.field_types)[type.getSelectedItemPosition()];
                        String alias = name.getText().toString().trim();
                        mListener.OnFieldChosen(alias, fieldType);
                    }
                }
            });
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }
}
