package gitlet;

import java.io.File;
import java.io.IOException;

/**
 * Driver class for Gitlet, the tiny stupid version-control system.
 *
 * @author Ananya Bahugudumbi with collaborator Ethan Auyeung
 */
public class Main {

    /**
     * Usage: java gitlet.Main ARGS, where ARGS contains
     * <COMMAND> <OPERAND> ....
     */
    public static void main(String... args) throws IOException {
        Repository repo = new Repository();
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        if (!(new File(".gitlet/stage/init.txt")).exists()
                && !args[0].equals("init")) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
        if (args[0].equals("init") && args.length == 1) {
            repo.init();
        } else if (args[0].equals("add") && args.length == 2) {
            repo.add(args[1]);
        } else if (args[0].equals("commit") && args.length == 2) {
            repo.commit(args[1]);
        } else if (args[0].equals("log") && args.length == 1) {
            repo.log();
        } else if (args[0].equals("checkout") && args.length == 2) {
            repo.checkout3(args[1]);
        } else if (args[0].equals("checkout") && args[1].equals("--")
                && args.length == 3) {
            repo.checkout(args[2]);
        } else if (args[0].equals("checkout") && args[2].equals("--")
                && args.length == 4) {
            repo.checkout(args[1], args[3]);
        } else if (args[0].equals("rm") && args.length == 2) {
            repo.remove(args[1]);
        } else if (args[0].equals("global-log") && args.length == 1) {
            repo.globalLog();
        } else if (args[0].equals("find") && args.length == 2) {
            repo.find(args[1]);
        } else if (args[0].equals("status") && args.length == 1) {
            repo.status();
        } else if (args[0].equals("branch") && args.length == 2) {
            repo.branch(args[1]);
        } else if (args[0].equals("rm-branch") && args.length == 2) {
            repo.rmBranch(args[1]);
        } else if (args[0].equals("reset") && args.length == 2) {
            repo.reset(args[1]);
        } else if (args[0].equals("merge")) {
            repo.merge(args[1]);
        } else if (!args[0].equals("init") && !args[0].equals("merge")
                && !args[0].equals("add") && !args[0].equals("commit")
                && !args[0].equals("log") && !args[0].equals("checkout")
                && !args[0].equals("rm") && !args[0].equals("global-log")
                && !args[0].equals("find") && !args[0].equals("status")
                && !args[0].equals("branch") && !args[0].equals("rm-branch")
                && !args[0].equals("reset") && !args[0].equals("merge")) {
            System.out.println("No command with that name exists.");
            System.exit(0);
        } else {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }
}
