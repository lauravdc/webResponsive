package tcp;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClienteTCP {
    //Creación del socket para la conexión TCP
    private Socket socket;
    //Creación del PrintWriter para enviar datos al servidor
    private PrintWriter print;
    //Creación del BufferedReader para enviar datos del servidor
    private BufferedReader leer;

    //Creación de la ventana principal
    private JFrame frame = new JFrame("Messenger TCP");
    //Creación del area para mostrar mensajes
    private JTextArea areaChat = new JTextArea();
    //Creación del campo de texto para enviar las respuestas
    private JTextField texto = new JTextField();
    //Botón para enviar mensajes
    private JButton botonEnviar = new JButton("Enviar");
    //Botón para cambiar de color
    private JButton botonColor = new JButton("color");


    public ClienteTCP(String serverAddress, int port) throws IOException {
        //Hacemos la conexión al servidor inicializando las variables
        socket = new Socket(serverAddress, port);
        print = new PrintWriter(socket.getOutputStream(), true);
        leer = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        //Mostramos una ventana de dialogo emergente para preguntar por el nickName
        String nickname = JOptionPane.showInputDialog(frame, "Introduce tu NickName:");
        //Enviamos el nickname al servidor
        print.println(nickname);


        vistaGUI();
        //Iniciamos un hilo para recibir los mensajes
        new Thread(this::recibirMensaje).start();
    }

    //Edición de la interfaz gráfica
    private void vistaGUI() {
        areaChat.setEditable(false);
        frame.add(new JScrollPane(areaChat), BorderLayout.CENTER);
        /*Configuración para que el texto se envie
              cuando pulses el botón*/
        botonEnviar.addActionListener(e -> {
            print.println(texto.getText());
            texto.setText("");
        });
        /*Configuración para que el texto se envie
              cuando pulses la tecla enter*/
        texto.addActionListener(e -> {
            print.println(texto.getText());
            texto.setText("");
        });
        botonEnviar.setPreferredSize(new Dimension(80, 30));
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
        Color nuevoColor = JColorChooser.showDialog(frame, "Cambiar color del fondo", areaChat.getBackground());
        if (nuevoColor != null) {
            areaChat.setBackground(nuevoColor);
        }
    }

    //Metodo para recibir mensajes del servidor y mostrarlos el el area del chat
    private void recibirMensaje() {
        try {
            String message;
            while ((message = leer.readLine()) != null) {
                areaChat.append(message + "\n");
            }
        } catch (IOException e) {
            areaChat.append("Desconectado del servidor\n");
        }
    }

    //Inicializamos un cliente
    public static void main(String[] args) throws IOException {
        new ClienteTCP("localhost", 12345);
    }
}

