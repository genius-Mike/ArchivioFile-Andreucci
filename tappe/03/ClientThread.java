package scuola;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ClientThread extends Thread {
    Socket s;
    BufferedReader in;
    PrintWriter out;
    File cartella = null;
    File file = null;
    boolean spazio = true;
    String[][] TIPI = {
    {"txt", "text/plain; charset=utf-8"},
    {"md",  "text/markdown; charset=utf-8"},
    {"csv", "text/csv; charset=utf-8"},
    };

    public ClientThread(Socket s) {
        this.s = s;
    }

    public void run() {
        try {
            out = new PrintWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8), true);
            in = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
            //spiegazione ARRAYLIST: per evitare di andare a ritoccare i metodi per adeguarli alla nuova struttura di memoria
            //ho preferito salvare tutti le estensioni disponibili in un array:
            //in fondo se prima devo fare i controlli sulle estensioni è inutile vado a interrogare la matrice
            List<String> estensioni = new ArrayList<>();

            //LETTURA INPUT
            String richiesta = in.readLine();
            if (richiesta == null) {
                return;
            }
            //CONTROLLO BASE INPUT: VUOTO
            if (richiesta.trim().isEmpty()) {
                out.println("ERR SYNTAX");
                return;
            }

            //CONTROLLO BASE INPUT: COMANDO + NUMERO PARTI
            String[] parti = richiesta.trim().split("\\s+");

            if (!parti[0].equals("LEGGI")) {
                out.println("ERR UNKNOWNCMD");
                return;
            }
            if (parti.length != 2) {
                out.println("ERR SYNTAX");
                return;
            }

            //CONTROLLO NOME FILE 
            String nomeFile = parti[1];
            if (nomeFile.contains("/") || nomeFile.contains("\\")
                    || nomeFile.contains("..")) {
                out.println("ERR SYNTAX");
                return;
            }

            //CONTROLLO ESTENSIONE
            for (String tmp[] : TIPI) {
                estensioni.add(tmp[0]);
            }
            String estensione = leggiEstensione(nomeFile, estensioni);
            if(estensione.equals(".null")){
                out.println("ERR SYNTAX");
                return;
            }
            if (estensione.equals(".notfound")) {
                out.println("ERR NOTFOUND");
                return;
            }
            System.out.println(nomeFile);

            //STAMPA RISULTATO
            letturaFile(estensione);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (spazio) {
                out.println();
            }
            terminaConnessione();
        }
    }
    //TROVARE FILE NELLA CARTELLA
    public boolean collegamentoCartella(String nomeFile) {
        cartella = new File("documenti");
        file = new File(cartella, nomeFile);

        System.out.println(file.getAbsolutePath()); // Utile durante le prove
        boolean presente = file.isFile();
        return presente;
    }
    //TROVARE ESTENSIONE FILE
    //CORREZZIONE: nella tappa 2 non avevo notato che seppur funzionasse il ERR SYNTAX 
    //per input come "LEGGI saluto.txt" non avevo considerato "LEGGI ciccio" --> "ERR SYNTAX"
    //mentre l'errore corretto è "ERR NOTFOUND", pertanto ho dovuto modificare "leggiEstensione" 
    //e "collegamentoCartella" per correggere.
    public String leggiEstensione(String nomeFile, List<String> estensioni) {
        for (String estensione : estensioni) {
            String nomeFileCompleto = nomeFile + "." +estensione;                
            if (collegamentoCartella(nomeFileCompleto)) {
                return estensione;
            } else {
                if(collegamentoCartella(nomeFile)){
                    return ".null";
                }
                continue;
            }
        }
        return ".notfound";
    }
    //LETTURA FINALE DEL FILE
    public void letturaFile(String estensione) {
        try (BufferedReader lettoreFile = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            out.println("OK FILE");
            out.println(convertiEstensione(estensione));
            out.println(); // Riga vuota
            spazio = false;

            String riga;
            while ((riga = lettoreFile.readLine()) != null) {
                out.println(riga);
            }
        } catch (IOException e) {
            out.println("ERR READ");
        }
    }
    //CONVERSIONE STAMPA DELL'ESTENSIONE
    public String convertiEstensione(String estensione){
        for (String[] tmp : TIPI) {
            if(estensione.equals(tmp[0])){
                return "Content-Type: "+ tmp[1];
            }else {
                continue;
            }
        }
        return "ERR TYPE";
    }
    //TERMINARE LA CONNESSIONE
    public void terminaConnessione() {
        try {
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
