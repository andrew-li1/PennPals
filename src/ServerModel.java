import java.util.*;


/**
 * The {@code ServerModel} is the class responsible for tracking the
 * state of the server, including its current users and the channels
 * they are in.
 * This class is used by subclasses of {@link Command} to:
 *     1. handle commands from clients, and
 *     2. handle commands from {@link ServerBackend} to coordinate 
 *        client connection/disconnection. 
 */
public final class ServerModel implements ServerModelApi {
    

    /**
     * Constructs a {@code ServerModel} and initializes any
     * collections needed for modeling the server state.
     */
    
    
    
    private Map<Integer, String> users;
    private Map<String, Channel> channels;
    
    public ServerModel() {
        users = new TreeMap<>();
        channels = new TreeMap<>();       
    }


    //==========================================================================
    // Client connection handlers
    //==========================================================================

    /**
     * Informs the model that a client has connected to the server
     * with the given user ID. The model should update its state so
     * that it can identify this user during later interactions. The
     * newly connected user will not yet have had the chance to set a
     * nickname, and so the model should provide a default nickname
     * for the user.  Any user who is registered with the server
     * (without being later deregistered) should appear in the output
     * of {@link #getRegisteredUsers()}.
     *
     * @param userId The unique ID created by the backend to represent this user
     * @return A {@link Broadcast} to the user with their new nickname
     */
    public Broadcast registerUser(int userId) {
        String nickname = generateUniqueNickname();
        users.put(userId, nickname);
        return Broadcast.connected(nickname);
    }

    /**
     * Generates a unique nickname of the form "UserX", where X is the
     * smallest non-negative integer that yields a unique nickname for a user.
     * @return the generated nickname
     */
    private String generateUniqueNickname() {
        int suffix = 0;
        String nickname;
        Collection<String> existingUsers = getRegisteredUsers();
        do {
            nickname = "User" + suffix++;
        } while (existingUsers != null && existingUsers.contains(nickname));
        return nickname;
    }

