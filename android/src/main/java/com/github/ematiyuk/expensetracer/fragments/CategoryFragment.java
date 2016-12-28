package com.github.ematiyuk.expensetracer.fragments;

import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AlertDialog;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.Toast;

import com.github.ematiyuk.expensetracer.providers.ExpensesContract.Categories;
import com.github.ematiyuk.expensetracer.providers.ExpensesContract.Expenses;
import com.github.ematiyuk.expensetracer.R;
import com.github.ematiyuk.expensetracer.activities.CategoryEditActivity;

public class CategoryFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private ListView mCategoriesView;
    private SimpleCursorAdapter mAdapter;
    private View mProgressBar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_category, container, false);

        mCategoriesView = (ListView) rootView.findViewById(R.id.categories_list_view);
        mProgressBar = rootView.findViewById(R.id.categories_progress_bar);

        mCategoriesView.setEmptyView(rootView.findViewById(R.id.categories_empty_list_view));
        mCategoriesView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                prepareCategoryToEdit(id);
            }
        });
        rootView.findViewById(R.id.add_category_button_if_empty_list).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prepareCategoryToCreate();
            }
        });

        registerForContextMenu(mCategoriesView);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mAdapter = new SimpleCursorAdapter(getActivity(),
                R.layout.category_list_item, null,
                new String[] { Categories.NAME },
                new int[] { R.id.category_name_list_item}, 0);

        mCategoriesView.setAdapter(mAdapter);

        // Initialize the CursorLoader
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        reloadCategoryList();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_category, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_category_menu_item:
                prepareCategoryToCreate();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getActivity().getMenuInflater().inflate(R.menu.category_list_item_context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.delete_category_menu_item:
                deleteCategory(info.id);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projectionFields = new String[] {
                Categories._ID,
                Categories.NAME
        };

        return new CursorLoader(getActivity(),
                Categories.CONTENT_URI,
                projectionFields,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Hide the progress bar
        mProgressBar.setVisibility(View.GONE);

        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    private void reloadCategoryList() {
        // Show the progress bar
        mProgressBar.setVisibility(View.VISIBLE);
        // Reload data by restarting the cursor loader
        getLoaderManager().restartLoader(0, null, this);
    }

    private int deleteSingleCategory(long categoryId) {
        Uri uri = ContentUris.withAppendedId(Categories.CONTENT_URI, categoryId);

        // Defines a variable to contain the number of rows deleted
        int rowsDeleted;

        // Deletes the category that matches the selection criteria
        rowsDeleted = getActivity().getContentResolver().delete(
                uri,        // the URI of the row to delete
                null,       // where clause
                null        // where args
        );

        reloadCategoryList();
        showStatusMessage(getResources().getString(R.string.category_deleted));

        return rowsDeleted;
    }

    private int deleteAssociatedExpenses(long categoryId) {
        String selection = Expenses.CATEGORY_ID + " = ?";
        String[] selectionArgs = { String.valueOf(categoryId) };

        return getActivity().getContentResolver().delete(
                Expenses.CONTENT_URI,
                selection,
                selectionArgs
        );
    }

    private void deleteCategory(final long categoryId) {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.delete_category)
                .setMessage(R.string.delete_cat_dialog_msg)
                .setNeutralButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.delete_string, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        int expenseRowsDeleted = deleteAssociatedExpenses(categoryId);

                        String statusMsg = getResources().getQuantityString(
                                R.plurals.expenses_deleted_plurals_msg, expenseRowsDeleted, expenseRowsDeleted);
                        showStatusMessage(statusMsg);

                        deleteSingleCategory(categoryId);
                    }
                })
                .setIcon(R.drawable.ic_dialog_alert)
                .show();
    }

    private void showStatusMessage(CharSequence text) {
        Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
    }

    private void prepareCategoryToCreate() {
        startActivity(new Intent(getActivity(), CategoryEditActivity.class));
    }

    private void prepareCategoryToEdit(long id) {
        Intent intent = new Intent(getActivity(), CategoryEditActivity.class);
        intent.putExtra(CategoryEditFragment.EXTRA_EDIT_CATEGORY, id);
        startActivity(intent);
    }
}
