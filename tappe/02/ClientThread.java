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
    List<String> estensioni = new ArrayList<>(List.of("txt", "md", "csv"));

    public ClientThread(Socket s) {
        this.s = s;
    }

    public void run() {
        try {
            out = new PrintWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8), true);
            in = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
            
            String richiesta = in.readLine();
            if (richiesta == null) {
                return;
            }
            if (richiesta.trim().isEmpty()) {
                out.println("ERR SYNTAX");
                return;
            }

            String[] parti = richiesta.trim().split("\\s+");

            if (!parti[0].equals("LEGGI")) {
                out.println("ERR UNKNOWNCMD");
                return;
            }
            if (parti.length != 2) {
                out.println("ERR SYNTAX");
                return;
            }
            String nomeFile = parti[1];
            if (nomeFile.startsWith("/") || nomeFile.startsWith("\\")
                    || nomeFile.startsWith("..")) {
                out.println("ERR SYNTAX");
                return;
            }

            String estensione = leggiEstensione(nomeFile);
            if(estensione.equals(".null")){
                out.println("ERR SYNTAX");
                return;
            }
            if (!estensioni.contains(estensione)) {
                out.println("ERR NOTFOUND");
                return;
            }
            System.out.println(nomeFile);
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

    public boolean collegamentoCartella(String nomeFile, String estensione) {
        if (estensione.equals(".null")) {
            return false;
        }
        String nomeFileCompleto = nomeFile + "." + estensione;
        cartella = new File("documenti");
        file = new File(cartella, nomeFileCompleto);

        System.out.println(file.getAbsolutePath()); // Utile durante le prove
        boolean presente = file.isFile();
        return presente;
    }

    public String leggiEstensione(String nomeFile) {
        for (String estensione : estensioni) {
            if (collegamentoCartella(nomeFile, estensione)) {
                return estensione;
            } else {
                continue;
            }
        }
        return ".null";
    }

    public void letturaFile(String estensione) {
        try (BufferedReader lettoreFile = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            out.println("OK FILE");
            out.println("TYPE " + estensione);
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

    public void terminaConnessione() {
        try {
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
