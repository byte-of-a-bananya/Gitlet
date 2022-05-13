package gitlet;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.TreeMap;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.HashSet;


public class Repository implements Serializable {
    /** Serialization head commit. **/
    private final String head = ".gitlet/stage/head.txt";
    /** Serialization of current branch. **/
    private final String nowBranch = ".gitlet/stage/nowbranch.txt";
    /** Serialization of branch hashmap. **/
    private final String branchesAndHeads = ".gitlet/stage/branches."
            + "txt";
    /** Serialization of stage. **/
    private final String stage = ".gitlet/stage/stage.txt";
    /** Serialization of init status. **/
    private final String initialized = ".gitlet/stage/init.txt";
    /** Serialization of commit hash. **/
    private final String commits = ".gitlet/stage/commits.txt";

    public Repository() {
    }

    public void init() throws IOException {
        if (!(new File(initialized)).exists()) {
            File directory = new File(".gitlet");
            File commitDir = new File(".gitlet/commit");
            File blobDir = new File(".gitlet/blob");
            File stageDir = new File(".gitlet/stage");
            directory.mkdir();
            commitDir.mkdir();
            blobDir.mkdir();
            stageDir.mkdir();
            Utils.writeObject(new File(initialized), "true");
            Utils.writeObject(new File(stage), new Stage());
            Commit initCommit = new Commit();
            String initCommitHash = initCommit.getHash();
            File initCommitFile = new File(".gitlet/commit/"
                    + initCommitHash + ".txt");
            Utils.writeObject(new File(nowBranch), "master");
            Utils.writeObject(initCommitFile, initCommit);
            Utils.writeObject(new File(head), initCommit);
            TreeMap<String, String> branches = new TreeMap<String, String>();
            branches.put("master", initCommit.getHash());
            HashMap<String, String> commitsHash = new HashMap<String, String>();
            commitsHash.put(initCommitHash, "wootwoot");
            Utils.writeObject(new File(commits), commitsHash);
            Utils.writeObject(new File(branchesAndHeads), branches);

        } else {
            System.out.println("Gitlet version-control "
                    + "system already exists in "
                    + "the current directory.");
            System.exit(0);
        }
    }

    public void add(String file) {
        Stage st = Utils.readObject(new File(stage), Stage.class);
        st.add(file);
        Utils.writeObject(new File(stage), st);
    }

    @SuppressWarnings("unchecked")
    public void commit(String message) {
        if (message.equals("")) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }
        File getHashMap = new File(".gitlet/stage/addhash.txt");
        HashMap<String, String> stagedFiles = (HashMap<String, String>)
                Utils.readObject(getHashMap, HashMap.class);
        HashMap<String, String> removedFiles = Utils.readObject(
                new File(".gitlet/stage/remhash.txt"),
                HashMap.class);
        if (stagedFiles.isEmpty() && removedFiles.isEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        Commit parent = Utils.readObject(new File(head), Commit.class);
        Commit newCommit = new Commit(message, stagedFiles, parent.getHash(),
                Utils.readObject(new File(nowBranch), String.class));

        TreeMap<String, String> tree1 = Utils.readObject(new
                File(branchesAndHeads), TreeMap.class);
        tree1.replace(Utils.readObject(new File(nowBranch),
                String.class), newCommit.getHash());
        String newCommitHash = newCommit.getHash();
        File commitFileWrite = new File(".gitlet/commit/"
                + newCommitHash + ".txt");
        Utils.writeObject(commitFileWrite, newCommit);
        Stage st = Utils.readObject(new File(stage), Stage.class);
        st.clear();
        Utils.writeObject(new File(stage), st);
        Utils.writeObject(new File(head), newCommit);
        Utils.writeObject(new File(branchesAndHeads), tree1);
        HashMap<String, String> commitsHash = Utils.readObject(
                new File(commits), HashMap.class);
        commitsHash.put(newCommitHash, "wootwoot");
        Utils.writeObject(new File(commits), commitsHash);

    }

