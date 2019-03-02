package Lesson_1.Marathon;

/*
2. Добавить класс Team, который будет содержать название команды,
 массив из четырех участников (в конструкторе можно сразу указыватьвсех участников ),
метод для вывода информации о членах команды, прошедших дистанцию,
метод вывода информации обо всех членах команды.
 */

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Team implements Iterable<Competitor>{


    private String name;
    private List<Competitor> list = new ArrayList<>();
    

    public Team(String name) {
        this.name = name;
    }


    public void add(Competitor competitor) {

        if (competitor == null)
            throw new IllegalArgumentException("competitor == null");

        list.add(competitor);
    }



    public void getActiveInfo() {

        System.out.println("Team \"" + name +  "\" active members:");

        for (Competitor c : list) {

            if (c.isOnDistance())
                c.info();
        }

    }


    public void getAllInfo() {

        System.out.println("Team \"" + name +  "\" members:");

        for (Competitor c : list) {
            c.info();
        }

    }


    public String getName() {
        return name;
    }

    // ---------------------------------------------------------------------

    @Override
    public Iterator<Competitor> iterator() {
        return list.iterator();
    }
}
