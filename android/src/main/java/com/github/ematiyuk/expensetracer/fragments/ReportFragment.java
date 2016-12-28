package com.github.ematiyuk.expensetracer.fragments;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;

import com.github.ematiyuk.expensetracer.R;
import com.github.ematiyuk.expensetracer.activities.MainActivity;
import com.github.ematiyuk.expensetracer.adapters.SectionExpenseAdapter;
import com.github.ematiyuk.expensetracer.providers.ExpensesContract.Expenses;
import com.github.ematiyuk.expensetracer.providers.ExpensesContract.ExpensesWithCategories;
import com.github.ematiyuk.expensetracer.utils.Utils;

import java.util.Calendar;
import java.util.Date;

public class ReportFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        PopupMenu.OnMenuItemClickListener {
    private static final int SUM_LOADER_ID = 0;
    private static final int LIST_LOADER_ID = 1;

    private static final String REPORT_TYPE = "report_type";
    private static final String SELECTION_ARGS = "selection_args";
    private static final int DATE_REPORT = 10;
    private static final int DATE_RANGE_REPORT = 11;

    private ListView mExpensesListView;
    private SectionExpenseAdapter mAdapter;
    private View mProgressBar;
    private TextView mTotalValueTextView;
    private TextView mTotalCurrencyTextView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_report, container, false);

        mExpensesListView = (ListView) rootView.findViewById(R.id.expenses_report_list_view);
        mProgressBar = rootView.findViewById(R.id.expenses_report_progress_bar);
        mTotalValueTextView = (TextView) rootView.findViewById(R.id.expenses_report_total_text_view);
        mTotalCurrencyTextView = (TextView) rootView.findViewById(R.id.expenses_report_total_currency_text_view);

        mExpensesListView.setEmptyView(rootView.findViewById(R.id.expenses_report_empty_list_view));
        mTotalValueTextView.setText(Utils.formatToCurrency(0.0f));

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mAdapter = new SectionExpenseAdapter(getActivity());
        mExpensesListView.setAdapter(mAdapter);

        initLoaders();
    }

    @Override
    public void onResume() {
        super.onResume();
        reloadReportData();
        reloadSharedPreferences();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_report, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.filter_expenses_menu_item:
                View menuItemView = getActivity().findViewById(R.id.filter_expenses_menu_item);
                showPopupMenu(menuItemView);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void showPopupMenu(View v) {
        PopupMenu popup = new PopupMenu(getActivity(), v);
        popup.setOnMenuItemClickListener(ReportFragment.this);

        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.report_filter_popup, popup.getMenu());
        popup.show();
    }

    /* from PopupMenu.OnMenuItemClickListener */
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        ((MainActivity) getActivity()).hideNavigationBar();
        switch (item.getItemId()) {
            case R.id.today_filter_option:
                makeTodaysReport();
                return true;
            case R.id.week_filter_option:
                makeWeeklyReport();
                return true;
            case R.id.month_filter_option:
                makeMonthlyReport();
                return true;
            case R.id.date_filter_option:
                makeDateReport();
                return true;
            case R.id.range_filter_option:
                makeDateRangeReport();
                return true;
            default:
                return false;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        int reportType = args.getInt(REPORT_TYPE);
        String[] selectionArgs = args.getStringArray(SELECTION_ARGS);
        Uri uri = null;
        switch (id) {
            case SUM_LOADER_ID:
                if (reportType == DATE_REPORT) {
                    uri = ExpensesWithCategories.SUM_DATE_CONTENT_URI;
                } else if (reportType == DATE_RANGE_REPORT) {
                    uri = ExpensesWithCategories.SUM_DATE_RANGE_CONTENT_URI;
                }
                break;
            case LIST_LOADER_ID:
                mProgressBar.setVisibility(View.VISIBLE);
                if (reportType == DATE_REPORT) {
                    uri = ExpensesWithCategories.DATE_CONTENT_URI;
                } else if (reportType == DATE_RANGE_REPORT) {
                    uri = ExpensesWithCategories.DATE_RANGE_CONTENT_URI;
                }
                break;
        }

        return new CursorLoader(getActivity(),
                uri,
                null,
                null,
                selectionArgs,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case SUM_LOADER_ID:
                int valueSumIndex = data.getColumnIndex(Expenses.VALUES_SUM);
                data.moveToFirst();
                float valueSum = data.getFloat(valueSumIndex);
                mTotalValueTextView.setText(Utils.formatToCurrency(valueSum));
                break;

            case LIST_LOADER_ID:
                // Hide the progress bar
                mProgressBar.setVisibility(View.GONE);
                // Update adapter's data
                mAdapter.swapCursor(data);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    private void reloadSharedPreferences() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String prefCurrency = sharedPref.getString(SettingsFragment.KEY_PREF_CURRENCY, "");

        mTotalCurrencyTextView.setText(prefCurrency);
        mAdapter.setCurrency(prefCurrency);
    }

    private void reloadReportData() {
        // Show the progress bar
        mProgressBar.setVisibility(View.VISIBLE);
        // Today's report by default
        makeTodaysReport();
    }

    private void makeTodaysReport() {
        String today = Utils.getDateString(new Date());
        getActivity().setTitle(getString(R.string.filter_todays_expenses));

        String[] selectionArgs = { today };

        restartLoaders(DATE_REPORT, selectionArgs);
    }

    private void makeWeeklyReport() {
        Calendar calendar = Calendar.getInstance();
        Date todayDate = new Date();
        calendar.setTime(todayDate); // Set today
        calendar.add(Calendar.DAY_OF_YEAR, -7); // Subtract 7 days from today
        String startDate = Utils.getDateString(calendar.getTime());
        String endDate = Utils.getDateString(todayDate);

        getActivity().setTitle(getString(R.string.filter_weeks_expenses));

        String[] selectionArgs = { startDate, endDate };

        restartLoaders(DATE_RANGE_REPORT, selectionArgs);
    }

    private void makeMonthlyReport() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1); // Set the first day of month to 1
        String startDate = Utils.getDateString(calendar.getTime()); // Get start of month
        String endDate = Utils.getDateString(new Date());

        getActivity().setTitle(getString(R.string.filter_months_expenses));

        String[] selectionArgs = { startDate, endDate };

        restartLoaders(DATE_RANGE_REPORT, selectionArgs);
    }

    private void makeDateReport() {
        DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, day);
                String dateString = Utils.getDateString(calendar.getTime());
                String systemFormatDateStr = Utils.getSystemFormatDateString(getActivity(),
                        calendar.getTime());

                getActivity().setTitle(getString(R.string.filter_date_expenses, systemFormatDateStr));

                String[] selectionArgs = { dateString };

                restartLoaders(DATE_REPORT, selectionArgs);
            }
        };

        DatePickerFragment datePickerFragment = DatePickerFragment.newInstance(listener);
        datePickerFragment.show(getActivity().getSupportFragmentManager(), "date_picker");
    }

    private Date startDate;

    private void makeDateRangeReport() {
        final DatePickerDialog.OnDateSetListener endDateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, day);
                String startDateStr = Utils.getDateString(startDate);
                String endDateStr = Utils.getDateString(calendar.getTime());
                String sysFormatEndDateStr = Utils.getSystemFormatDateString(getActivity(),
                        calendar.getTime());
                String sysFormatStartDateStr = Utils.getSystemFormatDateString(getActivity(), startDate);

                getActivity().setTitle(getString(R.string.filter_date_range_expenses,
                        sysFormatStartDateStr, sysFormatEndDateStr));

                String[] selectionArgs = { startDateStr, endDateStr };

                restartLoaders(DATE_RANGE_REPORT, selectionArgs);
            }
        };

        DatePickerDialog.OnDateSetListener startDateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, day);
                startDate = calendar.getTime();

                DatePickerFragment datePickerFragment = DatePickerFragment.newInstance(endDateListener);
                datePickerFragment.show(getActivity().getSupportFragmentManager(), "end_date_picker");
            }
        };

        DatePickerFragment datePickerFragment = DatePickerFragment.newInstance(startDateListener);
        datePickerFragment.show(getActivity().getSupportFragmentManager(), "start_date_picker");
    }

    private void restartLoaders(int reportType, String[] selectionArgs) {
        Bundle args = createBundleArgs(reportType, selectionArgs);

        getLoaderManager().restartLoader(SUM_LOADER_ID, args, this);
        getLoaderManager().restartLoader(LIST_LOADER_ID, args, this);
    }

    private void initLoaders() {
        // Retrieve today's date string
        String today = Utils.getDateString(new Date());
        String[] selectionArgs = { today };

        Bundle args = createBundleArgs(DATE_REPORT, selectionArgs);

        // Initialize the CursorLoaders
        getLoaderManager().initLoader(SUM_LOADER_ID, args, this);
        getLoaderManager().initLoader(LIST_LOADER_ID, args, this);
    }

    private Bundle createBundleArgs(int reportType, String[] selectionArgs) {
        Bundle args = new Bundle();
        args.putInt(REPORT_TYPE, reportType);
        args.putStringArray(SELECTION_ARGS, selectionArgs);
        return args;
    }
}
