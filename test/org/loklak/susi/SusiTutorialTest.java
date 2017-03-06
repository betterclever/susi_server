package org.loklak.susi;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Map;

import org.eclipse.jetty.util.log.Log;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.loklak.SusiServer;
import org.loklak.data.DAO;
import org.loklak.server.ClientIdentity;

public class SusiTutorialTest {

    private final BufferedReader getTestReader() {
        ByteArrayInputStream bais = new ByteArrayInputStream(testFile.getBytes(StandardCharsets.UTF_8));
        return new BufferedReader(new InputStreamReader(bais, StandardCharsets.UTF_8));
    }
    
    public static String susiAnswer(String q, ClientIdentity identity) throws JSONException {
        SusiInteraction interaction = new SusiInteraction(DAO.susi, q, 0, 0, 0, 1, identity);
        JSONObject json = interaction.getJSON();
        DAO.susi.getLogs().addInteraction(identity.getClient(), interaction);
        String answer = json.getJSONArray("answers")
                .getJSONObject(0)
                .getJSONArray("actions")
                .getJSONObject(0)
                .getString("expression");
        return answer;
    }
    
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void test() {
        try {
            System.setProperty("java.awt.headless", "true"); // no awt used here so we can switch off that stuff
            Path data = FileSystems.getDefault().getPath("data");
            Map<String, String> config = SusiServer.readConfig(data);
            
            // initialize all data        
            try{
                DAO.init(config, data);
                BufferedReader br = getTestReader();
                JSONObject lesson = DAO.susi.readEzDLesson(br);
                DAO.susi.learn(lesson);
                br.close();
            } catch(Exception e){
                e.printStackTrace();
                Log.getLog().warn(e.getMessage());
                Log.getLog().warn("Could not initialize DAO. Exiting.");
                System.exit(-1);
            }
            
            ClientIdentity identity = new ClientIdentity("host:localhost");
            test("roses are red", "susi is a hack", identity);
            test("susi is a hack", "skynet is back", identity);
            assertTrue("Potatoes|Vegetables|Fish".indexOf(susiAnswer("What is your favorite dish", identity)) >= 0);
            test("Bonjour", "Hello", identity);
            test("Buenos días", "Hello", identity);
            test("Ciao", "Hello", identity);
            test("May I work for you?", "Yes you may", identity);
            test("May I get a beer?", "Yes you may get a beer!", identity);
            test("For one dollar I can buy a beer", "Yeah, I believe one dollar is a god price for a beer", identity);
            test("Someday I buy a car", "Sure, you should buy a car!", identity);
            //test("I really like bitburger beer", "You then should have one bitburger!", identity);
            test("What beer is the best?", "I bet you like bitburger beer!", identity);
            
        } catch (Exception e) {
            e.printStackTrace();
        }        
    }
    
    private static void test(String q, String e, ClientIdentity i) {
        try {
            String a = susiAnswer(q, i);
            boolean r = a.equals(e);
            if (!r) {
                DAO.log("** fail for: " + q);
                DAO.log("** expected: " + e);
                DAO.log("** returned: " + a);
            }
            assertTrue(r);
        } catch (JSONException x) {
            x.printStackTrace();
            assertTrue(false);
        }
    }

    private final static String testFile = 
                    "# susi EzD tutorial playground\n" +
                    "::prior\n" +
                    "roses are red\n" +
                    "susi is a hack\n" +
                    "skynet is back\n" +
                    "\n" +
                    "What is your favorite dish\n" +
                    "Potatoes|Vegetables|Fish\n" +
                    "\n" +
                    "Bonjour|Buenos días|Ciao\n" +
                    "Hello\n" +
                    "\n" +
                    "May I * you\n" +
                    "Yes you may\n" +
                    "\n" +
                    "May I get a *?\n" +
                    "Yes you may get a $1$!\n" +
                    "\n" +
                    "For * I can buy a *\n" +
                    "Yeah, I believe $1$ is a god price for a $2$\n" +
                    "\n" +
                    "* buy a *\n" +
                    "Sure, you should buy a $2$!\n" +
                    "\n" +
                    "I * like * beer\n" +
                    "You then should have one $2$>_beerbrand!\n" +
                    "\n" +
                    "* beer * best?\n" +
                    "I bet you like $_beerbrand$ beer!\n" +
                    "\n";
}


