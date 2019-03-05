package ru.home;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main(String[] args ) {

        DayOfWeek current = DayOfWeek.Tuesday;

        int total = 0;

        for (int i = current.ordinal(); i < DayOfWeek.values().length; i++) {

            DayOfWeek day = DayOfWeek.values()[i];
            total += day.getHours();
        }
        System.out.println(total);
    }
}
