package twitter;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

/**
 * UserTimeLineTest.
 * @author yuya
 *
 */
public class UserTimeLineTest {

    /** testResorceへのファイルパス. */
    private String fileName = UserTimeLineTest.class
            .getClassLoader().getResource("twitter4j.properties").getPath();
    /** UserTimeLine Instance. */
    private static UserTimeLine ut;
    /** TestUserName. */
    private String userName = "twitter";
    /** extract TweetNum. */
    private int countMax = 200;
    /** outputFileName. */
    private String outputFileName = userName + countMax + ".tsv";

    /**
     * プロパティ及びAPIセットアップ.
     * @throws IOException : FileIOException
     */
    @Before
    public final void setUp() throws IOException {
        ut = new UserTimeLine(fileName);
        File file = new File(outputFileName);
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * 指定した数のツイートが抽出出来ているかテスト.
     * @throws IOException : FIleIOException.
     */
    @Test
    public final void testExtractMyTweetNum() throws IOException {
        int i = 0;
        ut.extractMyTweet(userName, countMax);
        try {
            FileReader in = new FileReader(outputFileName);
            BufferedReader br = new BufferedReader(in);
            String line;
            while ((line = br.readLine()) != null) {
                i++;
            }
            in.close();
        } catch (IOException e) {
            System.out.println(e);
        }
        assertThat(i, is(countMax));
    }

}
