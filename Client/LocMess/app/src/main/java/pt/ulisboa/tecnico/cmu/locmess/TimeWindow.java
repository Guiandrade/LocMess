package pt.ulisboa.tecnico.cmu.locmess;

import java.io.Serializable;
import java.util.Calendar;

/**
 * Created by wazamaisers on 27-03-2017.
 */

public class TimeWindow implements Serializable {

    private static final long serialVersionUID = 1L;

    int startingHour;
    int startingMinute;
    int startingDay;
    int startingMonth;
    int startingYear;
    int endingHour;
    int endingMinutes;
    int endingDay;
    int endingMonth;
    int endingYear;
    Calendar startingTime;
    Calendar endingTime;

    public TimeWindow(int startingHour, int startingMinute, int startingDay, int startingMonth,
                      int startingYear, int endingHour, int endingMinutes, int endingDay,
                      int endingMonth, int endingYear) {

        this.startingHour = startingHour;
        this.startingMinute = startingMinute;
        this.startingDay = startingDay;
        this.startingMonth = startingMonth;
        this.startingYear = startingYear;
        this.endingTime = Calendar.getInstance();
        this.endingTime.set(startingYear,startingMonth-1,startingDay,startingHour,startingMinute);

        this.endingHour = endingHour;
        this.endingMinutes = endingMinutes;
        this.endingDay = endingDay;
        this.endingMonth = endingMonth;
        this.endingYear = endingYear;
        this.endingTime = Calendar.getInstance();
        this.endingTime.set(endingYear,endingMonth-1,endingDay,endingHour,endingMinutes);
    }

    public TimeWindow(int endingHour, int endingMinutes, int endingDay, int endingMonth,
                      int endingYear) {
        this.startingTime = Calendar.getInstance();
        this.startingHour = startingTime.get(Calendar.HOUR_OF_DAY);
        this.startingMinute = startingTime.get(Calendar.MINUTE);
        this.startingDay = startingTime.get(Calendar.DAY_OF_MONTH);
        this.startingMonth = startingTime.get(Calendar.MONTH)+1;
        this.startingYear = startingTime.get(Calendar.YEAR);


        this.endingHour = endingHour;
        this.endingMinutes = endingMinutes;
        this.endingDay = endingDay;
        this.endingMonth = endingMonth;
        this.endingYear = endingYear;
        this.endingTime = Calendar.getInstance();
        this.endingTime.set(endingYear,endingMonth-1,endingDay,endingHour,endingMinutes);
    }

    public int getStartingHour() {
        return startingHour;
    }

    public int getStartingMinute() {
        return startingMinute;
    }

    public int getStartingDay() {
        return startingDay;
    }

    public int getStartingMonth() {
        return startingMonth;
    }

    public int getStartingYear() {
        return startingYear;
    }

    public int getEndingHour() {
        return endingHour;
    }

    public int getEndingMinutes() {
        return endingMinutes;
    }

    public int getEndingDay() {
        return endingDay;
    }

    public int getEndingMonth() {
        return endingMonth;
    }

    public int getEndingYear() {
        return endingYear;
    }

    public Calendar getStartingTime() {
        return startingTime;
    }

    public Calendar getEndingTime() {
        return endingTime;
    }

    public boolean checkTime(){
        Calendar now = Calendar.getInstance();
        if(now.compareTo(getStartingTime()) >= 0 && now.compareTo(getEndingTime()) < 0){
            return true;
        }
        else{
            return false;
        }
    }
}
