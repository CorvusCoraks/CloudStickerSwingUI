/**
 * Created by GreMal on 21.02.2015.
 */

import ru.gremal.cs.common.interfaces.CoreSide;
import ru.gremal.cs.common.interfaces.UISide;
import ru.gremal.cs.common.tools.CommonTools;
import ru.gremal.cs.common.tools.InternetConnectionMessage;
import ru.gremal.cs.common.tools.StatusSender;
import ru.gremal.cs.common.ui.AbstractUI;
import ru.gremal.cs.common.ui.AbstractUIControl;
//import ru.gremal.cs.common.ui.AbstractUI;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import java.awt.*;
import java.awt.event.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.LogRecord;
//import CloudStickerCore;

public class GUI extends AbstractUI {

    //private CoreSide core;

    public static void main(String[] args) {
        // использование не планируется. Модуль, фактически, используется как библиотека
    }

    //private int mainWindowWidth;
    //private int mainWindowHeight;
    //private int mainWindowXPosition;
    //private int mainWindowYPosition;
    //private boolean isReady = false; // флаг готовности UI к работе
    private MainWindow frame;

    // файл языковой локализации
    final static private String locFileName = "language.ini";
    // карта языковой локализации
    private Map<String, String> localisation = new HashMap<String, String>();
    /* Инициализация модели должна происходить после завершения создания GUI, так как модель получает ссылки на некоторые
    * элементы GUI. То есть, инициализация модели происходит только после того, когда флаг isGUIready станет равным true */
    //protected static boolean isGUIready = false;
    //protected Map<JComponent, > editMarkersArray;

    /*
    * В классе главного окна даётся только раскладка компонентов\
    * Планируемая раскладка:
    * Главное окно делится на три зоны: север, центр, юг.
    * Зона север, возможно, будет резервной, пустой.
    * В зоне центр, будет помещатся набор вкладок: заметка, компания, настройки и т. п.
    * в зоне юг будет подвал: кнопка синхронизации заметки, строка статуса, зона рекламы (возможно).
    * */
    public class MainWindow extends JFrame
    {
        private boolean isCloseOperationRunned = false; // запущен ли протокол закрытия окна программы?
        final static private int TIME_FOR_ONE_STATUS = 4000; // миллисикунды, время показа одного статуса
        private inBoxPairTextFieldAndButton[] arrayTextFieldsAndButtons = new inBoxPairTextFieldAndButton[getMaxCompanyCount() - 1];
        private inBoxPairTextFieldAndButton currentDevice = new inBoxPairTextFieldAndButton("Этот Ваш компьютер");
        private inBoxPairTextFieldAndButton invitationInputBox = new inBoxPairTextFieldAndButton("Ввести пароль для присоединения");
        //private JButton synchronisationButton = new JButton("Синхронизация через - 15 сек.");
        private JButton synchronisationButton = new JButton("Синхронизировать");
        private SwingUI.STextArea noteArea = new SwingUI.STextArea();
        //private JTextArea noteArea = new JTextArea();
        /* стркока статуса не может быть пустой, иначе она сразу исчезает. То есть раскладка элементов в окне
         * начинает прыгать */
        private static final String BLANCK_STATUS_STRING = "   ";
        //private boolean isGUIactive = false;
        // обязательно синхронизировать обращение к этому массиву!
        private Map<StatusSender, StatusStringObject> statusStringsArray = new HashMap<StatusSender, StatusStringObject>(); // идентификатор отправителя, статусное сообщениеж
        private JLabel statusStringLabel = new JLabel("Строка статуса");
        //private JButton downloadButton = new JButton("Скачать свежий CloudSticker");
        //private JTextField[] textFieldsWithDeviceNames = new JTextField[Controller.MAX_COMPANY_COUNT - 1];
        //private JButton[] buttonsWithDeviceNames = new JButton[Controller.MAX_COMPANY_COUNT - 1];
        //private JTextField invitationToCompanyTextField = new JTextField();
        //private JButton invitationToCompany

        /* Базовая (корневая) раскладка главного окна */
        protected MainWindow(String str)
        {
            super(str);
            this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            // добавляем перехват событий главного окна
            //this.addWindowListener(new MyWindowListener());
            // добавляем раскладку главного окна, так как раскладка по умолчанию нас не устраивает?
            BorderLayout mainLayout = new BorderLayout();
            mainLayout.setVgap(5);
            this.setLayout(mainLayout);
            //Panel header = new Panel();
            //Panel body = new Panel();

            //center.add(BorderLayout.CENTER, pane);


            //body.add(BorderLayout.CENTER, new JTextArea());

            // Добавить верхнуюю часть окна (заголовок)
            //this.add(BorderLayout.NORTH, new JTextArea("F"));
            // Текстовая область в центре главного окна
            this.add(BorderLayout.CENTER, getMainTabbedPane());
            //this.add(BorderLayout.CENTER, new JTextArea("D"));
            // Подвал главного окна
            //this.add(BorderLayout.SOUTH, new JTextArea("J"));
            this.add(BorderLayout.SOUTH, getMainFooter());

            this.addListeners();
            (new rotateStausList()).start();
            //isGUIready = true;
            setReady(true);
        }

        private JTabbedPane getMainTabbedPane(){
            // Раскладка элементов в центральной зоне главного окна
            //JPanel center = new JPanel(new BorderLayout());
            JTabbedPane pane = new JTabbedPane();
            pane.addTab("Заметка", getNotePane());
            pane.addTab("Компания", getCompanyPane());
            pane.addTab("О программе...", getAboutPane());
            return pane;
        }

        private JPanel getMainFooter(){
            // Раскладка элементов в подвале главного окна
            JPanel footer = new JPanel(new BorderLayout());
            //footer.setLayout(new BorderLayout());

            footer.add(BorderLayout.NORTH, synchronisationButton);
            footer.add(BorderLayout.CENTER, statusStringLabel);
            //footer.add(BorderLayout.SOUTH, new JLabel("Реклама"));
            footer.add(BorderLayout.SOUTH, new JLabel());
            return footer;
        }

