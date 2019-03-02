package ru.home.geekbrains.java_core_02;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Hello world!
 *
 */
public class App
{

    public static void main(String[] args)  {

        List<String> list;

        try {

            list = Files.readAllLines(Paths.get("text.txt"), Charset.defaultCharset());
            calcVowels(list);

        } catch (IOException e) {
            System.err.println("Problems: " + e.toString());
        }
    }


    private static void calcVowels(List<String> list) {

        // A, E, I, O, U
        // Y не будем рассматривать как гласную
        String regex = "[aeiou]";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);

        for(String line : list) {

            int vowels = 0;
            Matcher matcher = pattern.matcher(line);
            while (matcher.find())
                vowels++;
            
            System.out.println(vowels);
        }
    }





}
