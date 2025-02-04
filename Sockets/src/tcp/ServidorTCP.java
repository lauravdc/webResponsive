package tcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ServidorTCP {
    //HashSet para almacenar los nicknames de los clientes
    private static Set<String> nicknames = new HashSet<>();
    //lista de objetos PrintWriter para cada cliente conectado
    private static List<PrintWriter> clientes = new ArrayList<>();
    // Lista para almacenar el historial de mensajes enviados en el chat
    private static List<String> HistorialMensajes = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        // Creamos el socket del servidor que escucha en el puerto 12345
        ServerSocket serverSocket = new ServerSocket(12345);

        //Bucle infinito para aceptar siempre conexiones
        while (true) {
            //Aceptamos la conexión
            Socket socket = serverSocket.accept();
            //Creamos hilo para manejar a cada cliente
            new Thread(() -> ManejadorCliente(socket)).start();
        }
    }

    // Metodo para manejar la conexión y comunicación con un cliente
    private static void ManejadorCliente(Socket socket) {
        try {
            //Creamos un BufferedReader para leer mensajes desde el cliente
            BufferedReader leer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            //Creamos un PrintWriter para enviar mensajes al cliente
            PrintWriter print = new PrintWriter(socket.getOutputStream(), true);

            //Leemos el apodo del cliente y mandamos mensaje de bienvenida
            String nickname = leer.readLine();
            print.println("Bienvenido: "+ nickname);
            //Sincronizamos para verificar si el nickname ya está en uso
            synchronized (nicknames) {
                //Si está en uso se envia mensaje de error y se cierra la conexión
                if (nicknames.contains(nickname)) {
                    print.println("El NickName ya está ocupado , la conexión se cerrará");
                    socket.close();
                    return;
                }
                //Si no está en uso se agrega al HashSet de nicknames
                nicknames.add(nickname);
            }
            //Agregamos el PrintWriter del cliente a la lista de clientes
            clientes.add(print);

            //Enviamos historial de mensajes al nuevo cliente
            synchronized (HistorialMensajes) {
                for (String mensaje : HistorialMensajes) {
                    print.println(mensaje);
                }
            }

            //Mensaje a todos los clientes cuando se une otro cliente
            EnviarMensaje(nickname + " ha entrado al chat");

            String mensajes;
            while ((mensajes = leer.readLine()) != null) {
                String formateoMensaje = nickname + ": " + mensajes;
                //Guardamos el mensaje escrito del cliente en el historial de mensajes
                synchronized (HistorialMensajes) {
                    HistorialMensajes.add(formateoMensaje);
                }
                EnviarMensaje(formateoMensaje);
            }
            //Manejo de errores
        } catch (IOException e) {
            System.out.println("Cliente desconectado");
            //Cuando el cliente se desconecta se elimina su nickname y se cierra la conexión
        } finally {
            synchronized (nicknames) {
                nicknames.remove(socket);
            }
            //Enviamos mensaje a todos los clientes de la salida del cliente
            EnviarMensaje("El usuario ha salido del chat");
        }
    }

    // Metodo para enviar un mensaje a todos los clientes conectados
    private static void EnviarMensaje(String mens) {
        for (PrintWriter cliente : clientes) {
            cliente.println(mens);
        }
    }
}


