package gitlet;


import java.io.File;
import java.util.ArrayList;

public class BranchHelper {
    private static final File BRANCHES_FOLDER = Repository.BRANCHES_FOLDER;

    private static final File HEAD_BRANCH = Repository.HEAD_BRANCH;
    private static final File BRANCHES_MAP = Repository.BRANCHES_MAP;

    public static void branchSetUp() {
        if (!BRANCHES_FOLDER.exists()) {
            BRANCHES_FOLDER.mkdir();
        }
        if (!BRANCHES_MAP.exists()) {
            ArrayList<String> branchesMap = new ArrayList<>();
            Utils.writeObject(BRANCHES_MAP, branchesMap);
        }
    }


    public static void makeBranch(String branchName) {
        File newBranch = Utils.join(BRANCHES_FOLDER, branchName);
        File currentBranch = BranchHelper.getCurrentBranchFile();
        String currentHash = Utils.readContentsAsString(currentBranch);
        Utils.writeContents(newBranch, currentHash);
        ArrayList<String> branchesMap = BranchHelper.getBranchesMap();
        branchesMap.add(branchName);
        Utils.writeObject(BRANCHES_MAP, branchesMap);
    }

    public static String getCurrentBranchName() {
        return Utils.readContentsAsString(HEAD_BRANCH);
    }

    public static File getCurrentBranchFile() {
        String currentBranchName = BranchHelper.getCurrentBranchName();
        return Utils.join(BRANCHES_FOLDER, currentBranchName);
    }

    public static ArrayList<String> getBranchesMap() {
        return Utils.readObject(BRANCHES_MAP, ArrayList.class);
    }

    public static File getBranchFile(String branchName) {
        File thisFile = Utils.join(BRANCHES_FOLDER, branchName);
        if (thisFile.exists()) {
            return thisFile;
        } else {
            System.out.println("THAT BRANCH DONT EXIST");
            return null;
        }

    }

    public static String getBranchContents(String branchName) {
        File branch = BranchHelper.getBranchFile(branchName);
        return Utils.readContentsAsString(branch);
    }

    public static boolean branchExists(String branchName) {
        ArrayList<String> branchesMap = BranchHelper.getBranchesMap();
        return branchesMap.contains(branchName);
    }

    public static void moveCurrentBranchToCommit(Commit commit) {
        String commitHash = commit.getHashCode();
        File currentBranch = BranchHelper.getCurrentBranchFile();
        Utils.writeContents(currentBranch, commitHash);
    }

    public static void setCurrentBranch(String branchName) {
        Utils.writeContents(HEAD_BRANCH, branchName);
    }
}