        /* Панель с текстом заметки*/
        private JPanel getNotePane(){
            JPanel panel = new JPanel(new BorderLayout());
            noteArea.setLineWrap(true);
            noteArea.setWrapStyleWord(true);
            //noteArea.
            panel.add(BorderLayout.CENTER, new JScrollPane(noteArea));
            return panel;
        }

        /* Панель со списком включённых устройств */
        private JPanel getCompanyPane(){
            /* 1. создаём вертикальный Box col
            *  2. создаём горизонтальный Box куда вкладываем одно текстовое поле и одну кнопку
            *  3. повторяем п. 2 необходимое число раз, последовательно вкладывая получившиеся горизонтальные боксы
            *  в вертикальный, создавая стопку из строк и кнопок
            *  4. получившийся вертикальный бокс выкладываем на панель
            *  5. После вертикального бокса, добавляем ещё одну пару текстовое поле - кнопка (для ввода приглашения) */
            JPanel panel = new JPanel(new BorderLayout());
            //panel.setBorder(new TitledBorder("Заголовок"));
            // основной вертикальный бокс на панели закладок в разделе Круг-Компания
            Box megaBox = Box.createVerticalBox();
            { // добавить в МегаБокс поле с именем (меткой) данного устройства
                currentDevice.getButton().setVisible(false);
                //currentDevice.getTextField().setText(Controller.thisDeviceInfo.deviceLabel);
                megaBox.add(currentDevice.getRow());
            }
            { // вертикальный бокс для полей и кнопок других устройств круга
                Box col = Box.createVerticalBox();
                col.setBorder(new TitledBorder("Присоединённые устройства"));

                for (int i = 0; i < arrayTextFieldsAndButtons.length; i++) {
                    arrayTextFieldsAndButtons[i] = new inBoxPairTextFieldAndButton();
                    col.add(arrayTextFieldsAndButtons[i].getRow());
                }
                megaBox.add(col);
            }
            { // добавить в МегаБокс поле и кнопку для ввода пригласительного кода
                invitationInputBox.getButton().setText(localisation.get("btEnteringToCircle"));
                megaBox.add(invitationInputBox.getRow());
            }
            /* данная промежуточная панель (scrollBarPanel) необходима, так как без неё JScrollPane
            * растягивается по вертикали на максимум таба (согласно BorderLayout.CENTER),
            * растягивая попутно всё своё содержимое (текстовые поля),
            * что выглядит жутко. В этом же случае, JScrollPane растягивает вложенную scrollBarPanel (и хрен с ней),
            * а вложенные элементы (уложенные по BorderLayout.NORTH), остаются недеформированными */
            JPanel scrollBarPanel = new JPanel(new BorderLayout());
            scrollBarPanel.add(BorderLayout.NORTH, megaBox);
            //sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
            panel.add(BorderLayout.CENTER, new JScrollPane(scrollBarPanel));
            //panel.add(BorderLayout.NORTH, megaBox);
            return panel;
        }

        /* Панель "О программе" */
        private JPanel getAboutPane(){
            JPanel panel = new JPanel(new BorderLayout());
            Color backgroundColor = panel.getBackground(); // сохраняем стандартный фоновый цвет
            Box col = Box.createVerticalBox();
            col.add(new JLabel("CloudSticker"));
            col.add(new JLabel("(c) Michael Vassin"));
            col.add(new JLabel("2015"));
            JTextArea helpInfoArea = new JTextArea();
            //helpInfoArea.setColumns(20);
            helpInfoArea.setLineWrap(true);
            helpInfoArea.setWrapStyleWord(true);
            helpInfoArea.setBackground(backgroundColor); // применяем стандартный фоновый цвет
            helpInfoArea.setText("Внимание! Программа не хранит Вашу заметку на этом устройстве!\n\n" +
                    "Если устройство будет выключено без синхронизации с облаком, " +
                    "все изменения Вашей заметки будут утеряны!\n\n" +
                    "Внимание! Данная программа не приспособлена для работы с приватными данными, " +
                    "так как ваша заметка не шифруется, а передаётся и хранится в открытом виде!\n\n" +
                    "http://cloudsticker.gremal.ru/");
            helpInfoArea.setEditable(false);
            col.add(helpInfoArea);
            /* промежуточная панель для скроллбара. Для чего она нужна, смотри раскладку по закладке с компанией */
            //JPanel scrollBarPanel = new JPanel(new BorderLayout());
            //scrollBarPanel.add(BorderLayout.NORTH, col);
            //JScrollPane jsp = new JScrollPane(scrollBarPanel);
            //jsp.setLayout(new ScrollPaneLayout());
            //jsp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            // JButton downloadButton = new JButton("Скачать свежий CloudSticker");
            //col.add(downloadButton);
            panel.add(BorderLayout.CENTER, col);

            //panel.add(col);
            return panel;
        }

        //protected String getCurrentDeviceName(){ return currentDevice.getTextField().getText(); }
        //protected void setCurrentDeviceName(String name){ currentDevice.getTextField().setText(name); }
        //protected inBoxPairTextFieldAndButton getInvitationInputBox(){ return invitationInputBox; }
        //protected inBoxPairTextFieldAndButton[] getArrayTextFieldsAndButtons(){ return arrayTextFieldsAndButtons; }
        /* вспомогательные методы. Интерфейс выдачи данных в класс GUI */

        protected inBoxPairTextFieldAndButton getCurrentDeviceBox(){ return currentDevice; }
        protected inBoxPairTextFieldAndButton[] getOtherDevecesBoxes(){ return arrayTextFieldsAndButtons; }
        protected inBoxPairTextFieldAndButton getInvitationInputBox() { return invitationInputBox; }
        protected JButton getSynchronisationButton(){ return synchronisationButton; }
        // protected JTextField getTextFieldByButton(JButton button){}
        /* Класс представляет текстовое поле, справа кнопка. И всё это в Боксе.
        *  для вставки Бокса в интерфейс используется метот getRow*/
        class inBoxPairTextFieldAndButton{
            // ОБЯЗАТЕЛЬНО ограничить максимальную длину имени устройства
            private Box row = Box.createHorizontalBox();
            //private JTextField jtf = new JTextField();
            //private JButton jb = new JButton(localisation.get("btInviteToCircle"));

            private SwingUI.STextField stf = new SwingUI.STextField();
            private SwingUI.SButton sb = new SwingUI.SButton();


