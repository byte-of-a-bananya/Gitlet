package gitlet;
import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class Commit implements Serializable {
    /** message of commit. **/
    private final String message;
    /** date string. **/
    private final String date;
    /** hashvalue. **/
    private final String hash;
    /** name of the first parent commit. **/
    private final String parent;
    /** name of the first branch. **/
    private final String branch;
    /** name of the second parent commit. **/
    private final String secondParent;
    /** hashmap of tracked files. **/
    private final HashMap<String, String> files;

    @SuppressWarnings("unchecked")
    public Commit(String m, HashMap<String, String> fs, String p, String b) {
        message = m;
        parent = p;
        HashMap<String, String> hashHolder = Utils.readObject(new
                        File(".gitlet/commit/"
                        + p + ".txt"),
                Commit.class).getHashMap();
        files = (HashMap<String, String>)
                Utils.readObject(new File(".gitlet/commit/"
                                + p + ".txt"),
                Commit.class).getHashMap().clone();
        files.putAll(fs);
        HashMap<String, String> removedFiles =
                (HashMap<String, String>) Utils.readObject(
                    new File(".gitlet/stage/remhash.txt"), HashMap.class);
        for (String key : removedFiles.keySet()) {
            files.remove(key);
        }
        SimpleDateFormat dateFormat = new
                SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy Z");
        date = dateFormat.format(new Date());
        hash = makeHash();
        branch = b;
        secondParent = null;
    }

    @SuppressWarnings("unchecked")
    public Commit(String m, HashMap<String,
            String> fs, String p, String b, String sp) {
        message = m;
        parent = p;
        files = (HashMap<String, String>)
                Utils.readObject(new File(".gitlet/commit/"
                                + p + ".txt"),
                Commit.class).getHashMap().clone();
        files.putAll(fs);
        HashMap<String, String> removedFiles =
                (HashMap<String, String>) Utils.readObject(
                    new File(".gitlet/stage/remhash.txt"),
                        HashMap.class);
        for (String key : removedFiles.keySet()) {
            files.remove(key);
        }
        secondParent = sp;
        SimpleDateFormat dateFormat = new
                SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy Z");
        date = dateFormat.format(new Date());
        hash = makeHash();
        branch = b;
    }

    public Commit() {
        message = "initial commit";
        parent = null;
        files = new HashMap<String, String>();
        SimpleDateFormat dateFormat = new
                SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy Z");
        date = dateFormat.format(new Date(0));
        hash = makeHash();
        branch = "master";
        secondParent = null;
    }

    public String getDate() {
        return date;
    }

    public String getMessage() {
        return message;
    }

    public String makeHash() {
        return Utils.sha1(message + date + parent);
    }

    public HashMap<String, String> getHashMap() {
        return files;
    }

    public String getHash() {
        return hash.substring(0, 6);
    }

    public String getLongHash() {
        return hash;
    }

    public String getParent() {
        return parent;
    }

    public String getBranch() {
        return branch;
    }

    public boolean secP() {
        return secondParent != (null);
    }

    public String getSecP() {
        return secondParent;
    }

}
