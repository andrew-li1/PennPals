import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

public class ServerModelTest {
    private ServerModel model;

    /**
     * Before each test, we initialize model to be 
     * a new ServerModel (with all new, empty state)
     */
    @BeforeEach
    public void setUp() {
        // We initialize a fresh ServerModel for each test
        model = new ServerModel();
    }

    /** 
     * Here is an example test that checks the functionality of your 
     * changeNickname error handling. Each line has commentary directly above
     * it which you can use as a framework for the remainder of your tests.
     */
    @Test
    public void testInvalidNickname() {
        // A user must be registered before their nickname can be changed, 
        // so we first register a user with an arbitrarily chosen id of 0.
        model.registerUser(0);

        // We manually create a Command that appropriately tests the case 
        // we are checking. In this case, we create a NicknameCommand whose 
        // new Nickname is invalid.
        Command command = new NicknameCommand(0, "User0", "!nv@l!d!");

        // We manually create the expected Broadcast using the Broadcast 
        // factory methods. In this case, we create an error Broadcast with 
        // our command and an INVALID_NAME error.
        Broadcast expected = Broadcast.error(
            command, ServerResponse.INVALID_NAME
        );

        // We then get the actual Broadcast returned by the method we are 
        // trying to test. In this case, we use the updateServerModel method 
        // of the NicknameCommand.
        Broadcast actual = command.updateServerModel(model);

        // The first assertEquals call tests whether the method returns 
        // the appropriate Broadcast.
        assertEquals(expected, actual, "Broadcast");

        // We also want to test whether the state has been correctly
        // changed.In this case, the state that would be affected is 
        // the user's Collection.
        Collection<String> users = model.getRegisteredUsers();

        // We now check to see if our command updated the state 
        // appropriately. In this case, we first ensure that no 
        // additional users have been added.
        assertEquals(1, users.size(), "Number of registered users");

        // We then check if the username was updated to an invalid value 
        // (it should not have been).
        assertTrue(users.contains("User0"), "Old nickname still registered");

        // Finally, we check that the id 0 is still associated with the old, 
        // unchanged nickname.
        assertEquals(
            "User0", model.getNickname(0), 
            "User with id 0 nickname unchanged"
        );
    }

    /*
     * Your TAs will be manually grading the tests you write in this file.
     * Don't forget to test both the public methods you have added to your
     * ServerModel class as well as the behavior of the server in different
     * scenarios.
     * You might find it helpful to take a look at the tests we have already
     * provided you with in ChannelsMessagesTest, ConnectionNicknamesTest,
     * and InviteOnlyTest.
     */
    
    @Test
    public void testValidNickname() {
        model.registerUser(5);
        Command command = new NicknameCommand(5, "User5", "userFive");
        Broadcast actual = command.updateServerModel(model);
        Collection<String> users = model.getRegisteredUsers();
        Broadcast expected = Broadcast.okay(command, users);
        assertEquals(expected, actual);
        assertEquals(1, users.size());
        assertFalse(users.contains("User5"));
        assertEquals(
            "userFive", model.getNickname(5));
    }

    @Test
    public void testDeregisterUser() {
        model.registerUser(0);
        model.registerUser(1);
        model.registerUser(2);
        model.deregisterUser(2);
        assertNull(model.getNickname(2));
        Collection<String> users = model.getRegisteredUsers();
        assertEquals(2, users.size());
        assertFalse(users.contains("User2"));
    }
    
    @Test
    public void testDeregisterOwnerRemovesChannels() {
        model.registerUser(0);
        model.registerUser(1);
        model.registerUser(2);
        model.registerUser(3);
        Command create1 = new CreateCommand(0, "User0", "Channel0", false);
        create1.updateServerModel(model);
        Command create2 = new CreateCommand(0, "User0", "Channel10", false);
        create2.updateServerModel(model);
        Command join = new JoinCommand(1, "User1", "Channel0");
        join.updateServerModel(model);
        model.deregisterUser(0);
        Collection<String> users = model.getRegisteredUsers();
        assertFalse(users.contains("User0"));
        assertEquals(3, users.size());
        Collection<String> channels = model.getChannels();
        assertEquals(0, channels.size());       
    }
    
    @Test
    public void testGetUserId() {
        model.registerUser(0);
        model.registerUser(1);
        model.registerUser(2);
        assertEquals(1, model.getUserId("User1"));
        assertEquals(-1, model.getUserId("User3"));//null, should return -1
    }
    
    @Test
    public void testGetNickname() {
        model.registerUser(0);
        assertNull(model.getNickname(2));
        assertEquals("User0", model.getNickname(0));
        Command command = new NicknameCommand(0, "User0", "userZero");
        command.updateServerModel(model);
        assertEquals("userZero", model.getNickname(0));
    }
    
    @Test
    public void testGetRegisteredUsersEncapsulation() {
        model.registerUser(0);
        model.registerUser(1);
        model.registerUser(2);
        model.registerUser(3);
        Collection<String> users = model.getRegisteredUsers();
        users.add("User4");
        users.remove("User0");
        assertFalse((model.getRegisteredUsers()).contains("User4"));
        assertTrue((model.getRegisteredUsers()).contains("User0"));
    }
    