            // этот конструктор делает Бокс без рамки
            protected inBoxPairTextFieldAndButton() {
                sb.setSText(localisation.get("btInviteToCircle"));
                row.add(stf);
                row.add(sb);
                //row.add(jtf);
                //row.add(jb);
            }

            // с помощью этого конструктора делается Бокс с рамкой с заголовком str
            protected inBoxPairTextFieldAndButton(String str){
                sb.setSText(localisation.get("btInviteToCircle"));
                row.add(stf);
                row.add(sb);
                //row.add(jtf);
                //row.add(jb);
                row.setBorder(new TitledBorder(str));
            }

            protected SwingUI.STextField getTextField() { return stf; }

            protected SwingUI.SButton getButton() { return sb; }

            //protected JTextField getTextField() { return jtf; }

            //protected JButton getButton() { return jb; }

            protected Box getRow() { return row; }
        }

        private void addListeners(){
            // добавляем перехват событий главного окна
            this.addWindowListener(new MyWindowListener());
            this.addWindowFocusListener(new myWindowFocusListener());
            this.synchronisationButton.addActionListener(new SynchronisationButtonListener());
            this.currentDevice.getTextField().addCaretListener(new DeviceLabelTextFieldListener());
            // слушатели в списке устройств
            for(int i = 0; i < arrayTextFieldsAndButtons.length; i++) {
                arrayTextFieldsAndButtons[i].getTextField().addCaretListener(new DeviceLabelTextFieldListener());
                arrayTextFieldsAndButtons[i].getButton().addActionListener(new InviteOrKickButtonListener());
            }
            //this.arrayTextFieldsAndButtons
            this.noteArea.addCaretListener(new NoteTextAreaListener());
            this.invitationInputBox.getButton().addActionListener(new EnterToCircleButtonListener());
            //this.downloadButton.addActionListener(new DownloadButtonListener());
        }
/*

        class DownloadButtonListener implements ActionListener{
            public void actionPerformed(ActionEvent Ev){
                new newThread().start();
            }

            class newThread extends Thread
            {
                public void run(){

                    try{
                        java.awt.Desktop.getDesktop().open(new File(Controller.LAST_VER_FILE_LOCATION));
                    }catch(IOException ignore){*/
/*NOP*//*
}
                }
            }
        }
*/

        /* Перехватчик сообщений главного окна */
        class MyWindowListener extends WindowAdapter {
            /*
                Обработчик события "закрытие главного окна программы". То есть, тут происходит деницициализация приложения.
            */
            public void windowClosing(WindowEvent Ev) {
                //System.out.println("Обработчик события закрытия окна");
                /* Устанавливаем переменную, чтобы повторные нажатия на клавишу закрытия окна не порождали
                повторых запусков процедуры закрытия окна. Зачем?
                Затем, что при закрытии запускается синхронизация заметки,
                а это - новые бессмысленные нити и уже и так не быстро. */
                if(isCloseOperationRunned){ return; }else{ isCloseOperationRunned = true; }

                // блокируем окно
                setUIDisable();
                // запись параметров графического интерфейса в массив, для дальнейшего занесения в ini-файл
                /*
                putIniDataInMap("UI_mainWindowXPosition", String.valueOf((int) frame.getLocation().getX()));
                putIniDataInMap("UI_mainWindowYPosition", String.valueOf((int) frame.getLocation().getY()));
                putIniDataInMap("UI_mainWindowWidth", String.valueOf(frame.getWidth()));
                putIniDataInMap("UI_mainWindowHeight", String.valueOf(frame.getHeight()));
                */

                core.putPairToIniDataMap("mainWindowXPosition", String.valueOf((int) frame.getLocation().getX()));
                core.putPairToIniDataMap("mainWindowYPosition", String.valueOf((int) frame.getLocation().getY()));
                core.putPairToIniDataMap("mainWindowWidth", String.valueOf(frame.getWidth()));
                core.putPairToIniDataMap("mainWindowHeight", String.valueOf(frame.getHeight()));

                // Controller.gui.frame.setCursor(new Cursor(Cursor.WAIT_CURSOR));


                Thread thread = new newThread();
                thread.start();
                /* Без джойна не нет закрывающей синхронизации. Видимо, GUI закрывается слишком рано. */
                try {
                    thread.join();
                } catch (InterruptedException ignore) { /*NOP*/ }
                setUIEnable(); // может убрать эту активацию за ненадобностью?
            }

            class newThread extends Thread {
                public void run() {
                    // Устанавливаем для Core флаг на закрытие приложения
                    // CommunicationChannal CloseWindowChannal;
                    // boolean waiting = true;
                    /*
                    while(true) {
                        // цикл на время пока данный канал кем-то занят. Т. е. ждём освобождения канала
                        // проверка на доступность и необходимые в этом случае действия, производится в одном блоке synchronized
                        synchronized (getCloseWindowCommandAgreeChannal()) {
                            if (!getCloseWindowCommandAgreeChannal().isOrder()) {
                                getCloseWindowCommandAgreeChannal().setOrder(true);
                                break;
                            }
                        }
                        try{ sleep(10); }catch (InterruptedException ignore){}
                    while(!getCloseWindowCommandAgreeChannal().isReport()){
                        // пока из Core не пришло разрешение на закрытие UI, UI заморожено
                        try{ sleep(10); }catch (InterruptedException ignore){}
                    }
                    synchronized (getCloseWindowCommandAgreeChannal()) {
                        getCloseWindowCommandAgreeChannal().setReport(false);
                        getCloseWindowCommandAgreeChannal().setOrder(false);
                    }
                        */
                    // setCloseWindowCommand(true);
                    try {
                        core.startSynchronization();
                    } catch (InterruptedException ignore) {
                        /* NOP */
                    }
                    core.writeInit();
                    /*
                    setUIPaused(true);
                    while(getUIPaused()){
                        try{ sleep(10); }catch (InterruptedException ignore){  }
                    }
                    */
                    /* */
                    /*
                    Controller.gui.setFrameDisable();
                    Controller.model.startSynchronization();
                    Controller.model.writeInit();
                    Controller.gui.setFrameEnable();
                    */
                }
            }
        }

