package app.bryanlu.quizbowl;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import java.util.ArrayList;

/**
 * Created by Bryan Lu on 4/29/2017.
 *
 * Dialog shown when question categories are chosen in the Play fragment.
 */

public class CategoryDialog extends DialogFragment {
    public static final String TAG = "categories";
    private ArrayList<String> selectedCategories;
    DialogListener mListener;

    @Override @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        selectedCategories = new ArrayList<>();
        final String[] categories = {
                getString(R.string.bioKey),
                getString(R.string.chemKey),
                getString(R.string.geoKey),
                getString(R.string.mathKey),
                getString(R.string.physKey)
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Question Categories")
                .setMultiChoiceItems(categories, null,
                        new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id, boolean isChecked) {
                        if (isChecked) {
                            selectedCategories.add(categories[id]);
                        }
                        else if (selectedCategories.contains(categories[id])) {
                            selectedCategories.remove(categories[id]);
                        }
                    }
                })
                .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mListener.onDialogPositiveClick(CategoryDialog.this);
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Do nothing
                    }
                });

        return builder.create();
    }

    /** The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    interface DialogListener {
        void onDialogPositiveClick(CategoryDialog dialog);
    }

    // Override the Fragment.onAttach() method to instantiate the DialogListener
    // Adapted from http://stackoverflow.com/a/34268905
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (DialogListener) getParentFragment();
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + " must implement DialogListener");
        }
    }

    public ArrayList<String> getSelectedCategories() {
        return selectedCategories;
    }
}
