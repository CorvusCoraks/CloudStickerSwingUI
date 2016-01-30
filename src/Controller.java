/**
 * Created by GreMal on 21.02.2015.
 */

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
* Компания - устройства, участвующие в обслуживании данной конкректной заметки (одного конкретного пользователя
* */
public class Controller {
    protected final static String DEFAULT_DATA_STAMP = "0000-00-00 00:00:00";
    protected final static float PROGRAM_VERSION = 0.02f;
    protected final static int MAX_CHARS_IN_LABEL = 25;
    protected final static int MAX_CHARS_IN_NOTE = 1000; // максимальное количество символов в заметке
    protected final static int CHARS_IN_INVITATION_PASS = 5; // количество символов в пригласительном пароле
    protected final static String OS_NAME = System.getProperty("os.name");
    protected final static String LAST_VER_FILE_LOCATION = "http://cn.gremal.ru/files/lastver/cloudsticker.zip";
    protected static TestInternetConnectionThread jerkThread;
    //protected List<ConnectedElements> connectionsModelWithGUI;
    //private Controller controller;
    protected static GUI gui = null;
    protected static Model model = null;
    // файл создаётся с первоначальными настройками во время инсталляции программы

    // Карта для накопления статусов, пока ещё не готово GUI


    // Key - deviceId, Value - deviceLabel
    //protected static Map<String, String> devic    esInCircle = new HashMap<String, String>();

    //static DeviceInfo thisDeviceInfo = new DeviceInfo();
    //protected static String thisDeviceId;
    //protected static String thisUserId;

    public static void main(String[] args) throws IOException, InterruptedException {
        // обновление файла start.jar
        File oldStartFile = new File("./start.jar");
        File newStartFile = new File("./new_start.jar");

        if(newStartFile.exists()){
            oldStartFile.delete();
            newStartFile.renameTo(oldStartFile);
        }

        // Если запуск программы произошёл не через start.jar, то перекидываем управление принудительно в start.jar
        //System.out.println("на входе в main");
        List<String> argsList = Arrays.asList(args);
        if(!argsList.contains("start")){
            //System.out.println("внтутри Контроллера");
            /* ------------------------------------------------------------- */
            Process proc = Runtime.getRuntime().exec("java -jar start.jar");
            return;
            /* ------------------------------------------------------------- */
        }
        //Internet.Result result = Internet.getNote("hkZ6gBomOdh6o9cX","D4gGlTVoScqSfvXZ");

        //boolean connected = Model.isInternerConnectionActive();
        /* Сначала модель и ГУИ создаются БЕЗ взаимных связей, до полной сборки самих себя.
         * Только после этого устанавливаются связи. */

        /* Так как, не равенство null ссылок на gui и model НЕ означает, что их формирование и инициализация завершены,
        * надо в этих объектах завести поля, которые будут true ПОСЛЕ полной инициализации этих объектов
        *
        * Вызов констурктора gui не означает, что в следующей строке gui будет не null
        */
        model = new Model();

        // чтение файла ini
        model.readInit();

        // Первоначальный запуск нити проверки связи с Интернет
        jerkThread = new TestInternetConnectionThread(gui);
        jerkThread.start();

        // запуск GUI
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                gui = new GUI();
            }
        });

        // Задержка, чтобы позволить GUI полностью сформироваться до инициализации модели
        while (gui == null) { Thread.sleep(10); }
        while (!gui.getReady()) { Thread.sleep(10); }

        // инициализация данных
        model.initialization();

        // model.isMODELready = true;

        // Первоначальный запуск нити проверки связи с Интернет
/*        jerkThread = new TestInternetConnectionThread(gui);
        jerkThread.start();*/

        /* Проверка наличия новой версии программы на сервере (номер последней версии хранится в файле lastver.txt)
        * и скачивание новой версии при необходимости */
        /* Версия является десятичной дробью */
/*
         if(InternetConnectionTest.isCloudReachable() == InternetConnectionTest.InternetConnectionMessage.YES) {
             boolean isRefreshNeeded = false;
            Internet.Result answer = Internet.getLastProgramVer();
            String ver = (String) answer.content;
            if (!ver.equals("null") && !ver.equals(PROGRAM_VERSION)) {
                gui.putNewStatusInStatusString(GUI.StatusSender.CONTROLLER, "New version CloudNotes present. Please, update!", 10);
                File fileName = new File("./lastversion.jar");
                // if(fileName.exists()){ fileName.delete(); }
                // Если файл новоё версии уже есть в каталоге программы, то скачиваеть обновление не следует.
                if (!fileName.exists()) {
                    byte[] fileContent = Internet.getLastVerCloudNotes(LAST_VER_FILE_LOCATION);
                    FileOutputStream fileOutputStream = new FileOutputStream(fileName);
                    fileOutputStream.write(fileContent);
                    fileOutputStream.close();
                }
            }
        }*/

        if(InternetConnectionTest.isCloudReachable() == InternetConnectionTest.InternetConnectionMessage.YES) {
            boolean isRefreshNeeded = false;
            Internet.Result answer = Internet.getLastProgramVer();
            float ver = Float.parseFloat((String) answer.content);
            if (PROGRAM_VERSION < ver) {
                //gui.putNewStatusInStatusString(GUI.StatusSender.CONTROLLER, "New version CloudNotes ready.", 10);
                File fileName = new File("./cloudsticker.zip");
                // Если файл новоё версии уже есть в каталоге программы, то скачиваеть обновление не следует.
                if (!fileName.exists()) {
                    byte[] fileContent = Internet.getLastVerCloudNotes(LAST_VER_FILE_LOCATION);
                    FileOutputStream fileOutputStream = new FileOutputStream(fileName);
                    fileOutputStream.write(fileContent);
                    fileOutputStream.close();
                }
            }
        }

        // Передача статистики
        if(InternetConnectionTest.isCloudReachable() == InternetConnectionTest.InternetConnectionMessage.YES) {
            Map<String, String> hash = new HashMap<String, String>();
            hash.put("os", OS_NAME);
            Internet.Result answer = Internet.sendStatistics(hash);
/*            String ver = (String) answer.content;
            if (!ver.equals("null") && !ver.equals(PROGRAM_VERSION)) {
                gui.putNewStatusInStatusString(GUI.StatusSender.CONTROLLER, "New version CloudNotes present. Please, update!", 10);
            }*/
        }
    }

}
