package sigepac;

import sigepac.mvc.servidor.ServidorMvc;
import sigepac.capas.servidor.ServidorCapas;
import sigepac.poo.servidor.ServidorPoo;
import sigepac.hexagonal.servidor.ServidorHexagonal;
import sigepac.pruebas.PruebaTodasLasArquitecturas;

import java.util.Scanner;

public class Lanzador {
    public static void main(String[] args) {
        if (args.length > 0) {
            ejecutarComando(args[0].toLowerCase());
            return;
        }

        System.out.println("================================================================================");
        System.out.println("  SISTEMA SIGEPAC - GESTION DE PAGOS PLAZA CIVICA DE PITALITO (JAVA)");
        System.out.println("================================================================================");
        System.out.println("  Seleccione la arquitectura que desea iniciar:");
        System.out.println("  1. Arquitectura MVC (Puerto 8001)");
        System.out.println("  2. Arquitectura en Capas (Puerto 8002)");
        System.out.println("  3. Arquitectura Orientada a Objetos (Puerto 8003)");
        System.out.println("  4. Arquitectura Hexagonal (Puerto 8004)");
        System.out.println("  5. Iniciar TODAS las 4 arquitecturas en paralelo");
        System.out.println("  6. Ejecutar Suite de Pruebas Automatizadas (34/34 Tests)");
        System.out.println("  0. Salir");
        System.out.print("  Ingrese una opcion (0-6): ");

        Scanner sc = new Scanner(System.in);
        String opcion = sc.nextLine().trim();
        switch (opcion) {
            case "1": ejecutarComando("mvc"); break;
            case "2": ejecutarComando("capas"); break;
            case "3": ejecutarComando("poo"); break;
            case "4": ejecutarComando("hexagonal"); break;
            case "5": ejecutarComando("todas"); break;
            case "6": ejecutarComando("pruebas"); break;
            default:
                System.out.println("Saliendo de SIGEPAC...");
                break;
        }
    }

    public static void ejecutarComando(String comando) {
        try {
            switch (comando) {
                case "mvc":
                    System.out.println("\n[SIGEPAC] Iniciando Servidor 1 (MVC)...");
                    ServidorMvc.main(new String[0]);
                    break;
                case "capas":
                    System.out.println("\n[SIGEPAC] Iniciando Servidor 2 (Capas)...");
                    ServidorCapas.main(new String[0]);
                    break;
                case "poo":
                    System.out.println("\n[SIGEPAC] Iniciando Servidor 3 (Orientada a Objetos)...");
                    ServidorPoo.main(new String[0]);
                    break;
                case "hexagonal":
                    System.out.println("\n[SIGEPAC] Iniciando Servidor 4 (Hexagonal)...");
                    ServidorHexagonal.main(new String[0]);
                    break;
                case "todas":
                    System.out.println("\n[SIGEPAC] Iniciando las 4 arquitecturas simultáneamente...");
                    ServidorMvc.main(new String[0]);
                    ServidorCapas.main(new String[0]);
                    ServidorPoo.main(new String[0]);
                    ServidorHexagonal.main(new String[0]);
                    System.out.println("\n>>> TODAS LAS ARQUITECTURAS ACTIVAS:");
                    System.out.println(" - MVC:                http://localhost:8001");
                    System.out.println(" - En Capas:           http://localhost:8002");
                    System.out.println(" - Orientada a Objetos:http://localhost:8003");
                    System.out.println(" - Hexagonal:          http://localhost:8004");
                    System.out.println("====================================================================\n");
                    break;
                case "pruebas":
                    PruebaTodasLasArquitecturas.main(new String[0]);
                    break;
                default:
                    System.out.println("Comando no reconocido: " + comando);
                    System.out.println("Opciones válidas: mvc, capas, poo, hexagonal, todas, pruebas");
                    break;
            }
        } catch (Exception e) {
            System.err.println("Error al iniciar: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
