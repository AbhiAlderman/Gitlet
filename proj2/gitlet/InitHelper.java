package gitlet;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;


public class InitHelper {

    private static final File GITLET_DIR = Repository.GITLET_DIR;
    private static final File COMMITS_FOLDER = Repository.COMMITS_FOLDER;
    private static final File BRANCHES_FOLDER = Repository.BRANCHES_FOLDER;


    private static final File COMMIT_MAP = Repository.COMMIT_MAP;
    private static final File BRANCHES_MAP = Repository.BRANCHES_MAP;
    private static final File CWD_FILE_LIST = Repository.CWD_FILE_LIST;

    //Sets up the folders for both gitlet and commits
    public static void initSetup() {
        if (!GITLET_DIR.exists()) {
            GITLET_DIR.mkdir();
        }
        if (!COMMITS_FOLDER.exists()) {
            COMMITS_FOLDER.mkdir();
        }
        if (!COMMIT_MAP.exists()) {
            HashMap<String, File> commitMap = new HashMap<>();
            Utils.writeObject(COMMIT_MAP, commitMap);
        }
        if (!CWD_FILE_LIST.exists()) {
            ArrayList<String> newList = new ArrayList<>();
            Utils.writeObject(CWD_FILE_LIST, newList);
        }

    }

    public static boolean initExists(Commit initial) {
        HashMap<String, File> commitMap = Utils.readObject(COMMIT_MAP, HashMap.class);
        String initialHash = initial.getHashCode();
        return commitMap.containsKey(initialHash);
    }

}
