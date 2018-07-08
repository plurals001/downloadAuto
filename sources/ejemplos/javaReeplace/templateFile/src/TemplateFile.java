import java.io.*;

/**
 * Created by CJDMandMC on 05/07/2018.
 */
public class TemplateFile {
    public static void main (String[] args) throws IOException {

        final String fileName = "C:\\Users\\CJDMandMC\\Desktop\\template.txt";
        File file = new File("C:\\Users\\CJDMandMC\\Desktop\\cassinelli list 2.txt");
        FileReader fileReader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        BufferedWriter fileWriter = new BufferedWriter(new FileWriter(fileName));
        String line, str="";
        while ((line = bufferedReader.readLine()) != null) {

            if(!line.toLowerCase().contains("https://".toLowerCase()) && line.compareTo("") != 0){
                line = line.replaceAll(":", "");
                str += "%WGET% -O \"%FOLDER%/" + line + ".mp4\" ";
            } else if(line.toLowerCase().contains("https://".toLowerCase())){
                str += line;
            }

            if(line.compareTo("")==0){
                fileWriter.write(str);
                fileWriter.newLine();
                str="";
            }

        }
        fileWriter.close();
        fileReader.close();

    }
}
