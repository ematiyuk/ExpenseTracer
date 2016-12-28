package com.github.ematiyuk.expensetracer.fragments;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.github.ematiyuk.expensetracer.providers.ExpensesContract.Categories;
import com.github.ematiyuk.expensetracer.R;

public class CategoryEditFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String EXTRA_EDIT_CATEGORY = "com.github.ematiyuk.expensetracer.edit_category";

    private EditText mCatNameEditText;
    private long mExtraValue;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_category_edit, container, false);

        mCatNameEditText = (EditText) rootView.findViewById(R.id.category_name_edit_text);

        // Set listener on Done (submit) button on keyboard clicked
        mCatNameEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    checkEditTextForEmptyField(mCatNameEditText);
                    return true;
                }
                return false;
            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mExtraValue = getActivity().getIntent().getLongExtra(EXTRA_EDIT_CATEGORY, -1);
        // Create a new category
        if (mExtraValue < 1) {
            getActivity().setTitle(R.string.add_category);

        // Edit existing category
        } else {
            getActivity().setTitle(R.string.edit_category);
            setCategoryData();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_category_edit, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.done_category_edit_menu_item:
                if (checkEditTextForEmptyField(mCatNameEditText)) {
                    // Create a new category
                    if (mExtraValue < 1) {
                        insertNewCategory();

                    // Edit existing category
                    } else {
                        updateCategory(mExtraValue);
                    }
                    getActivity().finish();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean checkEditTextForEmptyField(EditText editText) {
        String inputText = editText.getText().toString().trim();
        if (inputText.length() == 0) {
            editText.setError(getResources().getString(R.string.error_empty_field));
            mCatNameEditText.selectAll();
            return false;
        } else {
            return true;
        }
    }

    private void setCategoryData() {
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public CursorLoader onCreateLoader(int id, Bundle args) {
        String[] projectionFields = new String[] {
                Categories._ID,
                Categories.NAME
        };

        Uri singleCategoryUri = ContentUris.withAppendedId(Categories.CONTENT_URI, mExtraValue);

        return new CursorLoader(getActivity(),
                singleCategoryUri,
                projectionFields,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        int categoryNameIndex = data.getColumnIndex(Categories.NAME);
        data.moveToFirst();
        String categoryName = data.getString(categoryNameIndex);
        mCatNameEditText.setText(categoryName);
    }

    @Override
    public void onLoaderReset(Loader loader) {
        mCatNameEditText.setText("");
    }

    private void insertNewCategory() {
        ContentValues insertValues = new ContentValues();
        insertValues.put(Categories.NAME, mCatNameEditText.getText().toString());

        getActivity().getContentResolver().insert(
                Categories.CONTENT_URI,
                insertValues
        );

        Toast.makeText(getActivity(),
                getResources().getString(R.string.category_added),
                Toast.LENGTH_SHORT).show();
    }

    private void updateCategory(long id) {
        ContentValues updateValues = new ContentValues();
        updateValues.put(Categories.NAME, mCatNameEditText.getText().toString());

        Uri categoryUri = ContentUris.withAppendedId(Categories.CONTENT_URI, id);

        getActivity().getContentResolver().update(
                categoryUri,
                updateValues,
                null,
                null
        );

        Toast.makeText(getActivity(),
                getResources().getString(R.string.category_updated),
                Toast.LENGTH_SHORT).show();
    }
}
