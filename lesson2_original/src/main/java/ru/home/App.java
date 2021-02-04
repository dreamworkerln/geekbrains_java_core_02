package ru.home;

import ru.home.exception.MyArrayDataException;
import ru.home.exception.MyArrayException;
import ru.home.exception.MyArraySizeException;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Hello world!
 *
 */
public class App 
{
    private static final int ARRAY_SIZE = 4;

    public static void main(String[] args ) {

        // here array[1][3] mean index  x=1, y=3
        String[][] array = new String[4][4];

        for (int j = 0; j < array[0].length; j++) {
            for (int i = 0; i < array.length; i++) {

                array[i][j] = Integer.toString(ThreadLocalRandom.current().nextInt(0, 99));
            }
        }

        array[1][3] = "?";

        try {

            int res = sumArray(array);

            System.out.println("Result: " + res);
        }
        catch (MyArrayException e) {

            System.out.println("Wrong input data: " + e.toString());
        }
        catch (Exception e) {
            System.err.println("Something bad happened: " + e.toString());
        }

    }




    
    static int sumArray(String[][] array) throws MyArrayException {

        int result = 0;

//       1. Напишите метод, на вход которого подается двумерный строковый массив размером 4х4,
//       при подаче массива другого размера необходимо бросить исключение MyArraySizeException.

        if (array.length != ARRAY_SIZE)
           throw new MyArraySizeException();

        for (String[] col : array) {

            if (col.length != ARRAY_SIZE)
                throw new MyArraySizeException();
        }

//      2. Далее метод должен пройтись по всем элементам массива, преобразовать в int, и просуммировать.
//      Если в каком-то элементе массива преобразование не удалось (например, в ячейке лежит символ или текст вместо числа),
//      должно быть брошено исключение MyArrayDataException – с детализацией, в какой именно ячейке лежат неверные данные.

        int x_tmp = -1;
        int y_tmp = -1;

        try {

            for (int j = 0; j < array[0].length; j++) {
                for (int i = 0; i < array.length; i++) {

                    // For error handling
                    x_tmp = i;
                    y_tmp = j;

                    result += Integer.parseInt(array[i][j]);
                }
            }

        }
        catch (Exception e) {
            throw new MyArrayDataException(x_tmp, y_tmp, array[x_tmp][y_tmp]);
        }

        return result;
    }

}
