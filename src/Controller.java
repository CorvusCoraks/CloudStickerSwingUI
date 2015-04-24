/**
 * Created by GreMal on 21.02.2015.
 */
//import com.sun.java.util.jar.pack.*;
//import com.sun.java.util.jar.pack.Package;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.File;
import java.io.FileWriter;
import java.net.InetAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

/*
* Компания - устройства, участвующие в обслуживании данной конкректной заметки (одного конкретного пользователя
* */
public class Controller {
    protected final static String DEFAULT_DATA_STAMP = "0000-00-00 00:00:00";
    protected final static int MAX_CHARS_IN_LABEL = 25;
    protected final static int MAX_CHARS_IN_NOTE = 1000; // максимальное количество символов в заметке
    protected final static int CHARS_IN_INVITATION_PASS = 5; // количество символов в пригласительном пароле
    protected final static String OS_NAME = System.getProperty("os.name");
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

        //Internet.isTimeServerConnected();
        /* загоняем накопленные статусы в уже готовый GUI */
/*        for (Map.Entry<GUI.StatusSender, GUI.StatusStringObject> pair : Model.getLasyStatusForGui().entrySet()) {
            gui.putNewStatusInStatusString(pair.getKey(), pair.getValue().statusString, pair.getValue().showCounts);
        }*/

        //System.out.println(OS_NAME);

        //Model.calendar.getTimeZone().setRawOffset(0);
        // String[] str = Model.calendar.getTimeZone().getAvailableIDs();
        //Date temp = Model.calendar.getTime();
        //Model.calendar.getTimeZone().setRawOffset((-1)*Model.calendar.getTimeZone().getRawOffset());
        //Map<String, Date> temp = Internet.getTimeStamps("4u8b4XBmdt8v32Te","wFQVlXLRms6LYJRh");
        //System.out.println(Tools.getRandomID());
        //Internet.updateNote("4u8b4XBmdt8v32Te", "wFQVlXLRms6LYJRh", "update!", Tools.String2Date("2013-01-13 12:10:13"));
        // Map<Date, String> map = Internet.getNote("4u8b4XBmdt8v32Te", "wFQVlXLRms6LYJRh");
        //String temp = Tools.Date2String(new Date());
        //Internet com = new Internet();
        //Internet.test();
        //System.out.println(new Date().toString());
        //System.out.println((new StringBuffer()).append(" текущее время: "). append(new Date()).toString());

        //Date date = Tools.getCurrentGMTTime();
        //System.out.println(date.toString());

        //System.out.println(ZonedDateTime.now(ZoneOffset.UTC).toString());

/*        SimpleDateFormat gmtDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        gmtDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        //Current Date Time in GMT
        System.out.println("Current Date and Time in GMT time zone: " + gmtDateFormat.format(new Date()));
*/

        //Read more: http://javarevisited.blogspot.com/2012/01/get-current-date-timestamps-java.html#ixzz3WSI2Yt1I

        //System.out.println(Tools.getCurrentGMTTime().toString());
/*

        DateFormat dateFormat = DateFormat.getDateTimeInstance();
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        System.out.println(dateFormat.format(new Date()));
*/
/*        Date date = new Date();
        System.out.println(date.toString());
        System.out.println(Tools.Date2String(date));
        System.out.println(Tools.String2Date(Tools.Date2String(date)).toString());*/

        //new Internet.TestInternerConnectionThread(gui).run();
    }
/*
    protected static class ConnectedElementsArray {
        private List<ConnectedElements> list;

        private JComponent getJComponent(Model.DeviceInfo info, String className){return null;}
        private Model.DeviceInfo getDeviceInfo(JComponent component){return null;}

        protected JTextField getTextField(Model.DeviceInfo info){return null;}
        protected JTextArea getTextArea(Model.DeviceInfo info){return null;}
        protected JButton getButton(Model.DeviceInfo info){return null;}

        *//* Класс коннектор, объединяющий два соответствующих элемента в модели и GUI *//*
        protected static class ConnectedElements {
            // сторона gui
            protected JTextField field;
            protected JTextArea area;
            protected JButton button;
            // сторона модели
            protected Model.DeviceInfo deviceInfo;
        }
    }*/



}
