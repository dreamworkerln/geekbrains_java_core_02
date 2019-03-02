package Lesson_1;

import java.util.Arrays;

public class Box implements Run{
    static String name;

    protected int weight;

    public void setWeight(int weight) {
        if (weight > 0) {
            this.weight = weight;
        } else {
            System.out.println("Вес не может быть отрицательным!");
        }
    }

    public Box() {

    }

    public Box(String name, int weight) {
        this.name = name;
        this.weight = weight;
    }

    @Override
    public void run() {

    }

//    void startTest(Tools tools) {
//        tools.infoToolsAndBox(this);
//    }
}

class TestBox {
    public static void main(String[] args) {

        Box box = new Box("box1", 10);
     //   box.weight = 10;

    }
}

//class Tools {
//    public Tools(String tools) {
//        this.tools = tools;
//    }
//
//    String tools;
//    void infoToolsAndBox(Box box) {
//        System.out.println(tools + " "
//                + box.name + " "
//                + box.weight);
//    }
//}
//
//class TestBox {
//    public static void main(String[] args) {
//
//        Box box = new Box("box1", 10);
//        Tools tools = new Tools("hummer");
//        box.startTest(tools);
//
//    }
//}
