

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.LinkedHashMap;

/**
 * @Author:  Daniel Rosales-Rodriguez
 * ChatChannel class to help
 * broadcast, add, and remove
 * clients from the channel
 */
public class ChatChannel {

    private String channelName;
    private LinkedHashMap<ObjectOutputStream, String> connections;

    /**
     *
     * @param channelName - name of channel
     */
    ChatChannel(String channelName){
        this.channelName = channelName;
        this.connections = new LinkedHashMap<ObjectOutputStream, String>();
    }

    /**
     * Method returns names of
     * channels in array list
     * @return - channel name
     */
    public String getChannelName(){
        return channelName;
    }

    /**
     * Method returns number
     * of users currently in
     * the channel
     * @return - num users
     */
    public int getNumUsers(){
        return connections.size();
    }

    public static void help() {
        System.out.println("~ Commands ~");
        System.out.println("- /nick <nickname> - sets your nickname");
        System.out.println("- /join <channel> - joins a channel");
        System.out.println("- /leave <channel> - leaves a channel");
        System.out.println("- /list - lists all channels and number of users in channel");
        System.out.println("- /stats - shows server stats");
        System.out.println("- /help - prints out help");
        System.out.println("- /quit - quits the server");
    }

    /**
     * Adds clients to channel
     * @param os - output stream
     * @param nickname - user
     */
    public void addClient(ObjectOutputStream os, String nickname) {
        connections.put(os, nickname);
    }

    /**
     * Removes clients from the channel
     * @param os - ObjectOutputStream
     * @param nickname - user
     */
    public void removeClient(ObjectOutputStream os, String nickname) {
        connections.remove(os, nickname);
    }

    /**
     * Displays who are sending
     * messages/who's seeing
     * them.
     * @param os - user
     * @param message - string
     * @throws IOException
     */
    public void broadcast(ObjectOutputStream os, String message) throws IOException {
        for(ObjectOutputStream user : connections.keySet()){
            if(user.equals(os)){
                continue;
            }
            user.writeObject(message);
        }
    }



    /**
     * Validates that nicknames are not equal
     * @param nickname - nickname
     * @return - boolean
     */
    public boolean checkNickname(String nickname) {
        for(String name : connections.values()){
            if(name.equals(nickname)){
                return true;
            }
        }
        return false;
    }
}