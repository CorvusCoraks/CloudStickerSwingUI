import java.io.File;
import java.io.IOException;

/**
 * Created by GreMal on 30.05.2015.
 */
/*
* Программа запуска CoudNotes. Если обнаруживает в директории программы новую версию, то удаляет старую
* и переименовывает обновлённый файл в CloudNotes.jar
*
* */
public class start {
    public static void main(String[] args) {
        File newFile = new File("./lastversion.jar");
        File oldFile = new File("./CloudNotes.jar");
        if(newFile.exists()){
            if(oldFile.exists()){
                if(oldFile.delete()){ newFile.renameTo(oldFile); }
            }
        }
        try{ Process proc = Runtime.getRuntime().exec("java -jar CloudNotes.jar start"); }
        catch(IOException ignore){/*NOP*/}
    }
}
