package ru.home.geekbrains;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Hello world!
 *
 */
public class App
{
    public static void main(String[] args) {

        System.out.println("---------------------------------------\n");

        System.out.println(checkPassword(""));
        System.out.println(checkPassword("6565656"));
        System.out.println(checkPassword("rer7A%fhg"));

        System.out.println("---------------------------------------\n");

        task01();

        System.out.println("---------------------------------------\n");

        task02();

    }


    static boolean checkPassword(String string) {

        final String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,20}$";

        final Pattern pattern = Pattern.compile(regex);
        final Matcher matcher = pattern.matcher(string);
        return matcher.find();
    }


    static void task01() {

        String[] input = new String[20];

        for (int i = 0 ; i< input.length; i++) {

            int num = ThreadLocalRandom.current().nextInt(0, 5);
            input[i] = String.format("%1$02d", num);
        }
        System.out.println(Arrays.toString(input));

        Map<String, Integer> map = new HashMap<>();


        for (String s : input) {
            map.merge(s, 1, Integer::sum);
        }

        System.out.println(map.toString());
    }


    static void task02() {

        PhoneBook book = new PhoneBook();

        book.add("Ivanov", "123");
        book.add("Ivanov", "123");
        book.add("Ivanov", "345");
        book.add("Ivanov", "789");

        book.add("Petrov", "111");
        book.get("Petrov").add("222");


        System.out.println(book.get("Ivanov"));

        System.out.println(book);


    }




}
