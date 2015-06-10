import javax.swing.*;
import java.io.*;
import java.util.*;

/**
 * Created by GreMal on 21.02.2015.
 */
public class Model {
    protected Map<String, String> iniData = new HashMap<String, String>();
    final private static String iniFileName = "cloudnotes.ini";
    private boolean isReady = false;
    /* максимальное количество устройств в компании, включая и данное устройство.
    *  в связи с этим, в интерфейсе, в разделе "Компания" должно быть текстовых полей и кнопок,
    *  связанных с другими устройствами, на одну меньше, т. е. MAX_COMPANY_COUNT - 1*/
    protected final static int MAX_COMPANY_COUNT = 8;
    //protected DeviceInfo[] devicesInCircle = new DeviceInfo[MAX_COMPANY_COUNT];
    private Map<String, DeviceInfo> devicesInCircle = new HashMap<String, DeviceInfo>(); // deviceID, device
    //protected Internet.DBMessages devicesRequestDBStatus = Internet.DBMessages.VOID;
    private NoteInfo noteInfo = new NoteInfo();
    protected static Calendar calendar = new GregorianCalendar();
    private Date nextSynchronisationTime = new Date();
    //private static Map<GUI.StatusSender, GUI.StatusStringObject> lazyStatusForGui = new HashMap<GUI.StatusSender, GUI.StatusStringObject>();

    static{


    }