        /* перехват события главного окна "в фокусе - не в фокусе". Почему-то, через WindowAdapter не ловится
        * Управление периодическии тестом связи:
        * а. Проверка на фокус. Если окно не в вокусе, проверка не нужна
        * б. Если нет ни одной метки wasChanged = true, то проверка не нужна
        * в. Если есть хоть одна метка wasChanged = true, то запускаем процесс периодической проверки связи.
        * г. Если произошла синхронизация, то есть, все wasChaged = false, прекращаем периодическую провекрку.
        * д. В случае пропадания связи на каком-либо этапе, замораживаем ввод в поля приложения,
        * продолжая периодическую проверку связи
        * е. Согласно принципу примата серверной записи над пользовательской, если после появления связи выясняется,
        * что заметка/метка на сервере старше, то при синхронизации этот вариант появляется у клиента.*/
        class myWindowFocusListener implements WindowFocusListener{
            @Override
            public void windowGainedFocus(WindowEvent e) {
                //System.out.println("Окно в фокусе.");
                // if(getIsWasChanged()){
                if (core.isWasChangedTrue()&&!core.isJerkThreadActive()){
                //if((getIsNoteWasChanged())||(getIsDeveceLabelWasChanged())){
                    // чтобы при активации события не запустить нить повторно (если она уже запущена)
                    //Controller.jerkThread.controller.wakeUp();
                    // ПРОСНУТЬСЯ!
                    //setJerkThreadWakeUpCommand(true);
                    core.jerkThreadWakeUp();
                }
            }

            @Override
            public void windowLostFocus(WindowEvent e) {
                //System.out.println("Окно не в фокусе.");
                //Controller.jerkThread.controller.pause();
                // СПАТЬ!
                //setJerkThreadWakeUpCommand(false);
                core.jerkThreadSleep();
            }
        }

        /* Перехват события "нажатие кнопки входа в круг" */
        class EnterToCircleButtonListener implements ActionListener
        {
            public void actionPerformed(ActionEvent Ev){
                new newThread().start();
            }

            class newThread extends Thread
            {
                public void run(){
                    setUIDisable();
                    core.enterToCircleButtonPressed();
                    /*
                    setEnterToCircleButtonPressed(true);
                    try{
                        while (isEnterToCircleButtonPressed()){ sleep(10); }
                    }catch(InterruptedException ignore){}
                    */
                    //CoreSide.EnterToCircleButtonPressed();
                    // пароль отработан, можно удалить его из GUI
                    frame.getInvitationInputBox().getTextField().setSText("");
                    setUIEnable();
                }
            }
        }

        /* Перехват события "нажатие кнопки Синхронизации */
        /* todo после нажатия кнопки Синхронизация, Core должен запрашивать содержимое полей UI, чтобы сохранить свежайшие данные */
        class SynchronisationButtonListener implements ActionListener
        {
            public void actionPerformed(ActionEvent Ev){
                new newThread().start();
            }

            class newThread extends Thread
            {
                public void run(){
                    setUIDisable();
                    //CoreSide.startSynchronization();
                    try {
                        core.startSynchronization();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    /*
                    setStartSynchronizationButtonPressed(true);
                    try{
                        while (isStartSynchronizationCommand()){ sleep(10); }
                    }catch(InterruptedException ignore){}
                    */
                    setUIEnable();
                }
            }
        }

        /* Перехват события "нажатие кнопки Пригласить/Выгнать*/
        class InviteOrKickButtonListener implements ActionListener
        {
            public void actionPerformed(ActionEvent Ev){
                new newThread(Ev).start();
            }

            class newThread extends Thread
            {
                ActionEvent ev;

                public void run() {
                    setUIDisable();
                    //Controller.model.InviteOrKickButtonPressed((JButton) ev.getSource());
                    //CoreSide.InviteOrKickButtonPressed((AbstractUIControl) ev.getSource());
                    core.inviteOrKickButtonPressed((AbstractUIControl) ev.getSource());
                    /*
                    setInviteOrKickButtonPressed((AbstractUIControl) ev.getSource());
                    try{
                        while (InviteOrKickButtonPressed() != null){ sleep(10); }
                    }catch(InterruptedException ignore){}
                    */
                    setUIEnable();
                }

                public newThread() {}

                public newThread(ActionEvent Ev){ this.ev = Ev; }
            }
        }

        // Перехват события "Движение курсора по полю"
        class DeviceLabelTextFieldListener implements CaretListener
        {
            public void caretUpdate(CaretEvent Ev){
                new newThread(Ev).start();
            }

            class newThread extends Thread
            {
                CaretEvent ev;

                public void run(){
                    // SwingUI.STextField stf = (SwingUI.STextField) ev.getSource();
                    /* При каждом срабатывании данного события, запускается отдельная нить, с инкрементацией соответствующего счётчика нитей */
                    new Thread(){
                        /* todo возможно излишняя нить в нити */
                        // запускаем нить отсчёта паузы с момента данного изменения
                        SwingUI.STextField stf = (SwingUI.STextField) ev.getSource();
                        @Override
                        public void run() {
                            try{
                                //super.run();
                                // увеличиваем счётчик процессов
                                stf.incChangeEventCounter();
                                // ждём отведённое время
                                sleep(FIELD_CHANGE_TIMEOUT);
                                // уменьшаем счётчик процессов
                                // int counter = stf.decChangeEventCounter();
                                // если на счётчике ноль, значит, это - последняя нить. Интервал времени от последнего изменения выдержан.
                                if(stf.decChangeEventCounter() == 0){
                                    //setIsDeviceLabelWasChanged(true);
                                    core.setDeviceLabelWasChangedFlagToTrue(stf);
                                }
                            }catch (InterruptedException ignore){}
                        }
                    }.start();
                    //if(CoreSide.isDeviceLabelEquals(stf)) {
                    //CoreSide.setDeviceLabelWasChangedFlagToTrue(stf);
                    //}
                    /*
                    JTextField jtf = (JTextField) ev.getSource();
                    if(Controller.model.isDeviceLabelEquals(jtf)) {
                        // меняем флаг изменения только тогда, когда реально содержимое метки в текстовом поле отличается от содержимого метки в модели
                        Controller.model.setDeviceLabelWasChangedFlagToTrue(jtf);
                    }
                    */
                }

