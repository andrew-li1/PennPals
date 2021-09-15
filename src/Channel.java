import java.util.*;


public class Channel implements Comparable<Object> {
     
    private Collection<Integer> channelUsers;
    private int owner;
    private String name;
    private boolean inviteOnly;
    
    public Channel(int o, boolean i) {
        owner = o;
        inviteOnly = i;
        channelUsers = new TreeSet<>();
        channelUsers.add(owner);
    }
    
    public void addUser(int n) {
        channelUsers.add(n);
    }
    
    public void removeUser(int n) {
        channelUsers.remove(n);
    }
    
    public boolean hasUser(int n) {
        return channelUsers.contains(n);
    }
    
    public Collection<Integer> getChannelUsers() {
        return channelUsers;
    }
    
    public int getOwner() {
        return owner;
    }
    
    public boolean isInviteOnly() {
        return inviteOnly;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((channelUsers == null) ? 0 : channelUsers.hashCode());
        result = prime * result + (inviteOnly ? 1231 : 1237);
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + owner;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Channel other = (Channel) obj;
        if (channelUsers == null) {
            if (other.channelUsers != null) {
                return false;
            }
        } else if (!channelUsers.equals(other.channelUsers)) {
            return false;
        }
        if (inviteOnly != other.inviteOnly) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (owner != other.owner) {
            return false;
        }
        return true;
    }
    
    @Override
    public int compareTo(Object obj) {
        if (this == obj) {
            return 0;
        }
        if (obj == null) {
            return 1;
        }
        if (getClass() != obj.getClass()) {
            return -1;
        }
        Channel other = (Channel) obj;
        if (channelUsers == null) {
            if (other.channelUsers != null) {
                return -1;
            }
        } else if (!channelUsers.equals(other.channelUsers)) {
            return 1;
        }
        if (inviteOnly != other.inviteOnly) {
            return 1;
        }
        if (name == null) {
            if (other.name != null) {
                return -1;
            }
        } else if (!name.equals(other.name)) {
            return 1;
        }
        if (owner != other.owner) {
            return 1;
        }
        return 0;
    }
    
    


}
