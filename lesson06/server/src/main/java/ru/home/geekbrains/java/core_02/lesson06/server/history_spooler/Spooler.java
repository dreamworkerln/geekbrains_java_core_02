package ru.home.geekbrains.java.core_02.lesson06.server.history_spooler;



/*
Тогда делаешь spooler

1) При запуске сервер вытаскивает все сообщения из таблицы БД
Далее при поступлении новых сообщений хранит их в буффере1 к-то время(или до заполнения буффера1 (коллекции))
после чего выгружает скопом весь буффер1 в БД.
и т.д.

Если сервак падает то пропадает то, что накапливалось в буффере1.


2) ..   при продключении нового клиента бегать каждый раз в базу не гуд,
но и хранить всю переписку в RAM тож не вариант.

как вариант - буффер2, в котором хранится n последних сообщений чата.
которые и выплевываются новому клиенту при подключении

и как в новостных лентах вк - жми кнопку еще
- тогда сервак сходит в базу и отдаст тебе еще более старых сообщений.

причем буффер1 и буффер 2 - это должен быть один и тот же буффер



------------------------------------------------------------------------------------------------------------




                              Буффер (очередь) длины N



                                                   Пишем новые данные в конец
                                                              |
###############################################################
|------------- N/2 ----------- |------------- N/2 ----------- |


При заполнении очереди целиком
(можно юзать ConcurrentSkipListMap пусть вырастет даже несколько длинее,
зато (возможно?)можно не использовать критические секции(семафоры)).


Берем левую часть очереди длиной N/2 и выгружаем в базу(при этом данные из очереди удаляем)
Также это должно происходить и по таймеру(на случай падения сервера)

                  Пишем новые данные в конец
                               |
################################
|------------- N/2 ----------- |





При подключении нового пользователя отдаем ему все сообщения,
находящиеся в правой части очереди (от текущего конца очереди влево N/2 штук или меньше (если очередь не доросла))

Если пользователь хочет более "древнюю" историю, то тогда он жмет кнопу "еще"
и вызывает
api getHistoryNext(long from(тут идентификатор id новости или timestamp), int count)
и сервер загружает из базы count сообщений и отдает клиенту.
*/




/*

    2) - не реализовано, т.е. все хранится в RAM

 */


import org.apache.log4j.Logger;
import ru.home.geekbrains.java.core_02.lesson06.server.entities.dao.HistoryDAO;

import java.lang.invoke.MethodHandles;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.locks.ReentrantLock;

public class Spooler extends Thread {

    private final static int SPOOL_THRESHOLD = 3;

    private static final Logger log = Logger.getLogger(MethodHandles.lookup().lookupClass());

    private ConcurrentNavigableMap<Integer, HistoryMsg> history;

    private ConcurrentNavigableMap<Integer, HistoryMsg> buffer = new ConcurrentSkipListMap<>();

    private ReentrantLock lock = new ReentrantLock();

    private int lastId = -1;


    public Spooler() {

        // load history
        history = HistoryDAO.load();
        lastId = HistoryDAO.getLastId();

    }

    @Override
    public void run() {


        try {

            // Timer
            while (!isInterrupted()) {

                Thread.sleep(1000000000);

                try {

                    spool();

                } catch (Exception e) {
                    log.error(e);
                }


            }
        }
        catch (Exception e) {
            log.error(e);
        }

        // on app exit try to save history and exit;
        try {
            spool();
        }
        catch (Exception e) {
            log.error(e);
        }
    }




    public void add(HistoryMsg hMsg) {
        try {

            buffer.put(hMsg.getId(), hMsg);
        }
        catch (Exception e) {
            log.error(e);
        }

        spool();
    }


    /**
     * Perform spooling
     */
    private void spool() {


        if (buffer.size() > SPOOL_THRESHOLD) {

            lock.lock();       // ENTER

            try {

                HistoryDAO.saveHistory(buffer);

                // copy buffer to history
                buffer.forEach(history::put);

                // if ok clear buffer
                buffer.clear();

            } catch (Exception e) {
                log.error(e);
            }
            finally {
                lock.unlock(); // LEAVE
            }
        }
    }

    public int getLastId() {
        return lastId;
    }

    public ConcurrentNavigableMap<Integer, HistoryMsg> getHistory() {

        return history;

    }


    public ConcurrentNavigableMap<Integer, HistoryMsg> getBuffer() {

        return buffer;
    }


}