                public newThread() {}

                public newThread(CaretEvent Ev){ this.ev = Ev; }
            }
        }

        // Перехват события "Движение курсора по текстовой области заметки"
        class NoteTextAreaListener implements CaretListener
        {
            public void caretUpdate(CaretEvent Ev){
                new newThread().start();
            }

            class newThread extends Thread
            {
                public void run(){
                    new Thread(){
                        /* todo возможно излишняя нить в нити */
                        // запускаем нить отсчёта паузы с момента данного изменения
                        @Override
                        public void run() {
                            try{
                                //super.run();
                                // увеличиваем счётчик процессов
                                // stf.incChangeEventCounter();
                                frame.noteArea.incChangeEventCounter();
                                // ждём отведённое время
                                sleep(FIELD_CHANGE_TIMEOUT);
                                // уменьшаем счётчик процессов
                                // int counter = stf.decChangeEventCounter();
                                // если на счётчике ноль, значит, это - последняя нить. Интервал времени от последнего изменения выдержан.
                                if(frame.noteArea.decChangeEventCounter() == 0){
                                    //while (frame.noteArea.getIsNoteWasChanged()){
                                        /* кто-то уже выставил этот флаг, а значит, возможно,
                                        уже запущен процесс связи с Core и наши изменения могут "пролететь" мимо.
                                         */
                                        //sleep(5);
                                    //}
                                    //setIsNoteWasChanged(true);
                                    core.setNoteWasChangedFlagToTrue();
                                }
                            }catch (InterruptedException ignore){ /*NOP*/ }
                        }
                    }.start();

                    // меняем флаг изменения только тогда, когда реально содержимое заметки в текстовом поле отличается от содержимого заметки в модели
                    //if(!CoreSide.isNoteEquals(frame.noteArea.getText())){ CoreSide.setNoteWasChangedFlagToTrue(); }
                    int chars = getMaxCharsInNote() - frame.noteArea.getText().length();
                    if (chars > 0){frame.statusStringsArray.put(StatusSender.NOTE_LENGTH_CONTROL, new StatusStringObject(String.format("Остаток - %d символов", chars)));}
                    else if(chars < 0){ frame.statusStringsArray.put(StatusSender.NOTE_LENGTH_CONTROL, new StatusStringObject(String.format("Лишние - %d символов", (-1)*chars))); }
                    else{ frame.statusStringsArray.remove(StatusSender.NOTE_LENGTH_CONTROL); }
                }
            }
        }

        class rotateStausList extends Thread{
            public void run(){
                try{
                    // временный массив. Копия основного массива статусов
                    Map<StatusSender, StatusStringObject> tempMap = new HashMap<StatusSender, StatusStringObject>();
                    while(true){
                        synchronized(statusStringsArray){
                            for (Map.Entry<StatusSender, StatusStringObject> pair : statusStringsArray.entrySet()) {
                                int step = 500; // миллисикунды
                                int i = 0;
                                // цикл while необходим, так как содержимое строки статуса может меняться прямо во время его показа
                                while(i < TIME_FOR_ONE_STATUS) {
                                    statusStringLabel.setText(pair.getValue().statusString);
                                    i += step;
                                    Thread.sleep(step);
                                }
                                // уменьшаем срок жизни положительных статусов
                                if(pair.getValue().showCounts != -1){pair.getValue().showCounts--;}
                                // заполняем временный массив.
                                tempMap.put(pair.getKey(), pair.getValue());
                            }
                            // обходим временный массив, удаляя из основного все "дохлые" статусы
                            for(Map.Entry<StatusSender, StatusStringObject> pair : tempMap.entrySet()){
                                if(pair.getValue().showCounts == 0){ statusStringsArray.remove(pair.getKey()); }
                            }
                        }
                        tempMap.clear();
                        // стираем статус в строке
                        statusStringLabel.setText((new StatusStringObject(BLANCK_STATUS_STRING)).statusString);
                        Thread.sleep(10); // пауза на изменение массива статусных строк
                    }
                }catch(NullPointerException ex){ /* Увы, GUI закрыт */ return;}
                catch(InterruptedException ex){ /* Что с этим делать? */ (new rotateStausList()).run(); }
            }
        }

        //protected StatusStringObject getNewStatusStringObject(String ){ return };
    }

    /* Это функция проверки связи для вызова из внешних модулей. Формирование статусной строки в GUI,
    запуск процедур активации и деактивации элементов GUI */
    public void setInternetConnectionStatuses(InternetConnectionMessage status){
        //InternetConnectionTest.InternerConnectionMessage status = InternetConnectionTest.isCloudReachable();

        if(status != InternetConnectionMessage.YES) {
            if (isFrameEnabled()) { setUIDisable(); }

            if(status == InternetConnectionMessage.CLOUD_NOT_FOUND){
                putNewStatusInStatusString(StatusSender.TEST_INTERNET_CONNECTION, "Облако не отвечает.");
            }else if(status == InternetConnectionMessage.NO){
                putNewStatusInStatusString(StatusSender.TEST_INTERNET_CONNECTION, "Доступ в интернет отсутствует.");
            }
            //return false;
        }else {
            if (!isFrameEnabled()) {
                setUIEnable();
            }
            putNewStatusInStatusString(StatusSender.TEST_INTERNET_CONNECTION, "Облако на связи.", 5);
        }

        //return true;
    }

    /* Класс-обёртка над строкой. Необходимо, чтобы карта статусов в поле Value держала этот объект.*/
    static class StatusStringObject{
        protected String statusString;
        protected int showCounts = -1; // если -1, то статус показывается вечно, то есть, пока его не убьёт отправитель
        protected StatusStringObject(String str){ this.statusString = str; }
        protected StatusStringObject(String str, int count){ this.statusString = str; this.showCounts = count; }
    }