    @Test
    public void testChannelsEncapsulation() {
        model.registerUser(1);
        model.registerUser(2);
        model.registerUser(3);
        model.addChannel(0, "Channel0", false);
        model.addChannel(2, "Channel2", false);
        Collection<String> channels = model.getChannels();
        channels.add("Channel4");
        channels.remove("Channel0");
        assertFalse((model.getChannels()).contains("Channel4"));
        assertTrue((model.getChannels()).contains("Channel0"));
    }
    
    @Test
    public void testJoinAndLeaveChannel() {
        model.registerUser(0);
        model.registerUser(1);
        model.registerUser(2);
        Command create = new CreateCommand(0, "User0", "Channel0", false);
        create.updateServerModel(model);
        Command join1 = new JoinCommand(1, "User1", "Channel0");
        join1.updateServerModel(model);
        Command join2 = new JoinCommand(2, "User2", "Channel8");
        Broadcast actual2 = join2.updateServerModel(model);
        Broadcast expected2 = Broadcast.error(join2, ServerResponse.NO_SUCH_CHANNEL);
        assertEquals(actual2, expected2);
        Collection<String> channels = model.getUsersInChannel("Channel0");
        assertEquals(2, channels.size());
        Command leave3 = new LeaveCommand(1, "User1", "Channel0");
        leave3.updateServerModel(model);
        assertFalse((model.getUsersInChannel("Channel0")).contains("User1"));
        Command leave2 = new LeaveCommand(2, "User2", "Channel0");
        Broadcast actualLeave = leave2.updateServerModel(model);
        Broadcast expectedLeave = Broadcast.error(join2, ServerResponse.USER_NOT_IN_CHANNEL);
        assertEquals(actualLeave, expectedLeave);
    }
    
    @Test
    public void testGetUsersInChannelEncapsulation() {
        model.registerUser(0);
        model.registerUser(1);
        model.registerUser(2);
        Command create = new CreateCommand(0, "User0", "Channel0", false);
        create.updateServerModel(model);
        Command join1 = new JoinCommand(1, "User1", "Channel0");
        join1.updateServerModel(model);
        Command join2 = new JoinCommand(2, "User2", "Channel0");
        join2.updateServerModel(model);
        @SuppressWarnings("unused")
        Collection<String> channels = model.getUsersInChannel("Channel0");
        channels = null;
        assertEquals(3, model.getUsersInChannel("Channel0").size());
        assertTrue((model.getUsersInChannel("Channel0")).contains("User1"));
    }
    
    @Test
    public void testGetOwner() {
        model.registerUser(0);
        model.registerUser(1);
        model.registerUser(2);
        Command create = new CreateCommand(0, "User0", "Channel0", false);
        create.updateServerModel(model);
        String owner = model.getOwner("Channel0");
        assertEquals("User0", owner);
    }
    
    @Test
    public void testGetOwnerNull() {
        model.registerUser(0);
        model.registerUser(1);
        model.registerUser(2);
        Command create = new CreateCommand(0, "User0", "Channel0", false);
        create.updateServerModel(model);
        String owner = model.getOwner("Channel6");
        assertNull(owner);
    }
    
    @Test
    public void testMesgChannelDoesNotExistMember() {
        model.registerUser(0);
        model.registerUser(1);
        Command create = new CreateCommand(0, "User0", "Channel0", false);
        create.updateServerModel(model);
        Command join = new JoinCommand(1, "User1", "Channel0");
        join.updateServerModel(model);
        Command mesg = new MessageCommand(4, "User4", "Channel0", "hey whats up hello");
        Set<String> recipients = new TreeSet<>();
        recipients.add("User1");
        recipients.add("User0");
        Broadcast expected = Broadcast.error(mesg, ServerResponse.USER_NOT_IN_CHANNEL);
        assertEquals(expected, mesg.updateServerModel(model));
    }
    
    @Test
    public void testKickErrors() {
        model.registerUser(0);
        model.registerUser(1);
        model.registerUser(2);
        Command create = new CreateCommand(0, "User0", "java", false);
        create.updateServerModel(model);
        
        Command invite = new InviteCommand(0, "User0", "java", "User1");
        invite.updateServerModel(model);

        Command kick1 = new KickCommand(0, "User0", "java", "User20");
        Broadcast expected1 = Broadcast.error(kick1, ServerResponse.NO_SUCH_USER);
        Broadcast actual1 = kick1.updateServerModel(model);
        assertEquals(expected1, actual1);
        
        Command kick2 = new KickCommand(1, "User1", "java", "User0");
        Broadcast expected2 = Broadcast.error(kick2, ServerResponse.USER_NOT_OWNER);
        assertEquals(expected2, kick2.updateServerModel(model));
        
        Command kick3 = new KickCommand(0, "User0", "java323", "User2");
        Broadcast expected3 = Broadcast.error(kick3, ServerResponse.NO_SUCH_CHANNEL);
        assertEquals(expected3, kick3.updateServerModel(model));
        
        Command kick4 = new KickCommand(0, "User0", "java", "User2");
        Broadcast expected4 = Broadcast.error(kick4, ServerResponse.USER_NOT_IN_CHANNEL);
        assertEquals(expected4, kick4.updateServerModel(model)); 
    }
}