    /**
     * Determines if a given nickname is valid or invalid (contains at least
     * one alphanumeric character, and no non-alphanumeric characters).
     * @param name The channel or nickname string to validate
     * @return true if the string is a valid name
     */
    public static boolean isValidName(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        for (char c : name.toCharArray()) {
            if (!Character.isLetterOrDigit(c)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Informs the model that the client with the given user ID has
     * disconnected from the server.  After a user ID is deregistered,
     * the server backend is free to reassign this user ID to an
     * entirely different client; as such, the model should remove all
     * state of the user associated with the deregistered user ID. The
     * behavior of this method if the given user ID is not registered
     * with the model is undefined.  Any user who is deregistered
     * (without later being registered) should not appear in the
     * output of {@link #getRegisteredUsers()}.
     *
     * @param userId The unique ID of the user to deregister
     * @return A {@link Broadcast} instructing clients to remove the
     * user from all channels
     */
    public Broadcast deregisterUser(int userId) {
        String nickname = users.get(userId);
        Collection<String> recipients = getSameChannelUsers(userId);
        recipients.remove(nickname);
        Collection<String> channelsDelete = new TreeSet<>();
        for (Map.Entry<String, Channel> entry : channels.entrySet()) {
            Channel c = entry.getValue();
            if (c.hasUser(userId)) {
                c.removeUser(userId);
            }
            if (userId == c.getOwner()) {
                channelsDelete.add(entry.getKey());
            }
        }
        for (String s : channelsDelete) {
            channels.remove(s);
        }
        users.remove(userId);
        return Broadcast.disconnected(nickname, recipients);
    }

    

    

    //==========================================================================
    // Server model queries
    // These functions provide helpful ways to test the state of your model.
    // You may also use them in your implementation.
    //==========================================================================
    
    public Collection<String> getSameChannelUsers(int userId) {
        Collection<String> sameChannelUsers = new TreeSet<>();
        for (Map.Entry<String, Channel> entry : channels.entrySet()) {
            Channel c = entry.getValue();
            if (c.hasUser(userId)) {
                for (int i : c.getChannelUsers()) {
                    sameChannelUsers.add(users.get(i));
                }
            }
        }
        return sameChannelUsers;
        
    }
    
    
    public void changeNickname(int userId, String newNickname) {
        users.replace(userId, newNickname);
    }
    
    public void addChannel(int userId, String channelName, boolean inviteOnly) {
        Channel c = new Channel(userId, inviteOnly);
        channels.put(channelName, c);
    }
    
    public void joinChannel(int userId, String channelName) {
        Channel c = channels.get(channelName);
        c.addUser(userId);
    }
    
    public void leaveChannel(int userId, String channelName) {
        Channel c = channels.get(channelName);
        if (c.getOwner()  == userId) {
            Collection<Integer> channelUsers = new TreeSet<>();
            for (int i : c.getChannelUsers()) {
                channelUsers.add(i);
            }
            for (int i : channelUsers) {
                c.removeUser(i);
            }
            channels.remove(channelName);
        } else {
            c.removeUser(userId);
        }
    }
    
    public boolean isInviteOnly(String channelName) {
        Channel c = channels.get(channelName);
        return c.isInviteOnly();
    }
    
    
    /**
     * Gets the user ID currently associated with the given
     * nickname. The returned ID is -1 if the nickname is not
     * currently in use.
     *
     * @param nickname The nickname for which to get the associated user ID
     * @return The user ID of the user with the argued nickname if
     * such a user exists, otherwise -1
     */
    public int getUserId(String nickname) {
        int key = -1;
        for (Map.Entry<Integer, String> entry : users.entrySet()) {
            if (entry.getValue().equals(nickname)) {
                key = entry.getKey();
            }
        }  
        return key;
        
    }

    /**
     * Gets the nickname currently associated with the given user
     * ID. The returned nickname is null if the user ID is not
     * currently in use.
     *
     * @param userId The user ID for which to get the associated
     *        nickname
     * @return The nickname of the user with the argued user ID if
     *          such a user exists, otherwise null
     */
    public String getNickname(int userId) {
        return users.get(userId);
    }

    /**
     * Gets a collection of the nicknames of all users who are
     * registered with the server. Changes to the returned collection
     * should not affect the server state.
     * 
     * This method is provided for testing.
     *
     * @return The collection of registered user nicknames
     */
    public Collection<String> getRegisteredUsers() {
        Collection<String> uCollection = new TreeSet<>();
        uCollection.addAll(users.values());
        return uCollection;
    }

    /**
     * Gets a collection of the names of all the channels that are
     * present on the server. Changes to the returned collection
     * should not affect the server state.
     * 
     * This method is provided for testing.
     *
     * @return The collection of channel names
     */
    public Collection<String> getChannels() {
        Collection<String> cCollection = new TreeSet<>();
        cCollection.addAll(channels.keySet());    
        return cCollection;
    }

     /**
     * Gets a collection of the nicknames of all the users in a given
     * channel. The collection is empty if no channel with the given
     * name exists. Modifications to the returned collection should
     * not affect the server state.
     *
     * This method is provided for testing.
     *
     * @param channelName The channel for which to get member nicknames
     * @return The collection of user nicknames in the argued channel
     */
    public Collection<String> getUsersInChannel(String channelName) {
        Channel c = channels.get(channelName);
        Collection<Integer> usersId = c.getChannelUsers();
        Collection<String> usersNames = new TreeSet<>();
        for (int i : usersId) {
            usersNames.add(users.get(i));
        }
        return usersNames;
    }

    /**
     * Gets the nickname of the owner of the given channel. The result
     * is {@code null} if no channel with the given name exists.
     *
     * This method is provided for testing.
     *
     * @param channelName The channel for which to get the owner nickname
     * @return The nickname of the channel owner if such a channel
     * exists, otherwise null
     */
    public String getOwner(String channelName) {
        String name = null;
        if (getChannels().contains(channelName)) {
            Channel c = channels.get(channelName);
            name = users.get(c.getOwner());
        }
        return name;
    }

}