    public void log() {
        Commit currCommit = Utils.readObject(
                new File(head), Commit.class);
        while (currCommit.getParent() != null) {
            System.out.println("===");
            System.out.println("commit " + currCommit.getLongHash());
            System.out.println("Date: " + currCommit.getDate());
            System.out.println(currCommit.getMessage());
            System.out.println();
            currCommit = Utils.readObject(
                    new File(".gitlet/commit/"
                            + currCommit.getParent() + ".txt"), Commit.class);
        }
        System.out.println("===");
        System.out.println("commit " + currCommit.getLongHash());
        System.out.println("Date: " + currCommit.getDate());
        System.out.println(currCommit.getMessage());
        System.out.println();
    }

    public void checkout(String fileName) {
        Commit headCommit = Utils.readObject(new File(head),
                Commit.class);
        File outFile = new File(fileName);
        if (outFile.exists()) {
            String blobHash = headCommit.getHashMap().get(fileName);
            File blobFile = new File(".gitlet/blob/"
                    + blobHash + ".txt");
            Blob b = Utils.readObject(blobFile, Blob.class);
            Utils.writeContents(outFile, b.getText());
        } else {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
    }

    public void checkout(String commitID, String fileName) {
        commitID = commitID.substring(0, 6);
        if (!(new File(".gitlet/commit/" + commitID
                + ".txt")).exists()) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        Commit headCommit = Utils.readObject(new
                        File(".gitlet/commit/"
                        + commitID + ".txt"),
                Commit.class);
        File outFile = new File(fileName);
        if (outFile.exists()) {
            String blobHash = headCommit.getHashMap().get(fileName);
            File blobFile = new File(".gitlet/blob/"
                    + blobHash + ".txt");
            Blob b = Utils.readObject(blobFile, Blob.class);
            Utils.writeContents(outFile, b.getText());
        } else {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
    }

    @SuppressWarnings("unchecked")
    public void checkout3(String branchName) throws IOException {
        String currentBranch = Utils.readObject(new
                File(nowBranch), String.class);
        if (branchName.equals(currentBranch)) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }
        TreeMap<String, String> tree = Utils.readObject(new
                File(branchesAndHeads), TreeMap.class);
        if (!tree.containsKey(branchName)) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }
        Commit currHead = Utils.readObject(new
                File(".gitlet/commit/"
                + tree.get(currentBranch) + ".txt"), Commit.class);
        Commit newHead = Utils.readObject(new
                File(".gitlet/commit/"
                + tree.get(branchName) + ".txt"), Commit.class);
        HashMap<String, String> currHash = currHead.getHashMap();
        HashMap<String, String> newHash = newHead.getHashMap();
        List<String> cwdList =
                Utils.plainFilenamesIn(System.getProperty("user.dir"));
        for (String key : newHash.keySet()) {
            File outFile = new File(key);
            if (cwdList.contains(key)
                    && !currHash.containsKey(key)
                    && !(new Blob(outFile)).getSha().equals(newHash.get(key))) {
                System.out.println("There is an untracked file "
                        + "in the way; delete it, "
                        + "or add and commit it first.");
                System.exit(0);
            }
            outFile.createNewFile();
            String blobHash = newHash.get(key);
            File blobFile = new File(".gitlet/blob/"
                    + blobHash + ".txt");
            Blob b = Utils.readObject(blobFile, Blob.class);
            Utils.writeContents(outFile, b.getText());
        }
        for (String f : currHash.keySet()) {
            if (!newHash.containsKey(f)) {
                Utils.restrictedDelete(f);
            }
        }
        Stage st = Utils.readObject(new File(stage), Stage.class);
        st.clear();
        Utils.writeObject(new File(stage), st);
        Utils.writeObject(new File(nowBranch), branchName);
        Utils.writeObject(new File(head), newHead);
    }

