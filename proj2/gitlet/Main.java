package gitlet;


/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author Abhi Alderman
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        String firstArg = args[0];
        switch (firstArg) {
            case "init":
                Repository.init();
                break;
            case "add":
                Repository.checkInitialized();
                Repository.add(args[1]);
                break;
            case "commit":
                Repository.checkInitialized();
                Repository.commit(args[1], null);
                break;
            case "log":
                Repository.checkInitialized();
                Repository.log();
                break;
            case "checkout":
                Repository.checkInitialized();
                if (args.length == 4) {
                    if (!args[2].equals("--")) {
                        System.out.println("Incorrect operands.");
                    } else {
                        Repository.checkoutCommitID(args[1], args[3]);
                        break;
                    }
                }
                if (args.length == 3) {
                    if (!args[1].equals("--")) {
                        System.out.println("Incorrect operands.");
                    } else {
                        Repository.checkoutHead(args[2]);
                    }
                }
                if (args.length == 2) {
                    Repository.checkoutBranch(args[1]);
                }
                break;
            case "rm":
                Repository.checkInitialized();
                Repository.remove(args[1]);
                break;
            case "global-log":
                Repository.checkInitialized();
                Repository.globalLog();
                break;
            case "find":
                Repository.checkInitialized();
                Repository.find(args[1]);
                break;
            case "branch":
                Repository.checkInitialized();
                Repository.makeBranch(args[1]);
                break;
            case "rm-branch":
                Repository.checkInitialized();
                Repository.removeBranch(args[1]);
                break;
            case "reset":
                Repository.checkInitialized();
                Repository.reset(args[1]);
                break;
            case "merge":
                Repository.checkInitialized();
                Repository.merge(args[1]);
                break;
            case "status":
                Repository.checkInitialized();
                Repository.status();
                break;
            default:
                System.out.println("No command with that name exists.");
                break;


        }

    }


}
