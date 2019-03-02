package Lesson_1.Marathon;

public class Main {
    public static void main(String[] args) {

        // ------------------------------------

        Team team = new Team("Dream team");
        team.add(new Human("Боб"));
        team.add(new Cat("Барсик"));
        team.add(new Dog("Бобик"));

        Course course = new Course();
        course.add(new Cross(80));
        course.add(new Water(20));
        course.add(new Wall(2));


        // ------------------------------------

        course.doIt(team);

        // ------------------------------------
        System.out.println();
        // ------------------------------------

        // results
        team.getAllInfo();
    }
}