    public void remove(String file) {
        Stage st = Utils.readObject(new File(stage), Stage.class);
        boolean staged = st.remove(file);
        Commit headCommit = Utils.readObject(new File(head), Commit.class);
        HashMap<String, String> hm = headCommit.getHashMap();
        boolean committed = false;
        if (hm.containsKey(file)) {
            st.stageForRemoval(file, hm.get(file));
            File f = new File(file);
            committed = true;
        }
        if (!staged && !committed) {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }
        Utils.writeObject(new File(stage), st);
    }

    @SuppressWarnings("unchecked")
    public void globalLog() {
        HashMap<String, String> commitsHash = Utils.readObject(
                new File(commits), HashMap.class);
        for (String key : commitsHash.keySet()) {
            Commit currCommit = Utils.readObject(new
                            File(".gitlet/commit/" + key + ".txt"),
                    Commit.class);
            System.out.println("===");
            System.out.println("commit " + currCommit.getLongHash());
            System.out.println("Date: " + currCommit.getDate());
            System.out.println(currCommit.getMessage());
            System.out.println();
        }
    }

    @SuppressWarnings("unchecked")
    public void branch(String branchName) {
        TreeMap<String, String> tree = Utils.readObject(new
                File(branchesAndHeads), TreeMap.class);
        if (tree.containsKey(branchName)) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
        tree.put(branchName, Utils.readObject(new File(head),
                Commit.class).getHash());
        Utils.writeObject(new File(branchesAndHeads), tree);
    }

    @SuppressWarnings("unchecked")
    public void find(String message) {
        ArrayList<String> list = new ArrayList<>();
        HashMap<String, String> commitsHash = Utils.readObject(new
                File(commits), HashMap.class);
        for (String key : commitsHash.keySet()) {
            Commit curr = Utils.readObject(new
                            File(".gitlet/commit/" + key + ".txt"),
                    Commit.class);
            if (curr.getMessage().equals(message)
                    && !list.contains(curr.getHash())) {
                list.add(curr.getLongHash());
            }
        }
        if (list.size() == 0) {
            System.out.println("Found no commit with that message.");
            System.exit(0);
        }
        for (int i = 0; i < list.size(); i++) {
            System.out.println(list.get(i));
        }
    }

    @SuppressWarnings("unchecked")
    public void rmBranch(String branch) {
        TreeMap<String, String> tree = Utils.readObject(new
                File(branchesAndHeads), TreeMap.class);
        if (!tree.containsKey(branch)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        if (branch.equals(Utils.readObject(new File(nowBranch),
                String.class))) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }
        tree.remove(branch);
        Utils.writeObject(new File(branchesAndHeads), tree);
    }

    @SuppressWarnings("unchecked")
    public void reset(String id) throws IOException {
        id = id.substring(0, 6);
        if ((new File(".gitlet/commit/"
                + id + ".txt")).exists()) {
            Commit newHead = Utils.readObject(new
                            File(".gitlet/commit/" + id + ".txt"),
                    Commit.class);
            Commit currHead = Utils.readObject(new
                    File(head), Commit.class);
            HashMap<String, String> currHash = currHead.getHashMap();
            HashMap<String, String> newHash = newHead.getHashMap();
            List<String> cwdList =
                    Utils.plainFilenamesIn(System.getProperty("user.dir"));
            for (String key : newHash.keySet()) {
                File outFile = new File(key);
                if (cwdList.contains(key) && !currHash.containsKey(key)
                        && !(new Blob(outFile)).
                        getSha().equals(newHash.get(key))) {
                    System.out.println("There is an untracked file "
                            + "in the way; delete it, "
                            + "or add and commit it first.");
                    System.exit(0);
                }
                outFile.createNewFile();
                String blobHash = newHash.get(key);
                File blobFile = new File(".gitlet/blob/"
                        + blobHash + ".txt");
                Blob b = Utils.readObject(blobFile,
                        Blob.class);
                Utils.writeContents(outFile, b.getText());
            }
            for (String f : currHash.keySet()) {
                if (!newHash.containsKey(f)) {
                    Utils.restrictedDelete(f);
                }
            }
            Stage st = Utils.readObject(new File(stage), Stage.class);
            st.clear();
            Utils.writeObject(new File(stage), st);
            Utils.writeObject(new File(nowBranch), newHead.getBranch());
            Utils.writeObject(new File(head), newHead);
            TreeMap<String, String> tree = Utils.readObject(new
                            File(branchesAndHeads),
                    TreeMap.class);
            tree.put(newHead.getBranch(), newHead.getHash());
            Utils.writeObject(new File(branchesAndHeads), tree);
        } else {
            System.out.println("No commit with that id exists.");
        }
    }

