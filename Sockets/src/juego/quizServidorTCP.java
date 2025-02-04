package juego;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class quizServidorTCP {
    // Lista para tener a todos los clientes
    private static List<PrintWriter> jugadores = new ArrayList<>();

    // Preguntas y respuestas del quiz
    private static List<String> preguntas = Arrays.asList(
            "¿Qué ciclista español ganó el Tour de Francia en 2008?",
            "¿Cuál es la capital de Francia?",
            "¿En qué país nació el ciclista Alberto Contador?",
            "¿Quién escribió 'Cien años de soledad'?",
            "¿Cuál es el planeta más cercano al sol?",
            "¿Quién es el ciclista más joven en ganar el Tour de Francia? "
    );
    private static List<String> respuestas = Arrays.asList("Carlos Sastre","Paris","España","Gabriel Garcia Marquez", "Mercurio", "Henri Cornet");

    public static void main(String[] args) throws IOException {
        // Creamos el ServerSocket que escuchará en el puerto 12345
        ServerSocket serverSocket = new ServerSocket(12345);
        System.out.println("Servidor de Quiz iniciado");

        // Bucle infinito para aceptar siempre conexiones de clientes
        while (true) {
            Socket socket = serverSocket.accept();
            // Creamos un nuevo hilo para manejar al cliente conectado
            new Thread(() -> manejarJugador(socket)).start();
        }
    }

    //Metodo para manejar la interacción con un cliente
    private static void manejarJugador(Socket socket) {
        //Contador para los puntos ganados de cada jugador
        int contador=0;
        try {
            //Leemos los datos del jugador
            BufferedReader leer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            //Escribimos los datos para el jugador
            PrintWriter print = new PrintWriter(socket.getOutputStream(), true);

            synchronized (jugadores) {
                //Escribimos el mensaje de bienvenida
                print.println("Bienvenido al Quiz más divertido");
                //Agregamos al jugador a la lista de jugadores
                jugadores.add(print);
            }
            //Introducir nombre para el jugador
            print.println("Introduce tu nombre");
            String nombre = leer.readLine();

            //Bucle para hacer todas las preguntas al jugador
            for (int i = 0; i < preguntas.size(); i++) {
                //Imprime las preguntas
                print.println(preguntas.get(i));
                //Lee la respuesta escrita por el jugador
                String respuestaCliente = leer.readLine();
                //Compara la respuesta del jugador con las respuestas correctas
                if (respuestaCliente != null && respuestaCliente.equalsIgnoreCase(respuestas.get(i))) {
                    //Si es correcto envia mensaje y suma el punto al contador
                    print.println("¡Correcto!");
                    contador++;
                } else {
                    //Si es incorrecto muestra la respuesta correcta
                    print.println("Incorrecto \nLa respuesta correcta es: " + respuestas.get(i));
                }
            }
            //Muestra la puntuación total conseguida y un mensaje de despedida
            print.println("\n!!Has conseguido "+contador+" puntos!!");
            print.println("Fin del quiz \nGracias por participar "+nombre);
            /*Envia a los demás jugadores conectados un mensaje
              cuando otro jugador termine*/
            enviarAClientes(nombre+ " a finalizado el quiz");

            //Manejo de excepciones
        } catch (IOException e) {
            e.printStackTrace();
            //Se cierra la conexión al terminar
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    //Metodo para enviar un mensaje a los otros clientes conectados
    private static void enviarAClientes(String mensaje) {
        synchronized (jugadores) {
            for (PrintWriter cliente : jugadores) {
                cliente.println(mensaje);
            }
        }
    }
    }

