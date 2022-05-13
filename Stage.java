package gitlet;

import java.io.Serializable;
import java.util.HashMap;
import java.io.File;

public class Stage implements Serializable {
    /** Serialization of add stage. **/
    private final File saveHashAdd = new File(".gitlet/stage/addhash.txt");
    /** Serialization of remove stage. **/
    private final File saveHashRem = new File(".gitlet/stage/remhash.txt");

    public Stage() {
        Utils.writeObject(saveHashAdd, new HashMap<String, String>());
        Utils.writeObject(saveHashRem, new HashMap<String, String>());
    }
    @SuppressWarnings("unchecked")
    public void add(String f) {
        File file = new File(f);
        Blob blob = new Blob(file);
        String newBlobSha = blob.getSha();
        HashMap<String, String> remfiles =
                Utils.readObject(saveHashRem, HashMap.class);
        HashMap<String, String> files =
                Utils.readObject(saveHashAdd, HashMap.class);
        Commit com = Utils.readObject(new
                        File(".gitlet/stage/head.txt"),
                Commit.class);
        if (file.exists()) {
            if (remfiles.containsKey(f)) {
                if (!newBlobSha.equals(remfiles.get(f))) {
                    files.put(f, newBlobSha);
                    File blobFileWrite = new
                            File(".gitlet/blob/" + newBlobSha + ".txt");
                    Utils.writeObject(blobFileWrite, blob);
                } else {
                    files.remove(f);
                }
                remfiles.remove(f);
            } else if (!newBlobSha.equals(Utils.readObject(
                    new File(".gitlet/stage/head.txt"),
                    Commit.class).getHashMap().get(f))) {
                if (files.containsKey(f)) {
                    if (!newBlobSha.equals(files.get(f))) {
                        files.put(f, newBlobSha);
                        File blobFileWrite = new
                                File(".gitlet/blob/" + newBlobSha + ".txt");
                        Utils.writeObject(blobFileWrite, blob);
                    }
                } else {
                    files.put(f, blob.getSha());
                    File blobFileWrite = new
                            File(".gitlet/blob/" + newBlobSha + ".txt");
                    Utils.writeObject(blobFileWrite, blob);
                }
            } else {
                files.remove(f);
            }
        } else {
            System.out.println("File does not exist.");
            System.exit(0);
        }
        Utils.writeObject(saveHashRem, remfiles);
        Utils.writeObject(saveHashAdd, files);
    }

    @SuppressWarnings("unchecked")
    public boolean remove(String file) {
        HashMap<String, String> files =
                Utils.readObject(saveHashAdd, HashMap.class);
        if (files.containsKey(file)) {
            files.remove(file);
            Utils.writeObject(saveHashAdd, files);
            return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public void stageForRemoval(String file, String hash) {
        HashMap<String, String> remfiles =
                Utils.readObject(saveHashRem, HashMap.class);
        remfiles.put(file, hash);
        Utils.restrictedDelete(file);
        Utils.writeObject(saveHashRem, remfiles);
    }

    public void clear() {
        Utils.writeObject(saveHashAdd, new HashMap<String, String>());
        Utils.writeObject(saveHashRem, new HashMap<String, String>());
    }
}
