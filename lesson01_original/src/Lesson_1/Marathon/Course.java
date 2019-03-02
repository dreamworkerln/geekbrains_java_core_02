package Lesson_1.Marathon;


/*
3. Добавить класс Course (полоса препятствий), в котором будут находиться массив препятствий
и метод, который будет просить команду пройти всю полосу;
 */

import java.util.ArrayList;
import java.util.List;

public class Course {

    private List<Obstacle> list = new ArrayList<>();


    public void add(Obstacle obstacle) {

        if (obstacle == null)
            throw new IllegalArgumentException("obstacle == null");

        list.add(obstacle);
    }


    public void doIt(Team team) {

        System.out.println("Processing \"" + team.getName() + "\":");

        for (Competitor competitor : team) {
            
            for (Obstacle obstacle : list) {

                obstacle.doIt(competitor);

                // Выбыл с трассы
                if (!competitor.isOnDistance())
                    break;
            }
        }
    }
}
