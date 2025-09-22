package opciones;

import java.io.*;
import java.util.*;

public class Opcion1 {

    static class Configuracion {
        int TP;          
        int NPROC;       
        int[][] TAMS;    
    }

    static Configuracion leerConfiguracion(String archivo) throws IOException {
        Configuracion cfg = new Configuracion();
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (linea.startsWith("TP")) {
                    cfg.TP = Integer.parseInt(linea.split("=")[1].trim());
                } else if (linea.startsWith("NPROC")) {
                    cfg.NPROC = Integer.parseInt(linea.split("=")[1].trim());
                } else if (linea.startsWith("TAMS")) {
                    String[] partes = linea.split("=")[1].trim().split(",");
                    cfg.TAMS = new int[partes.length][2];
                    for (int i = 0; i < partes.length; i++) {
                        String[] dims = partes[i].split("x");
                        cfg.TAMS[i][0] = Integer.parseInt(dims[0]); 
                        cfg.TAMS[i][1] = Integer.parseInt(dims[1]);
                    }
                }
            }
        }
        return cfg;
    }

    static void generarArchivoProceso(int idProceso, int TP, int NF, int NC) throws IOException {
        int TAM_MATRIZ = NF * NC * 4;  
        int BASE_A = 0;
        int BASE_B = TAM_MATRIZ;
        int BASE_C = 2 * TAM_MATRIZ;

        List<String> referencias = new ArrayList<>();

        for (int i = 0; i < NF; i++) {
            for (int j = 0; j < NC; j++) {
                int pos = (i * NC + j) * 4;  

                int dvA = BASE_A + pos;
                int dvB = BASE_B + pos;
                int dvC = BASE_C + pos;

                referencias.add("M1:[" + i + "-" + j + "]," + (dvA / TP) + "," + (dvA % TP) + ",r");
                referencias.add("M2:[" + i + "-" + j + "]," + (dvB / TP) + "," + (dvB % TP) + ",r");
                referencias.add("M3:[" + i + "-" + j + "]," + (dvC / TP) + "," + (dvC % TP) + ",w");
            }
        }

        int NR = referencias.size(); 
        int NP = (int) Math.ceil((double) (BASE_C + TAM_MATRIZ) / TP);

        String nombreArchivo = "proc" + idProceso + ".txt";
        try (PrintWriter pw = new PrintWriter(new FileWriter(nombreArchivo))) {
            pw.println("TP=" + TP);
            pw.println("NF=" + NF);
            pw.println("NC=" + NC);
            pw.println("NR=" + NR);
            pw.println("NP=" + NP);

            for (String ref : referencias) {
                pw.println(ref);
            }
        }

        System.out.println("Archivo generado: " + nombreArchivo);
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Uso: java Opcion1 <archivo_configuracion>");
            return;
        }

        String archivoConfig = args[0];
        try {
            Configuracion config = leerConfiguracion(archivoConfig);

            if (config.TAMS.length != config.NPROC) {
                System.out.println("Error: TAMS debe tener un tamaño igual a NPROC.");
                return;
            }

            for (int p = 0; p < config.NPROC; p++) {
                generarArchivoProceso(p, config.TP, config.TAMS[p][0], config.TAMS[p][1]);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
