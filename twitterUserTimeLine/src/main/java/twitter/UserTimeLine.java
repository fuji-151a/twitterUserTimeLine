package twitter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Extract UserTimeLine and UserTweet.
 * @author yuya
 *
 */
class UserTimeLine {

    /** propertiesファイルを読み込むためのインスタンス. */
    private Properties prop = new Properties();
    /** consumerKey. */
    private static String consumerKey;
    /** cosumerSecret. */
    private static String consumerSecret;
    /** accessToken 初期値. */
    private static String accessToken = null;
    /** accessTokenSecret 初期値. */
    private static String accessTokenSecret = null;
    /** setUp status code. */
    private static final int RATE_LIMITED_STATUS_CODE = 400;
    /** OSごとの改行コード. */
    private static final String LINECODE = System.getProperty("line.separator");
    /** twitter. */
    private static Twitter tw = TwitterFactory.getSingleton();

    /**
     * SetUp API Key.
     * @param fileName PropertiesFile
     * @throws IOException
     */
    UserTimeLine(final String fileName) throws IOException {
        prop.load(new FileInputStream(fileName));
        consumerKey = prop.getProperty("oauth.consumerKey");
        consumerSecret = prop.getProperty("oauth.consumerSecret");
        accessToken = prop.getProperty("oauth.accessToken");
        accessTokenSecret = prop.getProperty("oauth.accessTokenSecret");
    }

    /**
     * Extract UserTime.
     * @throws TwitterException
     */
    public void getUserTimeLine() throws TwitterException {
        Twitter twitter = TwitterFactory.getSingleton();
        List<Status> statuses = twitter.getHomeTimeline();
        System.out.println("showing home time line.");
        for (Status status : statuses) {
            System.out.println(
                    status.getUser().getName()
                    + ":" + status.getText());
        }
    }

    /**
     *  ツイートに改行が含まれていた場合は半角空白文字に置き換える.
     * @param s : tweet
     * @return Replace String
     */
    private static String normalizeText(String s) {
          // to space
          s = s.replaceAll("\r\n", "\n");
          s = s.replaceAll("\n", " ");
          return s;
    }

    /**
     * Extract User Tweet.
     * @param userName : ユーザアカウント
     * @param countMax : 取得する件数
     * @throws IOException : FileWrite
     */
    public void extractMyTweet(
            final String userName,
            final int countMax
            ) throws IOException {
        int page = 1;
        ResponseList<Status> tl = null;
        int total = 0;
        Paging paging = new Paging(page++, countMax);
        StringBuffer sb = new StringBuffer();

        while (true) {
            try {
                tl = tw.getUserTimeline(userName, paging);
                total = tl.size();
                for (int i = 0; i < tl.size(); i++) {
                    Status s = tl.get(i);
                    // ツイートの内容
                    String text = normalizeText(s.getText());
                    // 投稿日時
                    Date created = s.getCreatedAt();
                    // 出力
                    sb.append(text);
                    sb.append("\t");
                    sb.append(created);
                    sb.append(LINECODE);
                }
            } catch (TwitterException e) {
                e.printStackTrace();
                // APIのキャパオーバーじゃなければ続行
                if (RATE_LIMITED_STATUS_CODE != e.getStatusCode()) {
                    continue;
                }
                e.printStackTrace();
                break;
            }
            // 全部取得出来たら終了
            if (tl.size() == total) {
                break;
            }
        }
        String fileName = userName + countMax + ".tsv";
        tweetWriter(fileName, sb.toString());
    }

    /**
     * Main.
     * @param args
     * args[0] : PropertiesFile
     * args[1] : account
     * args[2] : Extract TweetNum
     * @throws IOException : FileIOException.
     */
    public static void main(final String[] args) throws IOException {
        if (args.length != 3) {
            System.out.println("Please input format: "
                    + "java -cp UserTimeLine.jar twitter.twitterUserTimeLine "
                    + "PropertiesFile UserName ExtractTweetNumber");
        }
        String fileName = args[0];
        String userName = args[1];
        int countMax = Integer.valueOf(args[2]);

        UserTimeLine userTL = new UserTimeLine(fileName);
        ConfigurationBuilder builder = new ConfigurationBuilder();
        // SetUp AccessToken
        builder.setOAuthConsumerKey(consumerKey);
        builder.setOAuthAccessTokenSecret(consumerSecret);
        builder.setOAuthAccessToken(accessToken);
        builder.setOAuthAccessTokenSecret(accessTokenSecret);

        userTL.extractMyTweet(userName, countMax);
    }

    private void tweetWriter(String fileName, String data) throws IOException {
        File file = new File(fileName);
        FileWriter fw = new FileWriter(file);
        BufferedWriter bw = new BufferedWriter(fw);
        try {
            bw.write(data);
            bw.close();
        } catch (IOException e) {
            System.out.println(e);
        } finally {
            bw.close();
        }
    }
}
