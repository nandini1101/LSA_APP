package com.org.lsa.custom;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;

import androidx.appcompat.widget.AppCompatSpinner;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


public class MultiSelectSpinner extends AppCompatSpinner implements DialogInterface.OnMultiChoiceClickListener {
    public interface OnMultipleItemsSelectedListener {
        void selectedIndices(List<Integer> indices);

        void selectedStrings(List<String> strings);
    }

    private OnMultipleItemsSelectedListener listener;

    String[] _items = null;
    boolean[] mSelection = null;
    boolean[] mSelectionAtStart = null;
    String _itemsAtStart = null;
    public static String _itemsSelected = null;
    Context c;
    ArrayAdapter<String> simple_adapter;
    private boolean hasNone = false;

    public MultiSelectSpinner(Context context) {
        super(context);
        c = context;
        simple_adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_item);
        super.setAdapter(simple_adapter);
    }

    public MultiSelectSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);

        simple_adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_item);
        super.setAdapter(simple_adapter);
    }

    public void setListener(OnMultipleItemsSelectedListener listener) {
        this.listener = listener;
    }

    private String TAG = MultiSelectSpinner.class.getSimpleName();

    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
        if (mSelection != null && which < mSelection.length) {
            if (hasNone) {
                if (which == 0 && isChecked && mSelection.length > 1) {
                    for (int i = 1; i < mSelection.length; i++) {
//                        mSelection[i] = false;
                        mSelection[i] = true;
                        ((AlertDialog) dialog).getListView().setItemChecked(i, true);
//                        ((AlertDialog) dialog).getListView().setItemChecked(i, false);
                    }

                    if (Utility.showLogs == 0)
                        Log.d(TAG, "if checked");

                } else if (which == 0 && !isChecked && mSelection.length > 1) {
                    for (int i = 1; i < mSelection.length; i++) {
                        mSelection[i] = false;
//                        mSelection[i] = true;
//                        ((AlertDialog) dialog).getListView().setItemChecked(i, true);
                        ((AlertDialog) dialog).getListView().setItemChecked(i, false);
                    }
                    if (Utility.showLogs == 0)
                        Log.d(TAG, "else if not checked");
                } else if (which > 0 && mSelection[0] && isChecked) {
                    mSelection[0] = false;

                    ((AlertDialog) dialog).getListView().setItemChecked(0, false);

                    if (Utility.showLogs == 0)
                        Log.d(TAG, "which > 0 && mSelection[0] && isChecked");
                }
            }
            mSelection[which] = isChecked;
            simple_adapter.clear();

            if (getSelectedStrings().size() < (mSelection.length - 1)) {
                if (mSelection[0] == true) {
                    mSelection[0] = false;
                    ((AlertDialog) dialog).getListView().setItemChecked(0, false);
                }

            } else if (getSelectedStrings().size() == (mSelection.length - 1)) {
                if (mSelection[0] == false) {
                    mSelection[0] = true;
                    ((AlertDialog) dialog).getListView().setItemChecked(0, true);
                }

            }

            if (Utility.showLogs == 0) {
                Log.d(TAG, "Outside if mSelection.length: " + mSelection.length);
                Log.d(TAG, "Outside if getSelectedStrings().size(): "
                        + getSelectedStrings().size());
            }
//            simple_adapter.add(buildSelectedItemString());
            if (buildSelectedItemString().length() > 0) {
                simple_adapter.add(buildSelectedItemString());
                _itemsSelected = buildSelectedItemString();
            } else {
                simple_adapter.add("Non selected");
                _itemsSelected = null;
            }
        } else {
            throw new IllegalArgumentException(
                    "Argument 'which' is out of bounds.");
        }
    }

    @Override
    public boolean performClick() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Please select!");
        builder.setMultiChoiceItems(_items, mSelection, this);
        _itemsAtStart = getSelectedItemsAsString();