    protected GUI(CoreSide core)
    {
        this.core = core;
        //this.setCoreVersion(coreVersion);
        this.setCoreVersion(this.core.getCoreVersion());
        this.setUIVersion(0.01f);
        this.setLogFileHandler();

        // считывание языковых данных из файла идёт первым, так как эти данные нужны уже при создании объекта главного окна
        //CoreSide.getLogFileHandler().publish(new LogRecord(CoreSide.getLogLevel(), "GUI. Before localisation."));
        localisation();
        // Map локализации заполнена? Теперь можно создавать главное окно
        // todo версию можно установить только после создания GUI, так как Core только после этого и сможет передать иноформацию о версии.
        frame = new MainWindow(String.format("GreMal's CloudSticker, ver. %s", this.getCoreVersion()));
        setInitGUIParameters();
        //localisation();
        frame.setVisible(true);
    }

    /* Функция читает планируемые параметры GUI из соответствующего Мэпа контроллера */
    public void setInitGUIParameters()
    {
        // Устанавливаем размеры окна
        if((core.getPairFromIniDataMap("mainWindowWidth") != null)&&(core.getPairFromIniDataMap("mainWindowHeight") != null)){
            frame.setSize(Integer.parseInt(core.getPairFromIniDataMap("mainWindowWidth")),
                    Integer.parseInt(core.getPairFromIniDataMap("mainWindowHeight")));
        }else{
            core.putPairToIniDataMap("mainWindowWidth", "200");
            core.putPairToIniDataMap("mainWindowHeight", "200");
        }

        // Устанавливаем положение окна
        if((core.getPairFromIniDataMap("mainWindowXPosition") != null)&&(core.getPairFromIniDataMap("mainWindowYPosition") != null)){
            frame.setLocation(Integer.parseInt(core.getPairFromIniDataMap("mainWindowXPosition")),
                    Integer.parseInt(core.getPairFromIniDataMap("mainWindowYPosition")));
        }else{
            core.putPairToIniDataMap("mainWindowXPosition", "0");
            core.putPairToIniDataMap("mainWindowYPosition", "0");
        }

        /*
        // Устанавливаем размеры окна
        if((getIniDataFromMap("UI_mainWindowWidth") != null)&&(getIniDataFromMap("UI_mainWindowHeight") != null)){
            frame.setSize(Integer.parseInt(getIniDataFromMap("UI_mainWindowWidth")),
                    Integer.parseInt(getIniDataFromMap("UI_mainWindowHeight")));
        }else{
            putIniDataInMap("UI_mainWindowWidth", "200");
            putIniDataInMap("UI_mainWindowHeight", "200");
        }

        // Устанавливаем положение окна
        if((getIniDataFromMap("UI_mainWindowXPosition") != null)&&(getIniDataFromMap("UI_mainWindowYPosition") != null)){
            frame.setLocation(Integer.parseInt(getIniDataFromMap("UI_mainWindowXPosition")),
                    Integer.parseInt(getIniDataFromMap("UI_mainWindowYPosition")));
        }else{
            putIniDataInMap("UI_mainWindowXPosition", "0");
            putIniDataInMap("UI_mainWindowYPosition", "0");
        }
        */
        /*
        // Устанавливаем размеры окна
        if ( Controller.model.iniData.containsKey("mainWindowWidth") && Controller.model.iniData.containsKey("mainWindowHeight") )
        {} else
        {
            Controller.model.iniData.put("mainWindowWidth", "200");
            Controller.model.iniData.put("mainWindowHeight", "200");
        }
        frame.setSize(Integer.parseInt(Controller.model.iniData.get("mainWindowWidth")),
                Integer.parseInt(Controller.model.iniData.get("mainWindowHeight")));

        // Устанавливаем положение окна
        if ( Controller.model.iniData.containsKey("mainWindowXPosition") && Controller.model.iniData.containsKey("mainWindowYPosition") )
        {} else
        {
            Controller.model.iniData.put("mainWindowXPosition", "0");
            Controller.model.iniData.put("mainWindowYPosition", "0");
        }
        frame.setLocation(Integer.parseInt(Controller.model.iniData.get("mainWindowXPosition")),
                Integer.parseInt(Controller.model.iniData.get("mainWindowYPosition")));
        */
    }

    // чтение из файла данных локализации (язык интерфейса)

    private void localisation(){
        try{
            localisation = CommonTools.readFromIniFile(locFileName);
        }catch(FileNotFoundException ex){/* todo Отработать */}
        catch(IOException ex){/* todo Отработать */}
    }

    /*
    Блок геттеров и сеттеров
    */
    /* **********************************************************************************/
    /* Интерфейсные функции для того, чтобы Model мог получить необходимые ссылки из GUI */
    /* **********************************************************************************/
    // Получить статус готовности GUI
    //public boolean getReady(){ return this.isReady; }
    // Поле с именем текущего устройства
    // protected JTextField getThisDeviceTextField(){ return frame.getCurrentDeviceBox().getTextField(); }
    public AbstractUIControl getThisDeviceTextField(){ return frame.getCurrentDeviceBox().getTextField(); }
    // Кнопка сохранения имени данного устройства (рядом с полем данного устройства)
    // protected JButton getThisDeviceButton() { return frame.getCurrentDeviceBox().getButton(); }
    public AbstractUIControl getThisDeviceButton(){ return frame.getCurrentDeviceBox().getButton(); }
    // Получить текстовые поля других устройств
    public AbstractUIControl[] getOtherCircleDevicesTextField(){
        SwingUI.STextField[] result = new SwingUI.STextField[getMaxCompanyCount() - 1];
        for(int i = 0; i < result.length; i++){ result[i] = frame.getOtherDevecesBoxes()[i].getTextField(); }
        return result;
    }
    /*    protected JTextField[] getOtherCircleDevicesTextField(){
            JTextField[] result = new JTextField[Model.MAX_COMPANY_COUNT - 1];
            for(int i = 0; i < result.length; i++){ result[i] = frame.getOtherDevecesBoxes()[i].getTextField(); }
            return result;
        }*/
    // Получить кнопки рядом с текстовыми полями других устройств
    public AbstractUIControl[] getOtherCircleDevicesButton(){
        SwingUI.SButton[] result = new SwingUI.SButton[getMaxCompanyCount() - 1];
        for(int i = 0; i < result.length; i++){ result[i] = frame.getOtherDevecesBoxes()[i].getButton(); }
        return result;
    }
    /*    protected JButton[] getOtherCircleDevicesButton(){
            JButton[] result = new JButton[Model.MAX_COMPANY_COUNT - 1];
            for(int i = 0; i < result.length; i++){ result[i] = frame.getOtherDevecesBoxes()[i].getButton(); }
            return result;
        }*/
    //protected JButton getSynchronisationButton(){ return frame.getSynchronisationButton(); }
    public AbstractUIControl getNoteTextArea(){ return frame.noteArea; }
    //protected JTextArea getNoteTextArea(){ return frame.noteArea; }
    //public void setThisDeviceLable(String label){ frame.getCurrentDeviceBox().getTextField().setText(label); }
    //public String[] getCircleDevicesTextLabels(){}
    //public void
    public AbstractUIControl getTextPaired(AbstractUIControl auic){
        SwingUI.SButton button = (SwingUI.SButton) auic;
        MainWindow.inBoxPairTextFieldAndButton[] pairs = frame.getOtherDevecesBoxes();
        for(int i = 0; i < pairs.length; i++){
            if(pairs[i].getButton() == button){ return pairs[i].getTextField(); }
        }
        return null;
    }
    /*protected JTextField getTextPaired(JButton button){
        MainWindow.inBoxPairTextFieldAndButton[] pairs = frame.getOtherDevecesBoxes();
        for(int i = 0; i < pairs.length; i++){
            if(pairs[i].getButton() == button){ return pairs[i].getTextField(); }
        }
        return null;
    }*/
    public AbstractUIControl getInvitationTextField(){ return frame.getInvitationInputBox().getTextField(); }
    //protected JTextField getInvitationTextField(){ return frame.getInvitationInputBox().getTextField(); }

