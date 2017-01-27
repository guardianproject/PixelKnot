package info.guardianproject.pixelknot;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;

import java.util.Date;

public class UIHelpers {
    public static int dpToPx(int dp, Context ctx)
    {
        Resources r = ctx.getResources();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
    }

    public static String dateDiffDisplayString(Date date, Context context, int idStringNever, int idStringRecently, int idStringMinutes, int idStringMinute,
                                               int idStringHours, int idStringHour, int idStringDays, int idStringDay)
    {
        if (date == null)
            return "";

        Date todayDate = new Date();
        double ti = todayDate.getTime() - date.getTime();
        if (ti < 0)
            ti = -ti;
        ti = ti / 1000; // Convert to seconds
        if (ti < 1)
        {
            return context.getString(idStringNever);
        }
        else if (ti < 60)
        {
            return context.getString(idStringRecently);
        }
        else if (ti < 3600 && (int) Math.round(ti / 60) < 60)
        {
            int diff = (int) Math.round(ti / 60);
            if (diff == 1)
                return context.getString(idStringMinute, diff);
            return context.getString(idStringMinutes, diff);
        }
        else if (ti < 86400 && (int) Math.round(ti / 60 / 60) < 24)
        {
            int diff = (int) Math.round(ti / 60 / 60);
            if (diff == 1)
                return context.getString(idStringHour, diff);
            return context.getString(idStringHours, diff);
        }
        else
        // if (ti < 2629743)
        {
            int diff = (int) Math.round(ti / 60 / 60 / 24);
            if (diff == 1)
                return context.getString(idStringDay, diff);
            return context.getString(idStringDays, diff);
        }
        // else
        // {
        // return context.getString(idStringNever);
        // }
    }
}
