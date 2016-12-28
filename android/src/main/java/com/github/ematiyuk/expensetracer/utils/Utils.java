package com.github.ematiyuk.expensetracer.utils;

import android.content.Context;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utils {
    public static String getSystemFormatDateString(Context context, Date date) {
        java.text.DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(context);
        return  dateFormat.format(date);
    }

    public static String getSystemFormatDateString(Context context, String dateString) {
        java.text.DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(context);
        return  dateFormat.format(stringToDate(dateString));
    }

    public static String getDateString(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy", Locale.US);
        try {
            return dateFormat.format(date);
        } catch (Exception pe) {
            pe.printStackTrace();
            return "no_date";
        }
    }

    private static Date stringToDate(String dateString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy", Locale.US);
        try {
            return dateFormat.parse(dateString);
        } catch (ParseException pe) {
            pe.printStackTrace();
            return null;
        }
    }

    public static String formatToCurrency(float value) {
        final NumberFormat numberFormat = NumberFormat.getNumberInstance();
        numberFormat.setMaximumFractionDigits(2);
        numberFormat.setMinimumFractionDigits(2);
        return numberFormat.format(value);
    }
}
