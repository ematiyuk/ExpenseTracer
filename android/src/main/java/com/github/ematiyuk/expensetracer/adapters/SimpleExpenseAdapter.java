package com.github.ematiyuk.expensetracer.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.ematiyuk.expensetracer.providers.ExpensesContract;
import com.github.ematiyuk.expensetracer.R;
import com.github.ematiyuk.expensetracer.utils.Utils;

public class SimpleExpenseAdapter extends CursorAdapter {
    private String mCurrency;

    public SimpleExpenseAdapter(Context context) {
        super(context, null, 0);
    }

    public void setCurrency(String currency) {
        mCurrency = currency;
        notifyDataSetChanged();
    }

    // The newView method is used to inflate a new view and return it
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.expense_list_item, parent, false);
    }

    // The bindView method is used to bind all data to a given view
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find fields to populate in inflated template
        TextView tvExpenseValue = (TextView) view.findViewById(R.id.expense_value_text_view);
        TextView tvExpenseCurrency = (TextView) view.findViewById(R.id.expense_currency_text_view);
        TextView tvExpenseCatName = (TextView) view.findViewById(R.id.expense_category_name_text_view);

        // Extract values from cursor
        float expValue = cursor.getFloat(cursor.getColumnIndexOrThrow(ExpensesContract.Expenses.VALUE));
        String categoryName = cursor.getString(cursor.getColumnIndexOrThrow(ExpensesContract.Categories.NAME));

        // Populate views with extracted values
        tvExpenseValue.setText(Utils.formatToCurrency(expValue));
        tvExpenseCatName.setText(categoryName);
        tvExpenseCurrency.setText(mCurrency);
    }
}
