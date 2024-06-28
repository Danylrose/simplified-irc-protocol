

import java.io.*;
import java.net.Socket;
import java.util.Scanner;


/**
 * @author  Daniel Rosales-Rodriguez
 * Class for client to connect to server
 */
public class ChatClient {
    static Socket socket;
    static InputStream in;
    static OutputStream out;
    static ObjectInputStream objectIn;
    static ObjectOutputStream objectOut;
    static ChatListener listener;


    public static void main(String args[]) throws IOException {

        try {
            requests();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }



    ChatClient(String host, int port) throws IOException {
        socket = new Socket(host, port);
        out = socket.getOutputStream();
        objectOut = new ObjectOutputStream(out);
        in = socket.getInputStream();
        objectIn = new ObjectInputStream(in);
    }

    public static void requests() throws IOException, InterruptedException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to Chat! Connect to a server using /connect <host> <port>.");
        while (true) {

            String input = scanner.nextLine();
            String[] messageParts = input.split(" "); //debug here
            String response = " ";
            // if the message is empty
            if(messageParts.length == 0){
                System.out.println("Invalid command. Please try again.");
                continue;
            }
            if (messageParts[0].charAt(0) == '/') {

                switch (messageParts[0]) {


                    case "/connect":
                        // if the message is not 3 parts
                        if (messageParts.length != 3) {
                            System.out.println("Invalid command. Please try again.");
                            break;
                            // if the socket is not null
                        } else if (socket != null) {
                            System.out.println("You are already connected to a server.");
                            break;
                        }
                        try {
                            // instantiate a new ChatClient
                            new ChatClient(messageParts[1], Integer.parseInt(messageParts[2]));
                            response = "Connected to Chat at " + messageParts[1] + " on port " + messageParts[2]
                            + "!\n" + "Invoke /help for a list of commands.";
                            System.out.println(response);
                            // starts the listener thread
                            listener = new ChatListener();
                            listener.start();

                        } catch (IOException e) {
                            System.err.println("Could not connect to server.");
                            throw new RuntimeException(e);
                        }
                        break;

                    case "/nick":
                        // if the socket is null
                        if(socket == null) {
                            System.out.println("You are not connected to a server.");
                            break;
                         // if the message is not 2 parts
                        } else if (messageParts.length != 2) {

                            System.out.println("Invalid command.");
                            System.out.println("/nick <nickname> to set your nickname.");
                            break;
                        }

                        objectOut.writeObject(input);
                        objectOut.flush();
                        break;

                    case "/list":

                        if(socket == null) {
                            System.out.println("You are not connected to a server.");
                            break;
                        }
                        objectOut.writeObject("/list");
                        break;

                    case "/join":
                        if(socket == null) {
                            System.out.println("You are not connected to a server.");
                            break;
                        } else if(messageParts.length != 2) {
                            System.out.println("Invalid command. Please try again.");
                            System.out.println("/join <channel> to join a channel");
                            break;
                        }
                        objectOut.writeObject(input);
                        break;

                    case "/leave":
                        if(socket == null) {
                            System.out.println("You are not connected to a server");
                            break;
                        } else if(messageParts.length != 2) {
                            System.out.println("Invalid command. Please try again.");
                            System.out.println("/leave <channel> to leave a channel");
                            break;
                        }
                        objectOut.writeObject(input);
                        objectOut.flush();
                        break;

                    case "/quit":
                        if(socket == null) {
                            System.out.println("You are not connected to a server.");
                            break;
                        }
                        listener.stopListening();
                        socket = null;
                        System.out.println("You have quit Chat! Goodbye!\n");
                        System.out.println("Welcome to Chat! Connect to a server using /connect <host> <port>.");
                        System.out.println();
                        break;

                    case "/help":
                        if(socket == null) {
                            System.out.println("You are not connected to a server");
                            break;
                        } else if(messageParts.length != 1) {
                            System.out.println("Invalid command");
                            System.out.println("/help to print out help");
                            break;
                        }
                        ChatChannel.help();
                        break;

                    case "/stats":
                        if(socket == null) {
                            System.out.println("You are not connected to a server");
                            break;
                        } else if(messageParts.length != 1) {
                            System.out.println("Invalid command");
                            System.out.println("/stats for server stats.");
                            break;
                        }
                        objectOut.writeObject("/stats");
                        break;
                }
            } else {
                if(socket == null) {
                    System.out.println("You are not connected to a server");
                    continue;
                }
                objectOut.writeObject(input);
                objectOut.flush();
            }
        }

    }

    private static class ChatListener extends Thread {
        boolean isNotDone = true;
        @Override
        public void run() {
            while (isNotDone) {
                try {
                    // read the object from the input stream
                    String message = (String) objectIn.readObject();
                    System.out.println(message);

                } catch (IOException e) {
                    e.printStackTrace();
                    stopListening();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    stopListening();
                    return;
                }
            }
            try {
                objectIn.close();
                objectOut.close();
                socket.close();
                return;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public void stopListening(){
            isNotDone = false;
            interrupt();
        }


    }
}