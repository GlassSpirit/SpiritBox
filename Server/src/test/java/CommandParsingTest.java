import glassspirit.box.command.Command;
import glassspirit.box.server.ServerCommandHandler;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CommandParsingTest {

    @Before
    public void init() {
        ServerCommandHandler commandHandler = new ServerCommandHandler();
    }

    @Test
    public void parseOneStringArg() {
        Command command = new Command("/login Simple");
        Assert.assertEquals("Simple", command.getContext().get("username"));
    }

    @Test
    public void parseTwoStringArgs() {
        Command command = new Command("lOgiN us\"er \'pa ss word\'");
        Assert.assertEquals("us\"er", command.getContext().get("username"));
        Assert.assertEquals("pa ss word", command.getContext().get("password"));
    }

    @Test
    public void parseRemainingStringArg() {
        Command command = new Command("/msg How are \"you\"?");
        Assert.assertEquals("How are \"you\"?", command.getContext().get("message"));
    }

}
