package ru.home;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main(String[] args ) {

        System.out.println(getWorkingHours(DayOfWeek.Tuesday));
    }
    
    static int getWorkingHours(DayOfWeek current) {

        int result = 0;
        for (int i = current.ordinal(); i < DayOfWeek.values().length; i++) {

            DayOfWeek day = DayOfWeek.values()[i];
            result += day.getHours();
        }

        return result;
    }

}