    /* Функция читает сохранённые настройки программы из соотеветствующего ini-файла в список Мэп*/
    protected void readInit() throws IOException
    {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(iniFileName)));
        } catch (FileNotFoundException Exception){
            File myFile = new File(iniFileName);
            myFile.createNewFile();
            // Новый файл сохраняется на уровень выше в структуру каталогов. Почему?
        }
        iniData = Tools.readFromIniFile(iniFileName);
    }

    /*
* Сохранение всех параметвров настройки (и GUI в том числе) в файле ini
* */
    protected void writeInit()
    {
        //FileWriter writer;
        try {
            FileWriter writer = new FileWriter(iniFileName);

            Iterator<Map.Entry<String, String>> it = iniData.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, String> pair = it.next();
                String Key = pair.getKey();
                String Value = pair.getValue();
                StringBuilder builder = new StringBuilder();
                builder.append(Key).append((char) 32).append((char) 61).append((char) 32).append(Value).append("\r\n");
                // Ключ, пробел, равно, пробел, значение, перевод на новую строку
                writer.write(builder.toString());
            }
            writer.close();
        }catch(IOException ignore) {/*NOP*/}
    }

    /* Инициализация начальных не GUI-данных (возможно, после установки программы, то есть, при первом запуске)
*  изменение элементов GUI на основании полученных данных*/
    protected void initialization(){
        if(!isInternerConnectionActive()){ return; }
        //Internet.Result internetAnswer;
        // если хотя в файле настроек нет хотя бы одного из нижеследующих параметров, то содаём абсолютно новую запись
        if(!(iniData.containsKey("userID")&&iniData.containsKey("deviceID")/*&&iniData.containsKey("noteID")*/)){
            createNewNote();
        }
        getInitialisationDataFromDB();

        isReady = true;
    }

    /* Создаём новую заметку */
    private void createNewNote(){
        //if(!isInternerConnectionActive()){ return; }
        /* Внимание! Проверка связи сознательно здесь не производится! Так как производилась в вызывающей функции! */
        iniData.put("userID", Tools.getRandomID());
        iniData.put("deviceID", Tools.getRandomID());
        //iniData.put("noteID", Tools.getRandomID());
        // регистрируем новую запись в облаке
        // Internet.createNewNote(iniData.get("userID"), iniData.get("deviceID"));
        devicesInCircle.clear();

        Internet.Result internetAnswer= Internet.createNewNote(iniData.get("userID"), iniData.get("deviceID"));
        if(internetAnswer.dbStatus != Internet.DBMessage.SUCCESS){
            connectionErrorHandler(internetAnswer.dbStatus, "Ошибка создания новой записи.");
            return;
        }
    }


    /* Получить данные по СУЩЕСТВУЮЩЕЙ заметке из Базы Данных и настроить Модель и GUI
    * Т. е. либо заметка была создана непосредственно перед вызовом этой функции, либо была проведена проверка
    * её существования и доступности */
    private void getInitialisationDataFromDB(){
        /* Внимание! Проверка связи сознательно здесь не производится! Так как производилась в вызывающей функции! */
        //DeviceInfo[] tempArray = Internet.getAllDevicesInfo(iniData.get("userID"), iniData.get("deviceID"));
        Internet.Result internetAnswer = Internet.getAllDevicesInfo(iniData.get("userID"), iniData.get("deviceID"));
        /* неудачная попытка доступа к заметке */
        if(internetAnswer.dbStatus != Internet.DBMessage.SUCCESS){
            connectionErrorHandler(internetAnswer.dbStatus, "Получение из облака информации об устройствах в круге.");
            return;
        }else{
            // devicesRequestDBStatus = Internet.DBMessages.VOID; // возвращаем значение по умолчанию
             /*Ошибок нет, данные получены. */
            {
                // заполенение объекта noteInfo
                noteInfo.noteId = iniData.get("userID");
                //Map<Date, String> map = Internet.getNote(iniData.get("userID"), iniData.get("deviceID"));
                Internet.Result noteResult = Internet.getNote(iniData.get("userID"), iniData.get("deviceID"));
                if(noteResult.dbStatus != Internet.DBMessage.SUCCESS){
                    connectionErrorHandler(noteResult.dbStatus, "Получение из облака информации о заметке.");
                    return;
                }else{
                    for (Map.Entry<Date, String> temp : ((Map<Date, String>) noteResult.content).entrySet()) {
                        noteInfo.noteTimeStamp = temp.getKey();
                        noteInfo.note = temp.getValue();
                    }
                    noteInfo.textArea = Controller.gui.getNoteTextArea();
                    noteInfo.textArea.setText(noteInfo.note);
                    // раздача кнопок и текстовых полей устройствам
                    JButton[] buttons = Controller.gui.getOtherCircleDevicesButton();
                    JTextField[] textFields = Controller.gui.getOtherCircleDevicesTextField();
                    int i = 0;
                    //System.out.println("Начинаем раздачу полей и кнопок.");
                    for (DeviceInfo info : (DeviceInfo[]) internetAnswer.content) {
                        if (info.deviceId.equals(iniData.get("deviceID"))) {
                            // текущему устройству
                            info.textField = Controller.gui.getThisDeviceTextField();
                            info.button = Controller.gui.getThisDeviceButton();
                            info.textField.setText(info.deviceLabel);
                            //gui.setThisDeviceLable(info.deviceLabel);
                            //System.out.println("Это устройство получило.");
                        } else {
                            // другим устройствам
                            info.textField = textFields[i];
                            info.button = buttons[i];
                            info.textField.setText(info.deviceLabel);
                            info.button.setText(Controller.gui.localisation.get("btKickFromCircle"));
                            //System.out.println(String.format("Устройство №%d получило.", i));
                            i++;
                        }
                        devicesInCircle.put(info.deviceId, info);
                    }
                }
            }
        }
    }

    protected void connectGUIelementsWithModelObjects(){
        // Добавить ссылки на элементы GUI в model

        // Добавить ссылки на объекты модели обработчикам событий в GUI

    }

    protected void setNextSynchronisationTime(long ms){
        long currentDate = (new Date()).getTime();
        nextSynchronisationTime = new Date(currentDate + ms);
    }

    /* Фукция синхронизации заметки, меток устройств и всего прочего, что нужно синхронизировать. */
    protected synchronized void startSynchronization(){
        /*-------------------*/
/*        {
            String text = noteInfo.textArea.getText();
            text = text.replaceAll("\\\\", "<92>");
            String temp = text;
        }*/
        /*-------------------*/
        if(!isInternerConnectionActive()){ return; }

        Controller.gui.setFrameDisable();
        boolean isSynchronisation = true;

        // Map<String, Date> mapTimeStamps = Internet.getTimeStamps(iniData.get("userID"), iniData.get("deviceID"));
        Internet.Result answer = Internet.getTimeStamps(iniData.get("userID"), iniData.get("deviceID"), isSynchronisation);
        /* неудачная попытка доступа к заметке */
        if((answer.dbStatus == Internet.DBMessage.ACCESS_DENIED)||(answer.dbStatus == Internet.DBMessage.CIRCLE_NOT_FOUND)){
            // Доступ в круг запрещён. Скорее всего устройство кикнули. А значит, создаём новую заметку.
            // Или же не найдена эта заметка. Либо удалена из базы, либо сменён её userID
            // запоминаем метку устройства
            String thisDeviceLabel = devicesInCircle.get(iniData.get("deviceID")).deviceLabel;
            createNewNote();
            getInitialisationDataFromDB();
            DeviceInfo device = devicesInCircle.get(iniData.get("deviceID"));
            device.deviceLabel = thisDeviceLabel; // восстанавливаем метку устройства
            device.textField.setText(thisDeviceLabel); // Выводим старую метку устройства в текстовое поле.
            device.labelWasChanged = true; // чтобы метка синхронизировалась на сервер
            device.labelTimeStamp = new Date(); // чтобы метка синхронизировалась на сервер
            Controller.gui.clearFreeTextField();
            answer = Internet.getTimeStamps(iniData.get("userID"), iniData.get("deviceID"), isSynchronisation);
            // return;
        }else if(answer.dbStatus != Internet.DBMessage.SUCCESS){
            connectionErrorHandler(answer.dbStatus, "Получение из облака информации о TimeStamps меток устройств.");
            return;
        }
        //if(mapTimeStamps == null){ /* Отработать проблемы со связью */ }

        Map<String, Date> mapTimeStamps = (Map<String, Date>) answer.content;

        // Синхронизация заметки
        if(noteInfo.noteWasChanged){
            noteInfo.note = Controller.gui.getNoteTextArea().getText();
            noteInfo.noteTimeStamp = new Date();
            noteInfo.noteWasChanged = false;
        }
        if(noteInfo.noteTimeStamp.compareTo(mapTimeStamps.get(noteInfo.noteId)) > 0){
            /* TimeStamp у заметки на стороне клиента больше, чем на сервере.
            Значит, обновляем данные на сервере */
            answer = Internet.updateNote(iniData.get("userID"), iniData.get("deviceID"), noteInfo.note, noteInfo.noteTimeStamp);
            if(answer.dbStatus != Internet.DBMessage.SUCCESS){
                connectionErrorHandler(answer.dbStatus, "Обновление записи с клиента на сервер.");
                return;
            }
        }else if (noteInfo.noteTimeStamp.compareTo(mapTimeStamps.get(noteInfo.noteId)) < 0){
            /* TimeStamp у заметки на стороне клиента меньше, чем на сервере.
            Значит, обновляем данные на клиенте */
            //noteInfo.note = Internet.getNote(iniData.get("userID"), iniData.get("deviceID")).get(mapTimeStamps.get(iniData.get("userID")));
            answer = Internet.getNote(iniData.get("userID"), iniData.get("deviceID"));
            if(answer.dbStatus != Internet.DBMessage.SUCCESS){
                connectionErrorHandler(answer.dbStatus, "Получение заметки с сервера.");
                return;
            }
            noteInfo.noteTimeStamp = mapTimeStamps.get(iniData.get("userID"));
            noteInfo.note = ((Map<Date, String>) answer.content).get(noteInfo.noteTimeStamp);
            noteInfo.textArea.setText(noteInfo.note);
        }else{ /* = 0 -> ничего не делаем */ }

        //tempMap = Internet.getAllDevicesInfoMap(iniData.get("userID"), iniData.get("deviceID"));
        // получаем из базы информацию обо всех устройствах круга
        answer = Internet.getAllDevicesInfoMap(iniData.get("userID"), iniData.get("deviceID"));
        // if(tempMap.isEmpty()){ /* Отработать. если проблемы со связью */ }
        if(answer.dbStatus != Internet.DBMessage.SUCCESS){
            connectionErrorHandler(answer.dbStatus, "Получение информации обо всех устройствах круга.");
            return;
        }
        // Синхронизация членов круга
        {
            Map<String, DeviceInfo> tempMap = new HashMap<String, DeviceInfo>();
            for (Map.Entry<String, DeviceInfo> pair : devicesInCircle.entrySet()) {
                if (!mapTimeStamps.containsKey(pair.getKey())) {
                    // удалить из клиенетского списка данное устройство, так как его нет на сервере
                    // новые устройства попадают в devicesInCircle только через сервер
                    pair.getValue().textField.setText(""); // очищаем текстовое поле удалённого устройства
                    Controller.gui.invertTextOnButton(pair.getValue().button); // инвертируем надпись на кнопке
                    continue;
                }
                tempMap.put(pair.getKey(), pair.getValue());
            }
            //tempMap.remove(iniData.get("userID")); //удаляем, попавший в
            devicesInCircle.clear();
            devicesInCircle.putAll(tempMap);

            for (Map.Entry<String, Date> pair : mapTimeStamps.entrySet()) {
                // проверяме, чтобы в список устройств не попала заметка (так как она заведомо содержится в mapTimeStamps с сервера)
                if (!devicesInCircle.containsKey(pair.getKey())&&!iniData.get("userID").equals(pair.getKey())) {
                    // добавить в клиентский список данное устройство, так как оно есть на сервере, а у клиента нет
                    // устройства удаляются из deviceInCircle только через сервер
                    devicesInCircle.put(pair.getKey(), ((Map<String, DeviceInfo>) answer.content).get(pair.getKey()));
                }
            }
        }

        // Синхронизация меток членов клуба
        Map<String, String> newLabels = new HashMap<String, String>(); // новые метки для отправки на сервер
        for(Map.Entry<String, Date> pair : mapTimeStamps.entrySet()){
            // исключаем из перебора в mapTimeStamps запись с информацией о заметке (она же не устройство)
            if(!pair.getKey().equals(iniData.get("userID"))) {
                DeviceInfo device = devicesInCircle.get(pair.getKey());
                if (device.labelWasChanged) {
                    device.deviceLabel = device.textField.getText();
                    device.labelTimeStamp = new Date();
                    device.labelWasChanged = false;
                }
                if (device.labelTimeStamp.compareTo(pair.getValue()) > 0) {
                    // TimeStamp больше у клиента, начинаем комплектовать мапу для отправки на сервер
                    newLabels.put(pair.getKey(), device.deviceLabel);
                } else if (device.labelTimeStamp.compareTo(pair.getValue()) < 0) {
                    // TimeStamp меньше у клиента, обновляем данные у клиента
                    device.labelTimeStamp = ((Map<String, DeviceInfo>) answer.content).get(pair.getKey()).labelTimeStamp;
                    device.deviceLabel = ((Map<String, DeviceInfo>) answer.content).get(pair.getKey()).deviceLabel;
                    device.textField.setText(device.deviceLabel);
                } else { /* = 0 -> либо метка не менялась нигде, либо только что скачали данные по новому устройству круга */
                    if(device.textField == null){
                        /* если текстовое поле null, значит это точно - новое устройство */
                        device.textField = Controller.gui.getFreeOtherDeviceTextField(); // Получаем в GUI свободное текстовое поле
                        device.textField.setText(device.deviceLabel); // обновляем текст в текстовом поле
                        device.button = Controller.gui.getButtonByTextField(device.textField); // получаем парную кнопку
                        Controller.gui.invertTextOnButton(device.button); // инвертируем надпись на кнопке
                        //Controller.gui.clearFreeTextField(); // и очищаем (на всякий случай) свободные текстовые поля устройств круга
                    }
                }
            }
        }

        // Controller.gu.clearFreeTextField();

        if(!newLabels.isEmpty()){
            // Обновляем данные на сервере
            answer = Internet.updateDevecesLabels(iniData.get("userID"), iniData.get("deviceID"), newLabels);
            if(answer.dbStatus != Internet.DBMessage.SUCCESS){
                connectionErrorHandler(answer.dbStatus, "Обновление меток устройств.");
                return;
            }
            // Map<String, Date> idAndTimeStamps = (Map<String, Date>) answer.content;
            /* Раздача новых LabelTimeStamp, полученных из БД. Таким образом, синхронизируются LabelTimeStamps */
            for(Map.Entry<String, String> pair : newLabels.entrySet()){
                devicesInCircle.get(pair.getKey()).labelTimeStamp = (Date) answer.content;
            }
        }

        Controller.gui.setFrameEnable();
    }

    protected synchronized void InviteOrKickButtonPressed(JButton button){
        if(!isReady){ return; } // модель ещё не готова
        if(!isInternerConnectionActive()){ return; }
        Internet.Result answer;
        // временный мэп, в котом легче искать по кнопкам среди устройств круга. Ключ - кнопка
        Map<JButton, DeviceInfo> buttonKey = new HashMap<JButton, DeviceInfo>();
        // временный мэп, в котом легче искать по текстовым полям среди устройств круга. Ключ - текстовое поле
        Map<JTextField, DeviceInfo> textfieldKey = new HashMap<JTextField,DeviceInfo>();
        for(Map.Entry<String, DeviceInfo> pair : devicesInCircle.entrySet()){
            DeviceInfo value = pair.getValue();
            buttonKey.put(value.button, value);
            textfieldKey.put(value.textField, value);
        }
        // Принадлежит ли данная кнопка, какому либо устройству из deviceCircle
        // Если принадлежит, значит требуемое действие - Kick
        // Если не принадлежит, значит требуемое действие - Invite
        if(buttonKey.containsKey(button)){
            // Кикаем устройство и выходим из процедуры
            // Удалить устройство из базы на сервере
            answer = Internet.kickDeviceFromCircle(iniData.get("userID"), iniData.get("deviceID"), buttonKey.get(button).deviceId);
            if(answer.dbStatus != Internet.DBMessage.SUCCESS){
                connectionErrorHandler(answer.dbStatus, "Удаление устройства из Базы данных.");
                return;
            }
            //textfieldKey.remove(buttonKey.get(button).textField); // удаление из вспомогательной карты
            //DeviceInfo device = buttonKey.get(button);
            buttonKey.get(button).textField.setText(""); // очистка поля с меткой устройства
            Controller.gui.invertTextOnButton(button); // инвертирование надписи на кнопке
            devicesInCircle.remove(buttonKey.get(button).deviceId); // удалить устройство из списка на клиенте
            //buttonKey.remove(button); // удалить из вспомогательной карты

            return;
        }

        // Данная кнопка не принадлежит устройства, значит - приглашение.
        // Очищаем все поля, которые не принадлежат какому либо устройству (удаляем дублирующий пароль)
        JTextField[] textFieldsInGui = Controller.gui.getOtherCircleDevicesTextField();
        for(int i = 0; i < textFieldsInGui.length; i++){
            if(!textfieldKey.containsKey(textFieldsInGui[i])){ textFieldsInGui[i].setText(""); }
        }
        // Генерируем пароль.
        String newPass = Tools.generate5DigitPass();
        // Закинуть пароль на сервер (предыдущий неотработанный пароль удалится автоматически)
        answer = Internet.setPassForNewDevice(iniData.get("userID"), iniData.get("deviceID"), newPass);
        if(answer.dbStatus != Internet.DBMessage.SUCCESS){
            connectionErrorHandler(answer.dbStatus, "Отправка нового пароля в БД.");
            return;
        }
        // Вывести в строку GUI пароль
        JTextField tf = Controller.gui.getTextPaired(button);
        tf.setText(newPass);
    }

    /* Это происходит, если нажата кнопка "войти в круг" */
    protected synchronized void EnterToCircleButtonPressed(){
        if(!isReady){ return; } // модель ещё не готова
        if(!isInternerConnectionActive()){ return; }
        String password = Controller.gui.getInvitationTextField().getText();
        // если длина пароля ноль или больше допустимого
        if((password.length() == 0)||(password.length() > Controller.CHARS_IN_INVITATION_PASS)){ return; }
        // если в поле пароля введены не числа
        try{
            int test = Integer.parseInt(password);
            if(test == 0){ return; }
        }catch (NumberFormatException ex){ return; }
        // Отправляем запрос на сервер
        Internet.Result answer = Internet.addDeviceInCircle(iniData.get("userID"), iniData.get("deviceID"), password);
        if(answer.dbStatus != Internet.DBMessage.SUCCESS){
            connectionErrorHandler(answer.dbStatus, "Получение userID круга, куда вступаем.");
            return;
        }
        // получив userID, меняем его на этом клиенте и загружаем заметку.
        iniData.put("userID", (String) answer.content);
        getInitialisationDataFromDB();
    }

    /* Функция обрабатывае стаусные ситуации во время связи с сервером. Т. е., пишется в логи,
    пишется в статусную строку GUI и т. п.
    message - статус
    textKey - текстовая строка характеризующая место и момент возникновения данного статуса */
    protected void connectionErrorHandler(Internet.DBMessage message, String textKey){
        switch (message) {
            case ACCESS_DENIED:
                /* Доступ к кругу запрещён. Данное устройство не в круге */
                break;
            case CIRCLE_NOT_FOUND:
                /* Круг не найден */
                break;
            case VOID:
                /* Неопределённое значение. */
                break;
            case FIELDS_COUNT_ERROR:
                /* Ошибки. Число строк в ответе из интернета не преобразуется в осмысленный результат. */
                break;
            case SERVER_CONNECTION_ERROR:
                /* Ошибки. Связь с сервером не сложилась. */
                break;
            case SUCCESS:
                /* Успех */
                break;
        }
    }

    /* ****************************************************************************/
    /* Интерфейсные функции, чтобы GUI мог получить необходимые данные из модели. */
    /* ****************************************************************************/
    /* провека данного текстового поля на занятость */
    protected boolean isTextFieldFree(JTextField jtf){
        if(!isReady){ return false; } // модель ещё не готова
        for(Map.Entry<String, DeviceInfo> pair : devicesInCircle.entrySet()){
            if(pair.getValue().textField == null){ continue; }
            if(pair.getValue().textField == jtf){ return false; }
        }
        return true;
    }
    //protected NoteInfo getNoteInfo(){return noteInfo;}
    //protected Map<String, DeviceInfo> getDevicesInCircle(){return devicesInCircle;}
    protected synchronized void setNoteWasChangedFlagToTrue(){
        if(!isReady){ return; } // модель ещё не готова
        noteInfo.noteWasChanged = true;
        //System.out.println(noteInfo);
        //System.out.println(noteInfo.textArea);
        //noteInfo.note = noteInfo.textArea.getText();
    }
    //protected void setThisDeviceLabelWasChangedFlagToTrue(){ devicesInCircle.get(iniData.get("deviceID")).labelWasChanged = true; }
    protected synchronized void setDeviceLabelWasChangedFlagToTrue(JTextField textField){
        if(!isReady){ return; } // модель ещё не готова
        for (Map.Entry<String, DeviceInfo> info : devicesInCircle.entrySet()) {
            if (info.getValue().textField == textField) {
                info.getValue().labelWasChanged = true;
                //info.getValue().deviceLabel = info.getValue().textField.getText();
                return;
            }
        }
    }


    // вспомогательный класс. Содержит информацию об устройстве входящим в круг.
    protected static class DeviceInfo{
        protected String deviceId;
        protected String deviceLabel;
        protected Date labelTimeStamp;
        protected boolean labelWasChanged = false;
        /* Метка WasChanged ставится в true, когда происходит редактирование соответствующего поля,
        * ибо часто подрачивать лучше boolean, чем так же часто создавать новый объект Date для занесения в TimeStamp
        * TimeStamp будет изменён непосредственно перед синхронизацией, согласно состояния флага WasChahged
        * Необходимость синхронизации данного поля будет решаться исключительно из сравнения TimeStamp */

        protected JTextField textField;
        protected JButton button;

        protected DeviceInfo(String deviceId, String deviceLabel, Date labelTimeStamp){
            this.deviceId = deviceId;
            this.deviceLabel = deviceLabel;
            this.labelTimeStamp = labelTimeStamp;


            //this.textField = gui.getMainFrame().

        }

        protected DeviceInfo(){}

        //protected void setTextLabel(){}
        //protected String getTextLabel(){return null;}
        //protected void setButtonLabel(){};
        //protected String getButtonLabel(){return null;}
    }

    protected static class NoteInfo{
        protected String noteId;
        protected String note;
        //protected String deviceLabel;
        protected Date noteTimeStamp;
        protected boolean noteWasChanged = false;
        /* Метка WasChanged ставится в true, когда происходит редактирование соответствующего поля,
        * ибо часто подрачивать лучше boolean, чем так же часто создавать новый объект Date для занесения в TimeStamp
        * TimeStamp будет изменён непосредственно перед синхронизацией, согласно состояния флага WasChahged
        * Необходимость синхронизации данного поля будет решаться исключительно из сравнения TimeStamp */

        protected JTextArea textArea;
        //protected JButton button;
    }

    /* проверка интернет соединения. */
    private boolean isInternerConnectionActive(){
        InternetConnectionTest.InternetConnectionMessage message = InternetConnectionTest.isCloudReachable();
        Controller.gui.setInternetConnectionStatuses(message);
        if(message == InternetConnectionTest.InternetConnectionMessage.YES){ return true; }
        return false;
    }

    /* Функция определяет, есть ли несинхронизированные данные */
    protected boolean isWasChangedTrue(){
        if(!isReady){ return false; } // модель ещё не готова
        if(noteInfo.noteWasChanged){ return true; }
        for(Map.Entry<String, DeviceInfo> pair : devicesInCircle.entrySet()){
            if(pair.getValue().labelWasChanged){ return true; }
        }
        return false;
    }

    // равна ли заметка в модели содержимому текстовой области
    // функция может показать, вносились ли изменения в содержимое заметки
    protected boolean isNoteEquals(String str){
        if(!isReady){ return false; } // модель ещё не готова
        return this.noteInfo.note.equals(str);
    }

    // равна ли метка устройства в модели содержимому соответствующего текстового поля
    // Функция может показать, производились ли изменения с данным полем
    protected boolean isDeviceLabelEquals(JTextField jtf){
        if(!isReady){ return false; } // модель ещё не готова
        for(Map.Entry<String, DeviceInfo> pair : devicesInCircle.entrySet()){
            if(pair.getValue().textField == jtf) {
                if (pair.getValue().deviceLabel.equals(jtf.getText())) {
                    return true;
                }else{ break; }
            }
        }
        return false;
    }
    /* возвращает временный массив статусов (когда GUI уже сформирован */
/*    protected static Map<GUI.StatusSender, GUI.StatusStringObject> getLasyStatusForGui(){
        Map<GUI.StatusSender, GUI.StatusStringObject> tempMap = new HashMap<GUI.StatusSender, GUI.StatusStringObject>(lazyStatusForGui);
        lazyStatusForGui.clear(); // очищаем карту, так как она нам больше не понадобится
        return tempMap;
    }*/
    /* Класс-идентификатор. Введён, чтобы отличать и не путаться вместо типа String */
/*
    protected class ID{
        private String id;

        protected String get(){ return id; }
        protected void set(String str){ id = str; }
        protected ID(String str){ id = str; }
    }
    */
    //protected class
}