    /* возвращает первое свободное текстовое поле, в GUI-таблице устройств круга. Алгоритм основан на предположении, что занятые поля идут, начиная с первого, без пробелов. То есть, за первым обнаруженным свободным полем идут только свободные поля. Если функция возвращает null свободных полей нет */
    public synchronized AbstractUIControl getFreeOtherDeviceTextField() throws InterruptedException{
        /* todo данную функцию переделать в простой вариант: Core при её вызове сразу передаёт в тело массив занятых полей */
        /*
        CommunicationChannal ch = getBusyTextFieldsChannal();
        AbstractUIControl[] busyFields = null;
        while (ch.isCommandGo()) {
            // кто-то уже запросил занятые поля
            Thread.sleep(5); // ждём освобождения канала
        }
        if (!ch.isCommandGo()) {
            ch.setCommandGo(true);
        }
        ; // Даём команду на исполение запроса
        while (!ch.isCommandReady()) {
            Thread.sleep(5);
        } // ждём исполнения запроса

        if(ch.isCommandReady()){ busyFields = (AbstractUIControl[]) ch.getAnswer(); } // запрос исполнен, получаем ответ
        for(int i = 0; i < frame.arrayTextFieldsAndButtons.length; i++){
            for(int j = 0; j < busyFields.length; j++){
                if(frame.arrayTextFieldsAndButtons[i].getTextField().equals(busyFields[j])){
                    continue;
                }
                if(j == busyFields.length - 1) {
                    return frame.arrayTextFieldsAndButtons[i].getTextField();
                }
            }
        }
        */

        /*
        for(int i = 0; i < frame.arrayTextFieldsAndButtons.length; i++){
            if(CoreSide.isTextFieldFree(frame.arrayTextFieldsAndButtons[i].getTextField())){
                return frame.arrayTextFieldsAndButtons[i].getTextField();
            }
        }
        SwingUI.STextField result = new SwingUI.STextField();
        result.setSText("AllTextFieldsOccupied");
        return result; // todo Отработать ошибку "все поля заняты"
        */

        for(int i = 0; i < frame.arrayTextFieldsAndButtons.length; i++){
            if(core.isTextFieldFree(frame.arrayTextFieldsAndButtons[i].getTextField())){
                return frame.arrayTextFieldsAndButtons[i].getTextField();
            }
        }
        //SwingUI.STextField result = new SwingUI.STextField();
        //result.setSText("AllTextFieldsOccupied");
        //return result; // todo Отработать ошибку "все поля заняты"

        return null; // все поля заняты
    }
    /*protected JTextField getFreeOtherDeviceTextField(){
        for(int i = 0; i < frame.arrayTextFieldsAndButtons.length; i++){
            if(Controller.model.isTextFieldFree(frame.arrayTextFieldsAndButtons[i].getTextField())){
                return frame.arrayTextFieldsAndButtons[i].getTextField();
            }
        }
        return new JTextField(); // только шоб не ругался компиллятор
    }*/
    // возвращает ссылку на кнопку по парному текстовому полю
    public AbstractUIControl getButtonByTextField(AbstractUIControl auic){
        SwingUI.STextField stf = (SwingUI.STextField) auic;
        if(frame.getCurrentDeviceBox().getTextField() == stf){ return frame.getCurrentDeviceBox().getButton(); }
        if(frame.getInvitationInputBox().getTextField() == stf) { return frame.getInvitationInputBox().getButton(); }
        for(int i = 0; i < frame.arrayTextFieldsAndButtons.length; i++){
            if(stf == frame.arrayTextFieldsAndButtons[i].getTextField()){
                return frame.arrayTextFieldsAndButtons[i].getButton();
            }
        }
        return new SwingUI.SButton(); // только шоб не ругался компиллятор
    }
    /*protected JButton getButtonByTextField(JTextField jtf){
        if(frame.getCurrentDeviceBox().getTextField() == jtf){ return frame.getCurrentDeviceBox().getButton(); }
        if(frame.getInvitationInputBox().getTextField() == jtf) { return frame.getInvitationInputBox().getButton(); }
        for(int i = 0; i < frame.arrayTextFieldsAndButtons.length; i++){
            if(jtf == frame.arrayTextFieldsAndButtons[i].getTextField()){
                return frame.arrayTextFieldsAndButtons[i].getButton();
            }
        }
        return new JButton(); // только шоб не ругался компиллятор
    }*/
    // возвращает ссылку на текстовое поле, по парной кнопке
    /*
    protected JTextField getTextFieldByButton(JButton btn){
        if(frame.getCurrentDeviceBox().getButton() == btn){ return frame.getCurrentDeviceBox().getTextField(); }
        if(frame.getInvitationInputBox().getButton() == btn) { return frame.getInvitationInputBox().getTextField(); }
        for(int i = 0; i < frame.arrayTextFieldsAndButtons.length; i++){
            if(btn == frame.arrayTextFieldsAndButtons[i].getButton()){
                return frame.arrayTextFieldsAndButtons[i].getTextField();
            }
        }
        return new JTextField(); // только шоб не ругался компиллятор
    }
    */
    // инвертируем текст кнопок Kick/Invite
    public void invertTextOnButton(AbstractUIControl auic){
        SwingUI.SButton sbtn = (SwingUI.SButton) auic;
        if(sbtn.getSText() == localisation.get("btInviteToCircle")){ sbtn.setSText(localisation.get("btKickFromCircle")); }
        else{ sbtn.setSText(localisation.get("btInviteToCircle")); }
    }
    /*    protected void invertTextOnButton(JButton jbtn){
            if(jbtn.getText() == localisation.get("btInviteToCircle")){ jbtn.setText(localisation.get("btKickFromCircle")); }
            else{ jbtn.setText(localisation.get("btInviteToCircle")); }
        }*/
/*    Чистка свободного текстового поля в GUI-массиве устройств круга и, если на парной кнопке осталась старая
    надпись Kick, меняем её на Invite */
    public void clearFreeTextField() throws InterruptedException{
        /*
        // todo данную функцию переделать в простой вариант: Core при её вызове сразу передаёт в тело массив занятых полей
        CommunicationChannal ch = getBusyTextFieldsChannal();
        AbstractUIControl[] busyFields = null;
        while (ch.isCommandGo()) {
            // кто-то уже запросил занятые поля
            Thread.sleep(5); // ждём освобождения канала
        }
        if (!ch.isCommandGo()) {
            ch.setCommandGo(true);
        }
        ; // Даём команду на исполение запроса
        while (!ch.isCommandReady()) {
            Thread.sleep(5);
        } // ждём исполнения запроса

        if(ch.isCommandReady()){ busyFields = (AbstractUIControl[]) ch.getAnswer(); } // запрос исполнен, получаем ответ

        for(int i = 0; i < frame.arrayTextFieldsAndButtons.length; i++){
            for(int j = 0; j < busyFields.length; j++){
                if(frame.arrayTextFieldsAndButtons[i].getTextField().equals(busyFields[j])){
                    continue;
                }
                if(j == busyFields.length - 1){
                    frame.arrayTextFieldsAndButtons[i].getTextField().setSText("");
                    frame.arrayTextFieldsAndButtons[i].getButton().setSText(localisation.get("btInviteToCircle"));
                }
            }
        }
        */
        for(int i = 0; i < frame.arrayTextFieldsAndButtons.length; i++){
            if(core.isTextFieldFree(frame.arrayTextFieldsAndButtons[i].getTextField())){
                frame.arrayTextFieldsAndButtons[i].getTextField().setSText("");
                frame.arrayTextFieldsAndButtons[i].getButton().setSText(localisation.get("btInviteToCircle"));
            }
        }
    }

