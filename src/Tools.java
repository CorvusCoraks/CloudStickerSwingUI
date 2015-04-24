/**
 * Created by GreMal on 28.02.2015.
 */
import java.io.*;
import java.lang.Math;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Tools {
    protected final static String SQL_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    /*
    * Функция возвращает 16-значный случайный идентификатор
    * */
    public static String getRandomID()
    {
        char[] charArray = new char[] {'0','1','2','3','4','5','6','7','8','9',
                'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z',
                'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z'};

        char[] result = new char[16];

        for (int i = 0; i < result.length; i++)
        {
            result[i] = charArray[random100(0, charArray.length - 1)];
        }

        StringBuilder builder = new StringBuilder();
        return builder.append(result).toString();
    }

    /* Генерирует случайный пароль из пяти цифр. Для входа в круг. */
    protected static String generate5DigitPass(){
        StringBuilder bld = new StringBuilder();
        for(int i = 0; i < Controller.CHARS_IN_INVITATION_PASS; i++){
            int temp = (int) (Math.random()*10);
            bld.append(temp);
        }
        return bld.toString();
    }

    /*
    *  Случайное целое число в заданном диапазоне (в данном случае, максимум - 100)
    * */
    private static int random100(int min, int max)
    {
        // делим 10000 на количество символов из которых может состоять идентификатор
        // получаем размер одного блока
        double block = (10000/(max - min));
        // превращаем случайное число 0 - 1, в случайное число 0 - 10000
        // это случайное число должно попасть в один из блоков
        double temp = Math.random()*10000;
        // получаем номер блока, куда попало случайное число (фактически, индекс в массиве charArray)
        int result = (int) (temp/block) + 1;
        /* маловероятна, но возможна ситуация, когда случаное число окажется близко к max, и таким образом
        *  полученный номер блока будет больше max. В этом случае, в ручную, устанавливаем номер блока, как последний
        *  то есть, равный max*/
        if (result > max) {result--;}
        return result;
    }

    /* функция чтения в Map данных из ini-файла */
    protected static Map<String, String> readFromIniFile(String fileName) throws FileNotFoundException, IOException{
        Map<String, String> map = new HashMap<String, String>();

        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        String str = "";
        while((str = reader.readLine()) != null){
            String[] temp = str.split("=");
            if(temp.length == 0){ continue; } // строку не удалось разбить на две части.
            map.put(temp[0].trim(), temp[1].trim());
        }
        reader.close();

        return map;
    }

    /* Функции работы со временем.
     * Так как время Date() выдаётся в миллисекундах от чего-то там 1970 года, то, логично говоря, это UTC (GMT).
     * Когда я вывожу время на печать, оно автоматически форматируется согласно временного пояса по умолчанию на
     * локальной машине. То есть, фактически, я отправляю в БД TimeStamps локального времени, что не есть хорошо.
     * Особенно, при смене часовых поясов, переходов на летнее/зимнее время и т. п.
     *
     * То есть, строка уходящая в БД должна содержать строковое представление времени по GMT. А в обратном направлении
     * время должно быть принято как GMT*/

    // конвертация строчного представления даты (формат SQL базы GMT) в Date
    protected static Date String2Date(String str){
        SimpleDateFormat sdp = new SimpleDateFormat(SQL_DATE_FORMAT);
        sdp.setTimeZone(TimeZone.getTimeZone("GMT")); // входящая строка - GMT
        Date result = null;
        try{result = sdp.parse(str);}catch(ParseException ex){/* Отработать исключение */}
        return result;
    }

    // конвертация даты Date в строчное представление согласно формата БД SQL GMT
    protected static String Date2String(Date date){
        SimpleDateFormat sdp = new SimpleDateFormat(SQL_DATE_FORMAT);
        sdp.setTimeZone(TimeZone.getTimeZone("GMT")); // результирующая строка - GMT
        //StringBuffer sb = new StringBuffer();
        String str = sdp.format(date);
        return str;
    }
 }
