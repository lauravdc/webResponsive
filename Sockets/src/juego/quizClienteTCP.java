package juego;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class quizClienteTCP {
    //Creación de la ventana principal
    private JFrame frame = new JFrame("Quiz TCP");
    //Creación del area para mostrar mensajes
    private JTextArea areaChat = new JTextArea();
    //Creación del campo de texto para enviar las respuestas
    private JTextField texto = new JTextField();
    //Botón para enviar mensajes
    private JButton botonEnviar = new JButton("Enviar");
    //Enviar mensajes al servidor
    private PrintWriter print;
    //Leer mensajes del servidor
    private BufferedReader leer;

    public quizClienteTCP(String serverAddress, int port) throws IOException {
           //Conectamos al servidor utilizando un socket
            Socket socket = new Socket(serverAddress, port);
            print = new PrintWriter(socket.getOutputStream(), true);
            leer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
           //Utilizamos la configuración de la interfaz gráfica
            vistaGUI();
            //Creamos un nuevo hilo que va a recibir los mensajes del servidor
            new Thread(this::recibirMensaje).start();
        }

        //Método para configurar la interfaz gráfica del jugador
        private void vistaGUI() {
            //Hacemos que el area de chat no pueda ser modificado por el usuario
            areaChat.setEditable(false);
            //Añadimos la barra de desplazamiento en el area de chat cuando sea muy grande
            frame.add(new JScrollPane(areaChat), BorderLayout.CENTER);

            /*Configuración para que el texto se envie
              cuando pulses el botón o se pulse enter*/
            texto.addActionListener(e -> enviarRespuesta());
            botonEnviar.addActionListener(e -> enviarRespuesta());

            //Configuración del panel con el campo de texto y el botón
            JPanel bottomPanel = new JPanel(new BorderLayout());
            bottomPanel.add(texto, BorderLayout.CENTER);
            bottomPanel.add(botonEnviar, BorderLayout.EAST);

            frame.add(bottomPanel, BorderLayout.SOUTH);
            frame.setSize(500, 500);
            //Se cierra la aplicación al cerrar la ventana
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        }

        //Método para enviar la respuesta al servidor
        private void enviarRespuesta() {
        //Envia el texto escrito por el jugador
            print.println(texto.getText());
            texto.setText("");
        }

        //Método para recibir mensajes del jugador
        private void recibirMensaje() {
            try {
                String message;
                //Bucle para leer el mensaje mientras el servidor los envia
                while ((message = leer.readLine()) != null) {
                    //Muestra el mensaje en el area de mensajes
                    areaChat.append(message + "\n");
                }
                //Manejo de excepciones
            } catch (IOException e) {
                areaChat.append("Desconectado del servidor\n");
            }
        }

        //Iniciamos el jugador
        public static void main(String[] args) throws IOException {
        //Conectamos con el servidor en localhost y el puerto 12345
            new quizClienteTCP("localhost", 12345);
        }
    }

