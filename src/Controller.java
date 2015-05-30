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
    protected final static String PROGRAM_VERSION = "0.0 alpha";
    protected final static int MAX_CHARS_IN_LABEL = 25;
    protected final static int MAX_CHARS_IN_NOTE = 1000; // максимальное количество символов в заметке
    protected final static int CHARS_IN_INVITATION_PASS = 5; // количество символов в пригласительном пароле
    protected final static String OS_NAME = System.getProperty("os.name");
    protected final static String LAST_VER_FILE_LOCATION = "http://cn.gremal.ru/files/lastversion.jar";
    protected static TestInternetConnectionThread jerkThread;
    //protected List<ConnectedElements> connectionsModelWithGUI;
    //private Controller controller;
    protected static GUI gui = null;
    protected static Model model = null;
    // файл создаётся с первоначальными настройками во время инсталляции программы

    // Карта для накопления статусов, пока ещё не готово GUI


    // Key - deviceId, Value - deviceLabel
    //protected static Map<String, String> devicesInCircle = new HashMap<String, String>();

    //static DeviceInfo thisDeviceInfo = new DeviceInfo();
    //protected static String thisDeviceId;
    //protected static String thisUserId;

    public static void main(String[] args) throws IOException, InterruptedException {
        // Если запуск программы произошёл не через start.jar, то перекидываем управление принудительно в start.jar
        //System.out.println("на входе в main");
        List<String> argsList = Arrays.asList(args);
        if(!argsList.contains("start")){
            //System.out.println("внтутри Контроллера");
            try{ Process proc = Runtime.getRuntime().exec("java -jar start.jar"); }
            catch(IOException ignore){/*NOP*/}
            return;
        }
        //Internet.Result result = Internet.getNote("hkZ6gBomOdh6o9cX","D4gGlTVoScqSfvXZ");

        //boolean connected = Model.isInternerConnectionActive();
        /* Сначала модель и ГУИ создаются БЕЗ взаимных связей, до полной сборки самих себя.
         * Только после этого устанавливаются связи. */
        model = new Model();

        // чтение файла ini
        model.readInit();

        // запуск GUI
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                gui = new GUI();
            }
        });

        // Задержка, чтобы позволить GUI полностью сформироваться до инициализации модели
/*        while (!gui.isGUIready) {
            Thread.sleep(10);
        }*/
        while (gui == null) {
            Thread.sleep(10);
        }

        // инициализация данных
        model.initialization();

        model.isMODELready = true; // в будущем переделать в GUI тест на null, как сделано выше

        // Первоначальный запуск нити проверки связи с Интернет
        jerkThread = new TestInternetConnectionThread(gui);
        jerkThread.start();

        /* Проверка наличия новой версии программы на сервере (номер последней версии хранится в файле lastver.txt)
        * и скачивание новой версии при необходимости */
        if(InternetConnectionTest.isCloudReachable() == InternetConnectionTest.InternetConnectionMessage.YES) {
            Internet.Result answer = Internet.getLastProgramVer();
            String ver = (String) answer.content;
            if (!ver.equals("null") && !ver.equals(PROGRAM_VERSION)) {
                gui.putNewStatusInStatusString(GUI.StatusSender.CONTROLLER, "New version CloudNotes present. Please, update!", 10);
                File fileName = new File("./lastversion.jar");
                // if(fileName.exists()){ fileName.delete(); }
                // Если файл новоё версии уже есть в каталоге программы, то скачиваеть обновление не следует.
                if(!fileName.exists()){
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
            hash.put("os", System.getProperty("os.name"));
            Internet.Result answer = Internet.sendStatistics(hash);
/*            String ver = (String) answer.content;
            if (!ver.equals("null") && !ver.equals(PROGRAM_VERSION)) {
                gui.putNewStatusInStatusString(GUI.StatusSender.CONTROLLER, "New version CloudNotes present. Please, update!", 10);
            }*/
        }
    }

}
