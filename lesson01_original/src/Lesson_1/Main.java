package Lesson_1;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
//    Box box = new Box("box1", 10);
//    Tools tools = new Tools("hummer");
//    box.startTest(tools);
//
//        int a = 10;
//
//        System.out.println(test1(a));

//        int[] mass = {1,1,1};
//
//        test2(mass);
//
//        System.out.println(Arrays.toString(mass));

//        String str1 = "Hello";
//        StringBuilder stringBuilder = new StringBuilder("Hello");
//        test4(stringBuilder);
//        System.out.println(stringBuilder);
//        Main main = new Main();
//        main.abc();

//        Box box1 = new Box();
//        Box box2 = new Box();
//
//        box1.name = "test1";
//        box1.weight = 10;
//
//        box2.name = "test2";
//        box2.weight = 20;
//
//        String name = "asd";
//
//        System.out.println(box1.name + " " + box1.weight + " " + box2.name + " " + box2.weight);

        Box[] boxes = {new Box(), new Box()};
        boxes[0].weight = 20;
        boxes[1].weight = 30;

        System.out.println( boxes[0].weight + " " + boxes[1].weight);

    }

    void abc() {
        System.out.println("abc");
    }

//    public static int test1(int a) {
//       return a++;
//    }
//
//
//    public static void test2(int[] mass){
//        mass[2] = 3;
//    }
//
//    public static void test4(StringBuilder stringBuilder){
//        stringBuilder.append("1");
//    }

}
