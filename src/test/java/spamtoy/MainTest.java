package spamtoy;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

public class MainTest {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private static String[] spams;

    @Before
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
    }

    @After
    public void restoreStreams() {
        System.setOut(originalOut);
    }

    /**
     * Randomly pick `count` different natural numbers smaller than `bound`
     * @param count: count of numbers to pick
     * @param bound: pick from numbers smaller than this
     * @return array of picked numbers
     */
    private static int[] getIndicesRandomly(int count, int bound) {
        assert(count <= bound);
        ArrayList<Integer> numbers = IntStream.range(0, bound).boxed().collect(Collectors.toCollection(ArrayList::new));

        int[] picked = new int[count];
        Random rand = new Random();
        for (int i = 0; i < count; i++) {
            int index = rand.nextInt(numbers.size());
            picked[i] = numbers.get(index);
            numbers.remove(index);
        }
        return picked;
    }

    public record Spam(String template, String recipient, String sender, String amount) {
        public String toString() {
            return String.format(template, recipient, sender, amount);
        }
    }

    /**
     * Generate 50 spam texts with 3 templates in `spamTemplates`, 30 with the 1st, 10 the 2nd, and 10 the 3rd.
     * Texts generated from the same template differ by recipient name, sender name, and an amount of money.
     * Recipient names and sender names are picked randomly from recipients.txt and senders.txt, respectively.
     * Amounts of money are generated randomly.
     */
    @BeforeClass
    public static void setUpSpams() {
        final String[] spamTemplates = new String[]{
            """
            Dear %1$s This is to inform you that after the meeting
            with The Federal Government Board Of Directors, we have concluded
            that you should be paid %3$s to make up for your losses in
            the past and also retrieve the good image of our federation that
            has been tarnished by hacker and scammers.Therefore you are
            hereby advised to get back to us without delay. Thanks Mr %2$s""",
            """
            Dear %1$s,
            
            My name is Mr. %2$s II bank of Africa manager in Benin Republic,
            The United Nations instruct us to contact you via your email and make
            sure that your ATM VISA CARD worth %3$s is delivered to you
            through DHL, we wish to inform you that your ATM card will expire in a
            few day's time if you do not use your pin before Next month, contact us
            with your mailing address and telephone number immediately of your ATM
            CARD,
            
            Thanks for banking with us
            
            Sincerely
            %2$s""",
            """
            Hello %1$s,
            
            I'm %2$s, a business tycoon, investor, and
            philanthropist.the vice chairman, chief executive officer (CEO), and
            the single largest shareholder of Walgreens Boots Alliance. I gave
            away 25 percent of my personal wealth to charity. And I also pledged
            to give away the rest of 25%% this year 2023 to Individuals.. I have
            decided to donate %3$s to you. If you are interested in my donation, do contact me for
            more info.
            
            Warm Regard
            CEO Walgreens Boots Alliance
            %2$s"""
        };
        String recipientsFile = "recipients.txt", sendersFile = "senders.txt";
        ArrayList<String> recipients = new ArrayList<>(), senders = new ArrayList<>();
        ClassLoader clsLoader = MainTest.class.getClassLoader();
        String line;

        try (InputStream inStream = clsLoader.getResourceAsStream(recipientsFile);
             InputStreamReader streamReader = new InputStreamReader(inStream);
             BufferedReader reader = new BufferedReader(streamReader)) {
            while ((line = reader.readLine()) != null)
                recipients.add(line);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (InputStream inStream = clsLoader.getResourceAsStream(sendersFile);
             InputStreamReader streamReader = new InputStreamReader(inStream);
             BufferedReader reader = new BufferedReader(streamReader)) {
            while ((line = reader.readLine()) != null)
                senders.add(line);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        int spamCount = 50;
        spams = new String[spamCount];
        int[] spamRecipientIndices = getIndicesRandomly(spamCount, recipients.size());
        int[] spamSenderIndices = getIndicesRandomly(spamCount, senders.size());
        Random rand = new Random();
        for (int i = 0; i < 50; i++) {
            String template = i < 30? spamTemplates[0] : i < 40? spamTemplates[1] : spamTemplates[2],
                    recipient = recipients.get(spamRecipientIndices[i]),
                    sender = senders.get(spamSenderIndices[i]),
                    amount = String.format("$%s,000,000", rand.nextInt(10, 50));
            Spam s = new Spam(template, recipient, sender, amount);
            spams[i] = s.toString();
        }
    }

    @Test
    public void testOut() {
        Main.main(new String[]{""});
        assertEquals("Hello world!\n", outContent.toString());
    }
}