    @SuppressWarnings("unchecked")
    public void status() {
        System.out.println("=== Branches ===");
        String currBranch = Utils.readObject(new
                File(nowBranch), String.class);
        for (String key : ((TreeMap<String, String>) Utils.readObject(
                new File(branchesAndHeads), TreeMap.class)).keySet()) {
            if (key.equals(currBranch)) {
                System.out.print("*");
            }
            System.out.println(key);
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        HashMap<String, String> staged = Utils.readObject(
                new File(".gitlet/stage/addhash.txt"),
                HashMap.class);
        forHelper(staged);
        System.out.println();
        HashMap<String, String> removed = Utils.readObject(
                new File(".gitlet/stage/remhash.txt"),
                HashMap.class);
        System.out.println("=== Removed Files ===");
        forHelper(removed);
        System.out.println();
        List<String> cwdList =
                Utils.plainFilenamesIn(System.getProperty("user.dir"));
        Commit currHead = Utils.readObject(new File(head), Commit.class);
        HashMap<String, String> currHash = currHead.getHashMap();
        System.out.println("=== Modifications Not Staged For Commit ===");
        for (String key : currHash.keySet()) {
            if (cwdList.contains(key)) {
                File f = new File(key);
                String b = new Blob(f).getSha();
                if (!b.equals(currHash.get(key))) {
                    System.out.println(key + " (modified)");
                }
            } else {
                if (!removed.containsKey(key)) {
                    System.out.println(key + " (deleted)");
                }
            }
        }
        for (String key : staged.keySet()) {
            if (cwdList.contains(key)) {
                File f = new File(key);
                String b = new Blob(f).getSha();
                if (!b.equals(staged.get(key))) {
                    System.out.println(key + " (modified)");
                }
            } else {
                System.out.println(key + " (deleted)");
            }
        }
        System.out.println();
        System.out.println("=== Untracked Files ===");
        forHelper2(cwdList, currHash, staged);
        System.out.println();
    }
    public void forHelper(HashMap<String, String> hash) {
        for (String key : hash.keySet()) {
            System.out.println(key);
        }
    }
    public void forHelper2(List<String> cwdList, HashMap<String,
            String> currHash, HashMap<String, String> staged) {
        for (String key : cwdList) {
            if (!currHash.containsKey(key) && !staged.containsKey(key)) {
                System.out.println(key);
            }
        }
    }
    @SuppressWarnings("unchecked")
    public ArrayList<String> helper1(String branch) {
        TreeMap<String, String> tree = Utils.readObject(new
                File(branchesAndHeads), TreeMap.class);
        ArrayList<String> list = new ArrayList<>();
        Commit curr = Utils.readObject(new
                        File(".gitlet/commit/"
                        + tree.get(branch) + ".txt"),
                Commit.class);
        while (curr.getParent() != null) {
            list.add(curr.getHash());
            curr = Utils.readObject(new
                            File(".gitlet/commit/"
                            + curr.getParent() + ".txt"),
                    Commit.class);
        }
        if (curr.getMessage().equals("initial commit")) {
            list.add(curr.getHash());
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    public void merge(String branch) throws IOException {
        String currentBranch = helper3(branch);
        TreeMap<String, String> branches = Utils.readObject(
                new File(branchesAndHeads), TreeMap.class);
        Commit newHead = Utils.readObject(new
                File(".gitlet/commit/"
                + branches.get(branch) + ".txt"), Commit.class);
        Commit currHead = Utils.readObject(new File(head), Commit.class);
        Commit splitCommit = Utils.readObject(new
                File(".gitlet/commit/"
                + getSplit(branch) + ".txt"), Commit.class);
        HashMap<String, String> newHeadHash = newHead.getHashMap();
        HashMap<String, String> currHeadHash = currHead.getHashMap();
        HashMap<String, String> splitHash = splitCommit.getHashMap();
        HashMap<String, String> allFilesHash = (HashMap<String, String>)
                newHead.getHashMap().clone();
        allFilesHash.putAll(currHead.getHashMap());
        allFilesHash.putAll(splitCommit.getHashMap());
        List<String> cwdList =
                Utils.plainFilenamesIn(System.getProperty("user.dir"));
        for (String file : allFilesHash.keySet()) {
            if (cwdList.contains(file) && !currHeadHash.containsKey(file)) {
                commit("Merged " + branch + " into " + currentBranch + ".");
                System.out.println("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
                System.exit(0);
            }
            if (!splitHash.containsKey(file)) {
                if (!newHeadHash.containsKey(file)
                        && currHeadHash.containsKey(file)) {
                    shortcut(file, currHeadHash, null, false,
                            false, false);
                } else if (newHeadHash.containsKey(file)
                        && !currHeadHash.containsKey(file)) {
                    shortcut(file, newHeadHash, null, false,
                            false, false);
                    File f = new File(file);
                } else {
                    if (currHeadHash.get(file).equals(newHeadHash.get(file))) {
                        shortcut(file, currHeadHash, null, false,
                                false, false);
                    } else {
                        shortcut(file, currHeadHash, newHeadHash, true,
                                false, false);
                    }
                }
            } else {
                helper4(file, splitHash, newHeadHash, currHeadHash);
            }
        }
        commit("Merged " + branch + " into " + currentBranch + ".");
    }
    public void helper4(String file, HashMap<String, String> splitHash,
                         HashMap<String, String> newHeadHash,
                         HashMap<String, String> currHeadHash) {
        if (!newHeadHash.containsKey(file)
                || !currHeadHash.containsKey(file)) {
            if (currHeadHash.containsKey(file)
                    && !newHeadHash.containsKey(file)) {
                if (currHeadHash.get(file).
                        equals(splitHash.get(file))) {
                    remove(file);
                } else {
                    shortcut(file, currHeadHash, null,
                            false, true, false);
                }
            } else if (newHeadHash.containsKey(file)
                    && !currHeadHash.containsKey(file)) {
                if (!newHeadHash.get(file).
                        equals(splitHash.get(file))) {
                    shortcut(file, null, newHeadHash,
                            false, false, true);
                }
            } else {
                remove(file);
            }
        } else {
            if (splitHash.get(file).equals(currHeadHash.get(file))) {
                shortcut(file, newHeadHash, null,
                        false, false, false);
            } else if (splitHash.get(file).
                    equals(newHeadHash.get(file))) {
                shortcut(file, currHeadHash, newHeadHash,
                        true, false, false);
            } else {
                shortcut(file, currHeadHash, newHeadHash,
                        true, false, false);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public String helper3(String branch) throws IOException {
        TreeMap<String, String> branches = Utils.readObject(
                new File(branchesAndHeads), TreeMap.class);
        HashMap<String, String> removedHash = Utils.readObject(new
                File(".gitlet/stage/remhash.txt"), HashMap.class);
        HashMap<String, String> addHash = Utils.readObject(new
                File(".gitlet/stage/addhash.txt"), HashMap.class);
        String currentBranch = Utils.readObject(new File(nowBranch),
                String.class);
        if (!removedHash.isEmpty() && !addHash.isEmpty()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
        if (!branches.containsKey(branch)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        if (currentBranch.equals(branch)) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }
        Stage st = Utils.readObject(new File(stage), Stage.class);
        if (!Utils.readObject(new
                        File(".gitlet/stage/addhash.txt"),
                HashMap.class).isEmpty()
                || !Utils.readObject(new
                        File(".gitlet/stage/remhash.txt"),
                HashMap.class).isEmpty()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
        st.clear();
        String splitPoint = getSplit(branch);
        Commit newHead = Utils.readObject(new
                File(".gitlet/commit/"
                + branches.get(branch) + ".txt"), Commit.class);
        Commit currHead = Utils.readObject(new File(head), Commit.class);
        Commit splitCommit = Utils.readObject(new
                File(".gitlet/commit/"
                + splitPoint + ".txt"), Commit.class);
        if (splitCommit.getHash().equals(newHead.getHash())) {
            System.out.println("Given branch is an "
                    + "ancestor of the current branch.");
            System.exit(0);
        }
        if (splitCommit.getHash().equals(currHead.getHash())) {
            System.out.println("Current branch fast-forwarded.");
            checkout3(branch);
            System.exit(0);
        }
        return currentBranch;
    }
    public void shortcut(String file, HashMap<String,
            String> hash1, HashMap<String, String> hash2,
                         boolean yes, boolean no,
                         boolean soso) {
        if (yes) {
            File f = new File(file);
            Utils.writeContents(f, conflict(hash1.get(file),
                    hash2.get(file)));
            System.out.println("Encountered a merge conflict.");
            add(file);
        } else if (no) {
            File f = new File(file);
            Utils.writeContents(f,
                    conflict(hash1.get(file), ""));
            System.out.println("Encountered a merge conflict.");
            add(file);
        } else if (soso) {
            File f = new File(file);
            Utils.writeContents(f,
                    conflict("", hash1.get(file)));
            System.out.println("Encountered a merge conflict.");
            add(file);
        } else {
            File f = new File(file);
            Utils.writeContents(f, (Utils.readObject(new
                    File(".gitlet/blob/"
                    + hash1.get(file)
                    + ".txt"), Blob.class)).getText());
            add(file);
        }
    }
    public String conflict(String headb, String newb) {
        String blob1Text;
        String blob2Text;
        if (headb.equals("")) {
            blob1Text = "";
        } else {
            blob1Text = (Utils.readObject(new
                            File(".gitlet/blob/" + headb + ".txt"),
                    Blob.class)).getText();
        }
        if (newb.equals("")) {
            blob2Text = "";
        } else {
            blob2Text = (Utils.readObject(new
                            File(".gitlet/blob/" + newb + ".txt"),
                    Blob.class)).getText();
        }
        String text = "<<<<<<< HEAD\n" + blob1Text
                + "=======\n" + blob2Text + ">>>>>>>\n";
        return text;
    }
    @SuppressWarnings("unchecked")
    public String getSplit(String branchName) {
        TreeMap<String, String> allBranches =
                Utils.readObject(new File(branchesAndHeads),
                        TreeMap.class);
        ArrayDeque<String> queue = new ArrayDeque<String>();
        HashSet<String> done = new HashSet<String>();
        boolean fork = false;
        String splitComHash;
        Commit currCommit = Utils.readObject(new File(head),
                Commit.class);
        Commit newBranch = Utils.readObject(new
                File(".gitlet/commit/"
                + allBranches.get(branchName) + ".txt"),
                Commit.class);
        queue.add(newBranch.getHash());
        queue.add(currCommit.getHash());
        while (!fork) {
            String firstCom = queue.remove();
            if (!done.contains(firstCom)) {
                String parent = Utils.readObject(new
                                File(".gitlet/commit/"
                                + firstCom + ".txt"),
                        Commit.class).getParent();
                Commit c = Utils.readObject(new
                                File(".gitlet/commit/"
                                + firstCom + ".txt"),
                        Commit.class);
                if (parent != null) {
                    queue.add(parent);
                }
                if (c.secP()) {
                    queue.add(c.getSecP());
                }
                done.add(firstCom);
            } else {
                return firstCom;
            }
        }
        return null;
    }
    public void diff(String branchName1) {
        TreeMap<String, String> branches = Utils.readObject(new File(branchesAndHeads), TreeMap.class);
        Commit x = Utils.readObject()

    }
    public void diff2(String branchName1, String branchName2) {

    }
}
