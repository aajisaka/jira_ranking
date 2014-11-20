import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parse Apache Hadoop commit log for importing to database.
 * The input is supposed to be created by "git log --numstat" or
 * "git log --numstat -C" (ignore moved codes).
 * The main method outputs issue_no, project_name, suffix,
 * inserted_lines, deleted_lines, and commit_date in csv format.
 * Inserted and deleted lines are grouped by the suffix of the committed
 * files. The format of the commit date is "yyyy-MM-dd HH:mm:ss".
 */
public class GitParse {

  private static final SimpleDateFormat INPUT_FORMAT =
      new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.ENGLISH);
  private static final SimpleDateFormat OUTPUT_FORMAT =
      new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  private static final Set<PrimaryKey> keySet = new HashSet<>(); 

  public static void main(String[] args) {
    if (args.length != 2) {
      System.err.println("Usage: <in> <out>");
      System.exit(1);
    }

    File input = new File(args[0]);
    File output = new File(args[1]);

    // for counting inserted lines and deleted lines
    Map<String, Integer> insertedMap = new HashMap<>();
    Map<String, Integer> deletedMap = new HashMap<>();

    // output
    StringBuilder out = new StringBuilder();

    try {
      BufferedReader reader = new BufferedReader(new FileReader(input));
      String dateStr = "";
      String project = "";
      int issueNo = -1;

      while (true) {
        String line = reader.readLine();
        if (line == null) break;

        if (line.startsWith("Date:")) {
          String previousDateStr = dateStr;
          dateStr = getOutputDate(line);
          // if the commit date is the same as the previous commit,
          // continue to count the inserted/deleted lines and skip output
          if (!dateStr.equals(previousDateStr)) {
            addOutput(out, issueNo, project, insertedMap, deletedMap,
                previousDateStr);
            issueNo = -1;
          }
        }

        // parse the commit message
        if (line.startsWith("HDFS", 4)) {
          project = "HDFS";
          issueNo = getIssueNo(line);
        } else if (line.startsWith("MAPREDUCE", 4)) {
          project = "MAPREDUCE";
          issueNo = getIssueNo(line);
        } else if (line.startsWith("HADOOP", 4)) {
          project = "HADOOP";
          issueNo = getIssueNo(line);
        } else if (line.startsWith("YARN", 4)) {
          project = "YARN";
          issueNo = getIssueNo(line);
        }

        // set inserted lines and deleted lines
        if (line.matches("^\\d.*")) {
          setNumLines(line, insertedMap, deletedMap);
        }
      }
      // add the last commit to the output
      addOutput(out, issueNo, project, insertedMap, deletedMap, dateStr);

      // output
      FileOutputStream fos = new FileOutputStream(output);
      fos.write(out.toString().getBytes(Charset.forName("UTF-8")));
    } catch (IOException | ParseException e) {
      e.printStackTrace();
    }
  }

  private static int getIssueNo(String line) {
    Pattern p = Pattern.compile("\\d+");
    Matcher m = p.matcher(line);
    if (m.find()) {
      return Integer.parseInt(line.substring(m.start(), m.end()));
    } else {
      System.err.println("IssueNo does not found: " + line);
      return -1;
    }
  }

  private static String getOutputDate(String line) throws ParseException {
    Date date = INPUT_FORMAT.parse(line.split("\\s+", 2)[1]);
    return OUTPUT_FORMAT.format(date);
  }

  /**
   * Set (suffix, number of inserted/deleted lines) to each Map.
   */
  private static void setNumLines(String line,
                                  Map<String, Integer> insertedMap,
                                  Map<String, Integer> deletedMap) {
    String[] numLines = line.split("\\s+");
    int inserted = Integer.parseInt(numLines[0]);
    int deleted = Integer.parseInt(numLines[1]);
    // re-use String[]
    numLines = line.split("\\.");
    String suffix;
    if (numLines.length > 1) {
      suffix = numLines[numLines.length - 1];
      // -C option sometimes adds '}' to the end of the line, so remove '}'.
      suffix = suffix.replaceAll("}", "");
    } else {
      // empty suffix (such as executable files)
      suffix = "null";
    }

    if (insertedMap.containsKey(suffix)) {
      inserted += insertedMap.get(suffix);
    }
    insertedMap.put(suffix, inserted);
    if (deletedMap.containsKey(suffix)) {
      deleted += deletedMap.get(suffix);
    }
    deletedMap.put(suffix, deleted);
  }

  private static void addOutput(StringBuilder out, int issueNo, String project,
                                Map<String, Integer> insertedMap,
                                Map<String, Integer> deletedMap,
                                String dateStr) {
    // avoid duplicate primary key
    PrimaryKey primaryKey = new PrimaryKey(issueNo, project, dateStr);
    if (issueNo > 0 && !keySet.contains(primaryKey)) {
      for (String key : insertedMap.keySet()) {
        out.append(issueNo).append(",")
            .append(project).append(",")
            .append(key).append(",")
            .append(insertedMap.get(key)).append(",")
            .append(deletedMap.get(key)).append(",")
            .append(dateStr).append('\n');
      }
      keySet.add(primaryKey);
    }
    insertedMap.clear();
    deletedMap.clear();
  }
}