//        builder.setNeutralButton("Clear", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                setSelection(0);
//            }
//        });
        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                System.arraycopy(mSelection, 0, mSelectionAtStart, 0, mSelection.length);
                listener.selectedIndices(getSelectedIndices());
                listener.selectedStrings(getSelectedStrings());
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                simple_adapter.clear();
                if (buildSelectedItemString().length() > 0) {
                    simple_adapter.add(_itemsAtStart);
                } else {
                    simple_adapter.add("Non selected");
                }

                System.arraycopy(mSelectionAtStart, 0, mSelection, 0, mSelectionAtStart.length);
            }
        });
        builder.show();
        return true;
    }

    @Override
    public void setAdapter(SpinnerAdapter adapter) {
        throw new RuntimeException(
                "setAdapter is not supported by MultiSelectSpinner.");
    }

    public void setItems(String[] items) {
        _items = items;
        // _items = items.toArray(new String[items.size()]);
        mSelection = new boolean[_items.length];
        mSelectionAtStart = new boolean[_items.length];
        simple_adapter.clear();
//        simple_adapter.add(_items[0]);
        simple_adapter.add("Non selected");
        Arrays.fill(mSelection, false);
//        mSelection[0] = true;
//        mSelectionAtStart[0] = true;

       /* if (buildSelectedItemString().length() > 0) {
            simple_adapter.add(buildSelectedItemString());
        } else {
            simple_adapter.add("Non selected");
        }*/
    }

    String[] _id_items = null;
    boolean[] id_mSelection = null;

    public void setItems(List<String> items, List<String> id_items) {
        _id_items = id_items.toArray(new String[id_items.size()]);
        id_mSelection = new boolean[_id_items.length];
      /*  mSelectionAtStart  = new boolean[_items.length];
        simple_adapter.clear();
        //simple_adapter.add(_items[0]);
        simple_adapter.add("Non selected");
        Arrays.fill(mSelection, false);
        // mSelection[0] = true;*/


    }

    public void setItems(List<String> items) {
        _items = items.toArray(new String[items.size()]);
        mSelection = new boolean[_items.length];
        mSelectionAtStart = new boolean[_items.length];
        simple_adapter.clear();
        //simple_adapter.add(_items[0]);
        simple_adapter.add("Non selected");
        Arrays.fill(mSelection, false);
        // mSelection[0] = true;


    }

    public void setSelection(String[] selection) {
        for (int i = 0; i < mSelection.length; i++) {
            mSelection[i] = false;
            mSelectionAtStart[i] = false;
        }
        for (String cell : selection) {
            for (int j = 0; j < _items.length; ++j) {
                if (_items[j].equals(cell)) {
                    mSelection[j] = true;
                    mSelectionAtStart[j] = true;
                }
            }
        }
        simple_adapter.clear();
//        simple_adapter.add(buildSelectedItemString());
        if (buildSelectedItemString().length() > 0) {
            simple_adapter.add(buildSelectedItemString());
        } else {
            simple_adapter.add("Non selected");
        }
    }

    public void setSelection(List<String> selection) {
        for (int i = 0; i < mSelection.length; i++) {
            mSelection[i] = false;
            mSelectionAtStart[i] = false;
        }
        for (String sel : selection) {
            for (int j = 0; j < _items.length; ++j) {
                if (_items[j].equals(sel)) {
                    mSelection[j] = true;
                    mSelectionAtStart[j] = true;
                }
            }
        }
        simple_adapter.clear();
//        simple_adapter.add(buildSelectedItemString());
        if (buildSelectedItemString().length() > 0) {
            simple_adapter.add(buildSelectedItemString());
        } else {
            simple_adapter.add("Non selected");
        }
    }


    public void setSelection(int index) {
        for (int i = 0; i < mSelection.length; i++) {
            mSelection[i] = false;
            mSelectionAtStart[i] = false;
        }
        if (index >= 0 && index < mSelection.length) {
            mSelection[index] = true;
            mSelectionAtStart[index] = true;
        } else {
            throw new IllegalArgumentException("Index " + index
                    + " is out of bounds.");
        }
        simple_adapter.clear();
//        simple_adapter.add(buildSelectedItemString());
        if (buildSelectedItemString().length() > 0) {
            simple_adapter.add(buildSelectedItemString());
        } else {
            simple_adapter.add("Non selected");
        }
    }

    public void setSelection(int[] selectedIndices) {
        for (int i = 0; i < mSelection.length; i++) {
            mSelection[i] = false;
            mSelectionAtStart[i] = false;
        }
        for (int index : selectedIndices) {
            if (index >= 0 && index < mSelection.length) {
                mSelection[index] = true;
                mSelectionAtStart[index] = true;
            } else {
                throw new IllegalArgumentException("Index " + index
                        + " is out of bounds.");
            }
        }
        simple_adapter.clear();
//        simple_adapter.add(buildSelectedItemString());
        if (buildSelectedItemString().length() > 0) {
            simple_adapter.add(buildSelectedItemString());
        } else {
            simple_adapter.add("Non selected");
        }
    }

    public List<String> getSelectedStrings() {
        List<String> selection = new LinkedList<>();
        for (int i = 1; i < _items.length; ++i) {
            if (mSelection[i]) {
                selection.add(_items[i]);
            }
        }/*for (int i = 0; i < _items.length; ++i) {
            if (mSelection[i]) {
                selection.add(_items[i]);
            }
        }*/
        return selection;
    }

    public List<Integer> getSelectedIndices() {
        List<Integer> selection = new LinkedList<>();
        for (int i = 0; i < _items.length; ++i) {
            if (mSelection[i]) {
                selection.add(i);
            }
        }
        return selection;
    }

    private String buildSelectedItemString() {
        StringBuilder sb = new StringBuilder();
        boolean foundOne = false;

        for (int i = 1; i < _items.length; ++i) {
            if (mSelection[i]) {
                if (foundOne) {
                    sb.append(", ");
                }
                foundOne = true;

                sb.append(_items[i]);
            }
        }/*for (int i = 0; i < _items.length; ++i) {
            if (mSelection[i]) {
                if (foundOne) {
                    sb.append(", ");
                }
                foundOne = true;

                sb.append(_items[i]);
            }
        }*/
        return sb.toString();
    }

    public String getSelectedItemsAsString() {
        StringBuilder sb = new StringBuilder();
        boolean foundOne = false;

        for (int i = 1; i < _items.length; ++i) {
            if (mSelection[i]) {
                if (foundOne) {
                    sb.append(", ");
                }
                foundOne = true;
                sb.append(_items[i]);
            }
        }/* for (int i = 0; i < _items.length; ++i) {
            if (mSelection[i]) {
                if (foundOne) {
                    sb.append(", ");
                }
                foundOne = true;
                sb.append(_items[i]);
            }
        }*/
        return sb.toString();
    }

    public void hasNoneOption(boolean val) {
        hasNone = val;
    }
}
