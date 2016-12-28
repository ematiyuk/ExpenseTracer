package com.github.ematiyuk.expensetracer.providers;

import android.net.Uri;
import android.provider.BaseColumns;

public final class ExpensesContract {
    /**
     * The authority for the expenses provider
     */
    public static final String AUTHORITY = "com.github.ematiyuk.expensetracer.provider";
    /**
     * The content:// style URI for expenses provider
     */
    public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

    /**
     * The contract class cannot be instantiated
     */
    private ExpensesContract(){}

    public static class Categories implements BaseColumns, CategoriesColumns {
        /**
         * This utility class cannot be instantiated
         */
        private Categories() {}

        /**
         * The content:// style URI for this table
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, "categories");

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of categories.
         */
        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd.ematiyuk.expensetracer.provider.expense_category";

        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single category.
         */
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd.ematiyuk.expensetracer.provider.expense_category";

        /**
         * Sort by ascending order of _id column (the order as items were added).
         */
        public static final String DEFAULT_SORT_ORDER = _ID + " ASC";
    }

    public static class Expenses implements BaseColumns, ExpensesColumns {
        /**
         * This utility class cannot be instantiated
         */
        private Expenses() {}

        /**
         * The content:// style URI for this table
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, "expenses");

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of expenses.
        */
        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd.ematiyuk.expensetracer.provider.expense";

        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single expense.
         */
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd.ematiyuk.expensetracer.provider.expense";

        /**
         * Sort by descending order of date (the most recent items are at the end).
         */
        public static final String DEFAULT_SORT_ORDER = DATE + " ASC";

        /**
         * Expense sum value column name to return for joined tables
         */
        public static final String VALUES_SUM = "values_sum";
    }

    public static class ExpensesWithCategories implements BaseColumns {
        /**
         * This utility class cannot be instantiated.
         */
        private ExpensesWithCategories() {}

        /**
         * The content:// style URI for this table.
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, "expensesWithCategories");

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of expenses with categories.
         */
        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd.ematiyuk.expensetracer.provider.expense_with_category";

        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single expense with a category.
         */
//        public static final String CONTENT_ITEM_TYPE =
//                "vnd.android.cursor.item/vnd.ematiyuk.expensetracer.provider.expense_with_category";

        /**
         * The content:// style URI for this joined table to filter items by a specific date.
         */
        public static final Uri DATE_CONTENT_URI = Uri.withAppendedPath(CONTENT_URI, "date");

        /**
         * The content:// style URI for this joined table to filter items by a specific date range.
         */
        public static final Uri DATE_RANGE_CONTENT_URI = Uri.withAppendedPath(CONTENT_URI, "dateRange");

        /**
         * The content:// style URI for getting sum of expense values
         * for this joined table by "date" filter.
         */
        public static final Uri SUM_DATE_CONTENT_URI = Uri.withAppendedPath(DATE_CONTENT_URI, "sum");

        /**
         * The content:// style URI for getting sum of expense values
         * for this joined table by "date range" filter.
         */
        public static final Uri SUM_DATE_RANGE_CONTENT_URI =
                Uri.withAppendedPath(DATE_RANGE_CONTENT_URI, "sum");
    }

    protected interface CategoriesColumns {
        String NAME = "name";
    }

    protected interface ExpensesColumns {
        String VALUE = "value";
        String DATE = "date";
        String CATEGORY_ID = "category_id";
    }
}
