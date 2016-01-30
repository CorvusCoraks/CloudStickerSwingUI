import java.io.File;
import java.io.IOException;
// import java.util.Arrays;

/**
 * Created by GreMal on 30.05.2015.
 */
/*
* Программа запуска CoudNotes. Если обнаруживает в директории программы новую версию, то удаляет старую
* и переименовывает обновлённый файл в CloudSticker.jar
*
* */
public class Start {
    public static void main(String[] args) throws IOException{
        File newFile = new File("./lastversion.jar");
        File oldFile = new File("./CloudSticker.jar");

        if(newFile.exists()) {
            if (oldFile.exists()) {
                if (oldFile.delete()) {
                    newFile.renameTo(oldFile);
                }
            }
        }

        Process proc = Runtime.getRuntime().exec("java -jar CloudSticker.jar start");
    }
}