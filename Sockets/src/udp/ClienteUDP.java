package udp;

import javax.swing.*;
import java.awt.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ClienteUDP {
    //Creación del socket para la conexión
    private DatagramSocket socket;
    private InetAddress direccion;
    private int puerto = 12345;
    private String nickname;

    //Creación de la ventana principal
    private JFrame frame = new JFrame("Messenger UDP");
    //Creación del area de chat
    private JTextArea Areachat = new JTextArea();
    //Creación del campo de texto para poder escribir los mensajes
    private JTextField texto = new JTextField();
    //Botones para cambiar el color y para enviar los mensajes
    private JButton botonColor = new JButton("Color");
    private JButton botonEnviar = new JButton("Enviar");

    public ClienteUDP() throws Exception {
        //Inicializamos el socket UDP
        socket = new DatagramSocket();
        //Le damos la dirección del localhost
        direccion = InetAddress.getByName("localhost");
        //Ventana para solicitar el nickname
        nickname = JOptionPane.showInputDialog(frame, "Introduce tu NickName:");
        //Enviamose el Nickname al servidor
        enviarMensaje(nickname);

        vistaGUI();
        //Iniciamos el hilo para recibir los mensajes
        new Thread(this::recibirMensaje).start();
    }

    //Edición de la interfaz gráfica
    private void vistaGUI() {
        Areachat.setEditable(false);
        frame.add(new JScrollPane(Areachat), BorderLayout.CENTER);
        /*Configuración para que el texto se envie
              cuando pulses el botón*/
        botonEnviar.addActionListener(e -> {
            enviarMensaje(texto.getText());
            texto.setText("");
        });
        /*Configuración para que el texto se envie
              cuando pulses la tecla enter*/
        texto.addActionListener(e -> {
            enviarMensaje(texto.getText());
            texto.setText("");
        });
        botonEnviar.setPreferredSize(new Dimension(80, 20));
        //Cuando se pulse en el botón color se llama al metodo cambiarColorFondo
        botonColor.addActionListener(e -> cambiarColorFondo());

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(texto, BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
        buttonPanel.add(botonEnviar);
        buttonPanel.add(botonColor);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);
        frame.add(bottomPanel, BorderLayout.SOUTH);
        frame.setSize(300, 400);
        //Cuando se cierra la ventana finaliza el programa
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    //Metodo para poder cambiar el color del fondo
    private void cambiarColorFondo() {
        Color nuevoColor = JColorChooser.showDialog(frame, "Cambiar color de fondo", Areachat.getBackground());
        if (nuevoColor != null) {
            Areachat.setBackground(nuevoColor);
        }
    }

    //Metodo para enviar los mensajes convertido en bytes
    private void enviarMensaje(String message) {
        try {
            byte[] data = message.getBytes();
            DatagramPacket packet = new DatagramPacket(data, data.length, direccion, puerto);
            socket.send(packet);
            //Control error
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*Metodo para recibir los mensajes del servidor, convertirlos
    en texto y mostrarlos por el area de chat*/
    private void recibirMensaje() {
        try {
            byte[] buffer = new byte[1024];
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength());
                Areachat.append(message + "\n");
            }
            //Control error
        } catch (Exception e) {
            Areachat.append("Desconectado del servidor.\n");
        }
    }
    //Inicializamos un cliente
    public static void main(String[] args) throws Exception {
        new ClienteUDP();
    }
}