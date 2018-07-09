import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by CJDMandMC on 08/07/2018.
 */
public class downloadHtml {
    private static final String urlHost = "http://membersarea.rawstrokes.com";

    public static void main(String[] args) throws IOException {
        // Set password
        Authenticator.setDefault(new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("esejag", "Caprica6".toCharArray());
            }
        });

        //Procecamos
        processLevel1();
        processLevel2();
        processLevel3();
        filterLevel3();
    }

    private static int getStatus(String urlCompose) throws IOException {
        URL url = new URL(urlCompose);
        HttpURLConnection http = (HttpURLConnection) url.openConnection();
        int statusCode = http.getResponseCode();
        http.disconnect();
        return statusCode;
    }

    private static void saveFile(String fileName, List<String> lines, boolean raw) throws IOException {
        //grabamos nivel 1 en un file txt
        File file = new File(fileName);
        BufferedWriter out = new BufferedWriter(new FileWriter(file));
        int lineCounter = 1;
        for (String line : lines) {
            if(!raw) {
                if (line.contains("#")) {
                    lineCounter = 1;
                    out.write(line);
                } else {
                    out.write(lineCounter + ": " + line);
                    lineCounter++;
                }
            } else {
                out.write(line);
            }
            out.newLine();
        }
        out.close();
    }

    private static void processLevel1() throws IOException {
        //nivel 1
        List<String> nivel1 = new ArrayList<>();

        for (int i = 1; i < 12; i++) {
            String relativePath = "/movies---page-" + i + ".html";
            if (getStatus(urlHost + relativePath) != 200) {
                relativePath = "/movies----page-" + i + ".html";
                if (getStatus(urlHost + relativePath) != 200) {
                    System.out.println("Imposible conectar con el host: " + urlHost + relativePath);
                }
            }

            //Log
            System.out.println("Procesamos nivel1: " + urlHost + relativePath + "...");

            URL url = new URL(urlHost + relativePath);
            InputStream is = url.openStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            //ciclamos linea por linea cada html
            String line;
            String pattern1 = "nonblock nontext rgba-background grpelem";
            String pattern2 = "nonblock nontext clip_frame grpelem";
            String pattern3 = "nonblock nontext clip_frame clearfix grpelem";
            nivel1.add("# " + urlHost + relativePath);
            while ((line = br.readLine()) != null) {
                //Match with patterns
                if (line.contains(pattern1) || line.contains(pattern2) || line.contains(pattern3)) {
                    //Obtenemos la cadena de la direccion relativa
                    line = line.substring(line.indexOf("href=\"") + 6);
                    line = line.substring(0, line.indexOf("\">"));

                    //Si la linea no contiene estos patrones
                    if (!line.contains("http://") && !line.contains("photos") && !line.contains("page") && !line.contains("index")) {
                        nivel1.add(line);
                    }
                }
            }
            br.close();
        }

        //Guardamos la salida en un archivo
        saveFile("nivel1.txt", nivel1, false);
    }

    private static void processLevel2() throws IOException {
        File file = new File("nivel1.txt");
        if (!file.exists()) {
            return;
        }
        Scanner input = new Scanner(file);

        //Leemos los path ya procesados
        File fileNivel2 = new File("nivel2.txt");
        List<String> relativePathLoaded = new ArrayList<>();
        if(fileNivel2.exists()){
            BufferedReader br = new BufferedReader(new FileReader(fileNivel2));
            String line;
            while((line = br.readLine()) != null) {
                line = line.substring(0, line.indexOf("::"));
                relativePathLoaded.add(line);
            }
            br.close();
        }

        while (input.hasNextLine()) {
            String line = input.nextLine();
            if (!line.contains("#")) {
                String relativePath = line.substring(line.indexOf(":") + 2);

                //Si este path ya no fue procesado, lo procesamos
                if(!relativePathLoaded.contains(relativePath)) {
                    //Contruccion de la url completa
                    String fullUrl = urlHost + "/" + relativePath;

                    //Log
                    System.out.println("Procesamos Nivel2: " + fullUrl + "...");

                    if (getStatus(fullUrl) != 200) {
                        System.out.println("Nivel2: Imposible procesar la direccion " + fullUrl);
                    } else {
                        URL url = new URL(fullUrl);
                        InputStream is = url.openStream();
                        BufferedReader br = new BufferedReader(new InputStreamReader(is));

                        //ciclamos linea por linea el html
                        String lineHtmlLevel2;
                        String urlNivel2="";
                        String title="";
                        while ((lineHtmlLevel2 = br.readLine()) != null) {
                            if (lineHtmlLevel2.contains("<center>")) {
                                //Obtenemos la url del archivo javascript
                                urlNivel2 = lineHtmlLevel2.substring(lineHtmlLevel2.indexOf("src='") + 5);
                                urlNivel2 = urlNivel2.substring(0, urlNivel2.indexOf("'>"));
                            }
                            if (lineHtmlLevel2.contains("<title>")) {
                                title = lineHtmlLevel2.substring(lineHtmlLevel2.indexOf("<title>") + 7);
                                title = title.substring(0, title.indexOf("</title>"));
                                title = title.replace("&amp;", "&").replace("&nbsp;", "");
                            }
                        }
                        updateFile("nivel2.txt", relativePath + "::" + title + ".mp4" + ";" + urlNivel2);
                        br.close();
                    }
                }
            }
        }
    }

    private static void updateFile(String fileName, String line) throws IOException {
        BufferedWriter bw = null;
        FileWriter fw = null;
        try {
            File file = new File(fileName);
            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            // true = append file
            fw = new FileWriter(file.getAbsoluteFile(), true);
            bw = new BufferedWriter(fw);
            bw.write(line + "\n");

        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            try {
                if (bw != null)
                    bw.close();
                if (fw != null)
                    fw.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private static void processLevel3() throws IOException {
        File fileNivel2 =  new File("nivel2.txt");
        if(!fileNivel2.exists()){
            return;
        }
        Scanner input = new Scanner(fileNivel2);

        //Leemos los path ya procesados
        File fileNivel3 = new File("nivel3.txt");
        List<String> titlesLoaded = new ArrayList<>();
        if(fileNivel3.exists()){
            BufferedReader br = new BufferedReader(new FileReader(fileNivel3));
            String line;
            while((line = br.readLine()) != null) {
                line = line.substring(0, line.indexOf(";"));
                titlesLoaded.add(line);
            }
            br.close();
        }

        while (input.hasNextLine()) {
            String line = input.nextLine();
            String title = line.substring(line.indexOf("::") + 2);
            title = title.substring(0, title.indexOf(";"));
            String pathLevel3 = line.substring(line.indexOf(";") + 1);

            if(!titlesLoaded.contains(title)) {
                //Log
                System.out.println("Procesamos Nivel3: " + pathLevel3 + "...");

                if (getStatus(pathLevel3) != 200) {
                    System.out.println("Nivel3: Imposible procesar la direccion " + pathLevel3);
                } else {
                    URL url = new URL(pathLevel3);
                    InputStream is = url.openStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));

                    //ciclamos linea por linea el html
                    String lineJsLevel3;
                    while ((lineJsLevel3 = br.readLine()) != null) {
                        if (lineJsLevel3.contains("center")) {
                            //Obtenemos la url del archivo javascript
                            String urlNivel3 = lineJsLevel3.substring(lineJsLevel3.indexOf("http://rawstrokes-onwldptwmmj.stackpathdns.com"));
                            urlNivel3 = urlNivel3.substring(0, urlNivel3.indexOf(".mp4") + 4);
                            updateFile("nivel3.txt", title + ";" + urlNivel3);
                        }
                    }
                    br.close();
                }
            }
        }
    }

    public static void filterLevel3() throws IOException {
        //Procesamos el filtro
        File fileFilter = new File("filter.txt");
        if(!fileFilter.exists()){
            return;
        }
        Scanner scFilter = new Scanner(fileFilter);
        List<String> tagFilter = new ArrayList<>();
        while (scFilter.hasNextLine()) {
            String line = scFilter.nextLine();
            if(line.contains("../")) {
                line = line.substring(line.indexOf("../"));
                tagFilter.add(line);
            }
        }

        //Procesamos nivel
        File fileLevel3 = new File("nivel3.txt");
        if(!fileLevel3.exists()){
            return;
        }
        Scanner scLevel3 = new Scanner(fileLevel3);

        updateFile("RS.txt", "#DESCARGAS DESDE \"RAWSTROKES.COM\"");
        updateFile("RS.txt", "");

        while (scLevel3.hasNextLine()) {
            String line = scLevel3.nextLine();
            String tag = line.substring(line.indexOf("../"));
            //Si esta linea esta en filter es uno de los archivos deseados
            if(tagFilter.contains(tag)){
                updateFile("RS.txt", line);
            }
        }

    }
}