    /* Сделать доступным элементы окна программы */
    protected synchronized void setUIEnable(){
        if(frame.synchronisationButton.isEnabled()){ return; } // если уже другой нитью всё установлено. Не фига по второму разу
        frame.noteArea.setEnabled(true);
        frame.getInvitationInputBox().getButton().setEnabled(true);
        frame.getInvitationInputBox().getTextField().setEnabled(true);
        frame.currentDevice.getButton().setEnabled(true);
        frame.currentDevice.getTextField().setEnabled(true);
        for(int i = 0; i < frame.arrayTextFieldsAndButtons.length; i++){
            frame.arrayTextFieldsAndButtons[i].getButton().setEnabled(true);
            frame.arrayTextFieldsAndButtons[i].getTextField().setEnabled(true);
        }
        frame.synchronisationButton.setEnabled(true);
    }

    /* Сделать недоступными элементы окна программы */
    protected synchronized void setUIDisable(){
        if(!frame.synchronisationButton.isEnabled()){ return; } // если уже другой нитью всё установлено. Не фига по второму разу
        frame.noteArea.setEnabled(false);
        frame.getInvitationInputBox().getButton().setEnabled(false);
        frame.getInvitationInputBox().getTextField().setEnabled(false);
        frame.currentDevice.getButton().setEnabled(false);
        frame.currentDevice.getTextField().setEnabled(false);
        for(int i = 0; i < frame.arrayTextFieldsAndButtons.length; i++){
            frame.arrayTextFieldsAndButtons[i].getButton().setEnabled(false);
            frame.arrayTextFieldsAndButtons[i].getTextField().setEnabled(false);
        }
        frame.synchronisationButton.setEnabled(false);
    }

    /* Функция возвращает статус активности окна приложения. Для поределения используется состояние noteArea */
    protected boolean isFrameEnabled(){ return frame.synchronisationButton.isEnabled(); }

    // вставить новый статус в массив статусов
    public void putNewStatusInStatusString(StatusSender sender, String status)
    {
        synchronized (frame.statusStringsArray){ frame.statusStringsArray.put(sender, new StatusStringObject(status)); }
    }
    // вставить новый статус в массив статусов, с указанием количества показов.
    // После указанного количества показов, статус удаляется из массива показов.
    protected void putNewStatusInStatusString(StatusSender sender, String status, int showCount)
    {
        synchronized (frame.statusStringsArray){ frame.statusStringsArray.put(sender, new StatusStringObject(status, showCount)); }
    }
    // удалить статус из массива статусов
    protected void removeStatusFromStatusString(StatusSender sender){
        synchronized (frame.statusStringsArray){ frame.statusStringsArray.remove(sender); }
    }

    // Возвращает переведённую строку из массива локализации по её ключу
    public String getLocalisationValueByKey(String key){
        return this.localisation.get(key);
    }

    /*enum StatusSender{
        TEST_INTERNET_CONNECTION,
        NOTE_LENGTH_CONTROL,
        LABEL_LENGTH_CONTROL,
        CONTROLLER,
        ENTER_TO_CIRCLE
        //DB_ERRORS
    }*/
}