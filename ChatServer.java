import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Daniel Rosales-Rodriguez
 * A multi-threaded-multi-channel Server that sends String objects to clients. You may
 * need to open port 5005 in the firewall on the host machine (unless you are
 * locally).
 */

public class ChatServer {
    private ServerSocket serverSocket;
    public static final int NUM_THREADS = 4;
    public static ExecutorService pool;

    private static ChatChannel[] channels;

    private static long serverStartTimestamp = (int) System.currentTimeMillis();


    public static void main(String[] args) {
        String[] channelName = {"Alpha", "Beta", "Gamma", "Epsilon"
                , "Zeta", "Eta", "Theta", "Iota"};
        if(args.length != 1){
            System.out.println("Usage: java ChatServer <port>");
            System.exit(1);
        }

        ChatServer server = new ChatServer(Integer.parseInt(args[0]));
        channels = new ChatChannel[8]; //Array of 8 chat channels
        for (int i = 0; i < 8; i++) {
            channels[i] = new ChatChannel(channelName[i]);
        }
        server.runServer();

    }


    /**
     * ChatServer object with port
     * and fixed number of threads (4)
     *
     * @param port - port number
     */
    public ChatServer(int port) {
        try {
            pool = Executors.newFixedThreadPool(NUM_THREADS);
            serverSocket = new ServerSocket(port);
            System.out.println("Chat Server: up and running on port " + port + " " + InetAddress.getLocalHost());
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    /**
     * The method that handles the clients, one at a time.
     */
    public void runServer() {
        Socket client;
        while (true) {
            try {
                client = serverSocket.accept(); // block statement, listening in
                pool.execute(new ChatServerConnection(client));
                // Thread.sleep(4000); //4 secs
                //client.close();
            } catch (IOException e) {
                System.err.println(e);
            }
        }
    }

    private class ChatServerConnection implements Runnable {

        private Socket client;
        private ObjectInputStream objectIn;
        private ObjectOutputStream objectOut;
        private String nickname;
        private ChatChannel currentChannel;

        /**
         * Socket constructor for client to bind
         *
         * @param client
         * @throws SocketException
         */
        ChatServerConnection(Socket client) throws SocketException {
            this.client = client;
        }


        @Override
        public void run() {
            try {
                objectOut = new ObjectOutputStream(client.getOutputStream());
                objectIn = new ObjectInputStream(client.getInputStream());
                System.out.println("ChatServer Connection: Received connect from " + client.getInetAddress().getHostAddress());

                while (true) {

                    String message = (String) objectIn.readObject();
                    String[] messageParts = message.split(" "); //debug here
                    String response = " ";

                    //validates slash command
                    if (messageParts[0].charAt(0) == '/') {
                        // begin to process message requests
                        switch (messageParts[0]) {
                            case "/nick":
                                // store new nickname
                                String newNickname = messageParts[1];
                                // if user is in a channel
                                if (currentChannel != null) {
                                    // nickname not in use, so we can proceed to update it
                                    if (!currentChannel.checkNickname(newNickname)) {
                                        // broadcast nickname change
                                        String changeAnnouncement = nickname + " has changed their nickname to " +
                                                newNickname + ".";
                                        // broadcast announcement
                                        currentChannel.broadcast(null, changeAnnouncement);
                                        // update nickname in hashmap
                                        currentChannel.addClient(objectOut, newNickname);
                                        nickname = newNickname;
                                        response = "Nickname set to " + newNickname;
                                    } else {
                                        response = "Nickname '" + newNickname + "' is already in use." +
                                                " Please choose a different one.";
                                    }
                                } else {
                                    // if not in channels then change nickname accordingly
                                    nickname = newNickname;
                                    response = "Nickname set to " + newNickname;
                                }
                                objectOut.writeObject(response);
                                objectOut.flush();
                                break;

                            case "/list":
                                // For each channel
                                // + Add it to the response
                                for (ChatChannel channel : channels) {
                                    response += channel.getChannelName();
                                    response += channel.getNumUsers();
                                    response += "\n";
                                }
                                objectOut.writeObject(response);
                                objectOut.flush();
                                break;

                            case "/join":
                                // validate nickname for null object or empty string
                                if (nickname == null || nickname.trim().isEmpty()) {
                                    response = "Set a nickname before joining a channel. Invoke /nick <nickname>.";
                                    // otherwise keep going
                                } else {
                                    // validate for whether user is in a channel
                                    if (currentChannel != null) {
                                        // ensures that they are already in a channel
                                        if(currentChannel.getChannelName().equals(messageParts[1])){
                                            response = "You are already in channel " + currentChannel.getChannelName();
                                            // notifies users that they must leave first before joining another channel
                                        } else {
                                            response = "You must leave the current channel (" +
                                                    currentChannel.getChannelName() + ") before joining another.";
                                        }
                                        // finally goes through adding user who has yet to join a channel
                                    } else {
                                        boolean channelFound = false;
                                        // checks for channel array
                                        for (ChatChannel channel : channels) {
                                            if (channel.getChannelName().equals(messageParts[1])) {
                                                // adds user
                                                channel.addClient(objectOut, nickname);
                                                // update the currentChannel reference
                                                currentChannel = channel;
                                                response = nickname + " joined channel " + channel.getChannelName();
                                                // notify channel user has joined
                                                channel.broadcast(objectOut, response);
                                                channelFound = true;
                                                // breaks out of the loop
                                                break;
                                            }
                                        }
                                        if (!channelFound) {
                                            response = "Channel '" + messageParts[1] + "' not found.";
                                        }
                                    }
                                }
                                objectOut.writeObject(response);
                                objectOut.flush();
                                break;


                            case "/leave":
                                // user is in the channel they want to leave
                                if (currentChannel != null && currentChannel.getChannelName().equals(messageParts[1])) {
                                    // remove client from channel
                                    currentChannel.removeClient(objectOut, nickname);
                                    response = nickname + " has left channel " + currentChannel.getChannelName();
                                    // notify channel user has left
                                    currentChannel.broadcast(objectOut, response);
                                    currentChannel = null;

                                } else {
                                    // user is trying to leave a channel they're not in or no channel at all
                                    if (currentChannel == null) {
                                        response = "You're not in any channel to leave.";
                                    } else {
                                        // user is in a different channel than the one they're trying to leave
                                        response = "You're not in channel " + messageParts[1] + ".";
                                    }
                                }

                                objectOut.writeObject(response);
                                objectOut.flush();
                                break;

                            case "/quit":
                                if (currentChannel != null) {
                                    currentChannel.removeClient(objectOut, nickname);
                                    break;
                                }
                                try {
                                    objectIn.close();
                                    objectOut.close();
                                    client.close();
                                    return;
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }

                            case "/help":
                                break;

                            case "/stats":
                                // server stats displaying users in each channel as well as server runtime
                                long serverUptime = (int) System.currentTimeMillis() - serverStartTimestamp;
                                response = "Server uptime: " + (serverUptime/1000) + " s\n";
                                for (ChatChannel channel : channels) {
                                    response += channel.getNumUsers();
                                    response += "\n";
                                }
                                response = "Users in each channel: \n" + response;
                                objectOut.writeObject(response);
                                objectOut.flush();

                                break;
                        }
                    } else {
                        // broadcast user chat in channel
                        currentChannel.broadcast(objectOut, nickname + ": " + message);
                    }
                }
            } catch (EOFException | SocketException exception){
                System.out.println("ChatServer Connection: " + client.getInetAddress().getHostAddress() + " has disconnected.");
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    if(objectIn != null){ objectIn.close(); }
                    if(objectOut != null){ objectOut.close(); }
                    if(client != null){ client.close(); }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

    }
}