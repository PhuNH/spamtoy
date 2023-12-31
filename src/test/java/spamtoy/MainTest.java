package spamtoy;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.Assert.*;

public class MainTest {
    private static final int MONEY_FROM = 10, MONEY_BOUND = 50,
            HAM_WORDS_FROM = 30, HAM_WORDS_BOUND = 151;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private static ArrayList<String> recipients, senders, words;

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

    private static InputStream getInputStream(String file) {
        ClassLoader clsLoader = MainTest.class.getClassLoader();
        InputStream inStream = clsLoader.getResourceAsStream(file);

        if (inStream == null) {
            throw new IllegalArgumentException(file + " not found");
        }
        return inStream;
    }

    private static ArrayList<String> readLines(InputStream inStream) {
        String line;
        ArrayList<String> lines = new ArrayList<>();
        try (InputStreamReader streamReader = new InputStreamReader(inStream);
             BufferedReader reader = new BufferedReader(streamReader)) {
            while ((line = reader.readLine()) != null)
                lines.add(line);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return lines;
    }

    @Before
    public void readData() {
        recipients = readLines(getInputStream("recipients.txt"));
        senders = readLines(getInputStream("senders.txt"));
        words = readLines(getInputStream("words.txt"));
    }

    /**
     * Generate `spamCount` spam texts with 3 templates in `spamTemplates`
     * - 60% with the 1st, 20% the 2nd, the rest the 3rd.
     * Texts generated from the same template differ by recipient name, sender name, and an amount of money.
     * Recipient names and sender names are picked randomly from recipients.txt and senders.txt, respectively.
     * Amounts of money are generated randomly.
     */
    private static String[] setUpSpams(int spamCount) {
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

        String[] spams = new String[spamCount];
        int[] spamRecipientIndices = getIndicesRandomly(spamCount, recipients.size());
        int[] spamSenderIndices = getIndicesRandomly(spamCount, senders.size());
        Random rand = new Random();
        int secondCount = spamCount / 5, firstCount = secondCount * 3;
        for (int i = 0; i < spamCount; i++) {
            String template = i < firstCount? spamTemplates[0] :
                    i < (firstCount + secondCount)? spamTemplates[1] : spamTemplates[2],
                    recipient = recipients.get(spamRecipientIndices[i]),
                    sender = senders.get(spamSenderIndices[i]),
                    amount = String.format("$%s,000,000", rand.nextInt(MONEY_FROM, MONEY_BOUND));
            Spam s = new Spam(template, recipient, sender, amount);
            spams[i] = s.toString();
        }

        return spams;
    }

    /**
     * Generate `hamCount` ham texts, for each of which, length is picked randomly between 30 and 150 words;
     * words are picked randomly from words.txt.
     */
    private static String[] setUpHams(int hamCount) {
        String[] hams = new String[hamCount];
        Random rand = new Random();
        for (int i = 0; i < hamCount; i++) {
            int length = rand.nextInt(HAM_WORDS_FROM, HAM_WORDS_BOUND);
            String[] pickedWords = new String[length];
            for (int j = 0; j < length; j++)
                pickedWords[j] = words.get(rand.nextInt(words.size()));
            hams[i] = String.join(" ", pickedWords);
        }
        return hams;
    }

    @Test
    public void testHalves() {
        String[] spams = setUpSpams(500), hams = setUpHams(500);
        String[] texts = Stream.concat(Arrays.stream(spams), Arrays.stream(hams)).toArray(String[]::new);
        Batch batch = new Batch(texts);
        // Groups: 300, 100, 100. Use 0.25 to be sure
        assertTrue(batch.prs[0] > 0.25);
        assertEquals(0, batch.prs[500], 0.0);
    }

    @Test
    public void testMoreSpams() {
        String[] spams = setUpSpams(800), hams = setUpHams(200);
        String[] texts = Stream.concat(Arrays.stream(spams), Arrays.stream(hams)).toArray(String[]::new);
        Batch batch = new Batch(texts);
        // Groups: 480, 160, 160. Use 0.4 to be sure
        assertTrue(batch.prs[0] > 0.4);
        assertTrue(batch.prs[480] > 0.1);
        assertEquals(0, batch.prs[800], 0.0);
    }

    @Test
    public void testFewerSpams() {
        String[] spams = setUpSpams(100), hams = setUpHams(900);
        String[] texts = Stream.concat(Arrays.stream(spams), Arrays.stream(hams)).toArray(String[]::new);
        Batch batch = new Batch(texts);
        // Groups: 60, 20, 20. Use 0.01 to be sure
        assertTrue(batch.prs[0] > 0.01);
        assertTrue(batch.prs[80] > 0.01);
        assertEquals(0, batch.prs[100], 0.0);
    }
}
