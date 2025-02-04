package udp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.util.*;

public class ServidorUDP {
    //Variables estáticas para que cada cliente pueda acceder a ellas
    private static final int PORT = 12345;
    //HashSet para almacenar los nicknames de los clientes
    private static Set<String> nicknames = new HashSet<>();
    //HashMap para mapear el SocketAdress de los clientes con su nickname
    private static Map<SocketAddress, String> clientes = new HashMap<>();
    //lista para guardar el historial de todos los mensajes
    private static List<String> historialMensaje = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        //Creamos el socket con el puerto 12345
        DatagramSocket socket = new DatagramSocket(PORT);
        //Array de bytes para recibir los datos
        byte[] buffer = new byte[1024];

        //Bucle infinito para que el servidor esté siempre escuchando
        while (true) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            //Se recibe un paquete de datos
            socket.receive(packet);
            //Convertimos los datos binarios recibidos en texto legible
            String usuario = new String(packet.getData(), 0, packet.getLength());
            //Obtenemos la dirección del cliente
            SocketAddress clientAddress = packet.getSocketAddress();
            //Si el cliente es nuevo verificamos que el nickname no esté ya en uso
            if (!clientes.containsKey(clientAddress)) {
                if (nicknames.contains(usuario)) {
                    //Si está en uso mandamos mensaje de error
                    MensajeErr("El NickName ya está en uso", socket, packet);
                } else {
                    /*Si no está en uso lo guarda en el hashset y lo mapea
                    con el SocketAdress en el hashMap*/
                    nicknames.add(usuario);
                    clientes.put(clientAddress, usuario);
                    //Se envia el historial para que pueda ver los mensajes antiguos
                    enviarHistorial(socket, clientAddress);
                    //Se envia mensaje de Bienvenida
                    EnviarMensaje("Bienvenido: "+usuario ,socket);
                    EnviarMensaje(usuario + " ha entrado al chat", socket);
                }
                //Si ya está registrado se envia como un mensaje con su nombre
            } else {
                String mensajeUsu = clientes.get(clientAddress) + ": " + usuario;
                synchronized (historialMensaje) {
                    historialMensaje.add(mensajeUsu);
                }
                EnviarMensaje(mensajeUsu, socket);
            }
        }
    }

    //Metodo para mandar un Mensaje de error
    private static void MensajeErr(String message, DatagramSocket socket, DatagramPacket packet) throws Exception {
        byte[] data = message.getBytes();
        DatagramPacket response = new DatagramPacket(data, data.length, packet.getSocketAddress());
        socket.send(response);
    }

    //Metodo para mandar el historial a los clientes nuevos
    private static void enviarHistorial(DatagramSocket socket, SocketAddress client) throws Exception {
        for (String msg : historialMensaje) {
            byte[] data = msg.getBytes();
            DatagramPacket packet = new DatagramPacket(data, data.length, client);
            socket.send(packet);
        }
    }
    //Metodo para enviar un mensaje a todos los clientes conectados
    private static void EnviarMensaje(String message, DatagramSocket socket) throws Exception {
        for (SocketAddress client : clientes.keySet()) {
            byte[] data = message.getBytes();
            DatagramPacket packet = new DatagramPacket(data, data.length, client);
            socket.send(packet);
        }
    }
}