package Lesson_1;

abstract class Animal {
    protected int a;
    private int z;

    public Animal(int a, int z) {
        this.a = a;
        this.z = z;
    }

    void voice() {
        System.out.println("звук животного!");
    }

    abstract void test();
}

class Cat extends Animal {
    int b;
    protected int a;

    public Cat(int a, int z, int b) {
        super(a, z);
        this.b = b;
    }

    @Override
    void voice() {
        super.voice();
       // System.out.println("звук кота!");
    }

    @Override
    void test() {

    }
}

class SuperCat extends Cat {
        int y;

    public SuperCat(int a, int z, int b, int y) {
        super(a, z, b);
        this.y = y;
    }

    void test() {
        super.a = 10;
    }

    @Override
    void voice() {
        System.out.println("звук зверского кота!");
    }

}

class MainCat {
    public static void main(String[] args) {
       // Animal animal = new Animal(1,2);
        Cat cat =  new Cat(1,2,3);
        SuperCat superCat = new SuperCat(1,2,3,4);

        //animal.voice();
        cat.voice();
        superCat.voice();
    }
}









class Shape {
    public void draw() {
//ничего не делаем
        System.out.println("Фигура");
    }
}

class Square extends Shape {
    public void draw() {
        System.out.println("Квадрат");
    }
}

class Сircle extends Shape {
    public void draw() {
        System.out.println("Круг");
    }
}

class Triangle extends Shape {
    public void draw() {
        System.out.println("Треугольник");
    }
}

class MainPoli {
    public static void main(String[] args) {
        Shape[] a = new Shape[]{new Shape(),
                new Triangle(),
                new Square(),
                new Сircle()};

        for(int i = 0; i < a.length; i++) {
            a[i].draw();
        }
    }
}





















