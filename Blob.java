package gitlet;

import java.io.File;
import java.io.Serializable;


/**
 * @author Ananya Bahugudumbi
 **/

public class Blob implements Serializable {
    /** Holds the blob's file. **/
    private final File f;
    /** Holds the file's text. **/
    private String text;
    /** Holds the blob's shavalue. **/
    private final String shaValue;

    public Blob(File c) {
        f = c;
        if (f.exists()) {
            text = Utils.readContentsAsString(this.f);
        } else {
            System.out.println("File does not exist.");
            System.exit(0);
        }
        shaValue = Utils.sha1(text);
    }

    public boolean compare(Blob b) {
        return b.getText().equals(getText());
    }

    public String getText() {
        return text;
    }

    public File getFile() {
        return f;
    }

    public String getSha() {
        return shaValue;
    }
}
