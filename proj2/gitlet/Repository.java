package gitlet;




import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collections;
import java.util.Set;
/** Represents a gitlet repository.
 *
 *  does at a high level.
 *
 *  @author Robert Abhi Alderman
 */
public class Repository implements Serializable {
    /*
     *
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = Utils.join(CWD, ".gitlet");
    public static final File STAGING_FOLDER = Utils.join(GITLET_DIR, ".staging");
    public static final File COMMITS_FOLDER = Utils.join(GITLET_DIR, ".commits");
    public static final File BLOBJECTS_FOLDER = Utils.join(GITLET_DIR, ".blobjects");
    public static final File BRANCHES_FOLDER = Utils.join(COMMITS_FOLDER, ".branches");

    //These 3 files correspond to the 3 hashmaps below
    public static final File COMMIT_MAP = Utils.join(COMMITS_FOLDER, "commitmap");
    public static final File HEAD_BRANCH = Utils.join(BRANCHES_FOLDER, "head");
    public static final File BLOBJECTS_MAP = Utils.join(BLOBJECTS_FOLDER, "blobjectsMap");
    public static final File STAGING_MAP = Utils.join(STAGING_FOLDER, "stagingMap");
    public static final File BRANCHES_MAP = Utils.join(BRANCHES_FOLDER, "branchesMap");
    public static final File STAGING_REMOVE = Utils.join(STAGING_FOLDER, "stagingRemove");
    public static final File CWD_FILE_LIST = Utils.join(GITLET_DIR, "cwdFileMap");

    private static final String ZERO_TIME = "Wed Dec 31 16:00:00 1969 -0800";


    public static void init() {
        InitHelper.initSetup();
        BranchHelper.branchSetUp();
        CommitHelper.commitSetUp();
        Commit initial = new Commit("initial commit", null, ZERO_TIME,
                new HashMap<>(), null);
        if (InitHelper.initExists(initial)) {
            System.out.println("A Gitlet version-control system already "
                    + "exists in the current directory.");
            return;
        }
        File masterBranch = Utils.join(BRANCHES_FOLDER, "master");
        Utils.writeContents(HEAD_BRANCH, "master");
        CommitHelper.saveCommit(initial);
        BranchHelper.moveCurrentBranchToCommit(initial);
        ArrayList<String> branchMap = BranchHelper.getBranchesMap();
        branchMap.add("master");
        Utils.writeObject(BRANCHES_MAP, branchMap);
    }

    public static void add(String fileName) {
        File cwdFile = Utils.join(CWD, fileName);
        InitHelper.initSetup();
        StageHelper.stageSetUp();
        StageHelper.checkCWDFile(cwdFile);
        Commit currentCommit = CommitHelper.getHeadCommit();
        if (StageHelper.existsInCommit(fileName, cwdFile, currentCommit)) {
            HashMap<String, File> stageMap = Utils.readObject(STAGING_MAP, HashMap.class);
            ArrayList<String> stageRemove = Utils.readObject(STAGING_REMOVE, ArrayList.class);
            if (stageMap.containsKey(fileName)) {
                stageMap.remove(fileName);
                Utils.writeObject(STAGING_MAP, stageMap);
            }
            if (stageRemove.contains(fileName)) {
                stageRemove.remove(fileName);
                Utils.writeObject(STAGING_REMOVE, stageRemove);
            }
            return;
        }
        StageHelper.addToStaging(fileName);
    }

    public static void remove(String fileName) {
        StageHelper.stageSetUp();
        HashMap<String, File> stageMap = StageHelper.getStageMap();
        Commit currentCommit = CommitHelper.getHeadCommit();
        if (stageMap.containsKey(fileName)) {
            stageMap.remove(fileName);
            Utils.writeObject(STAGING_MAP, stageMap);
            File stagedFile = Utils.join(STAGING_FOLDER, fileName);
            stagedFile.delete();
        } else if (currentCommit.getBlobMap().containsKey(fileName)) {
            ArrayList<String> stageRemove = StageHelper.getRemoveList();
            stageRemove.add(fileName);
            Utils.writeObject(STAGING_REMOVE, stageRemove);
            File cwdFile = Utils.join(CWD, fileName);
            cwdFile.delete();
            ArrayList<String> cwdFiles = Utils.readObject(CWD_FILE_LIST, ArrayList.class);
            cwdFiles.remove(fileName);
            Utils.writeObject(CWD_FILE_LIST, cwdFiles);
        } else {
            System.out.println("No reason to remove the file.");
        }
    }

    public static void commit(String message, String secondParentHash) {

        //Read from my computer: the head commit object and the staging area

        //Clone the HEAD commit
        //Modify its message and timestamp according to user input
        //Use the staging area to modify the files tracked by the new commit.

        //Write back any new objects made or any modified objects read earlier

        //Add the files in commit to the CWD!
        StageHelper.stageSetUp();
        if (message == null || message.isBlank() || message.isEmpty()) {
            System.out.println("Please enter a commit message.");
            return;
        }
        HashMap<String, File> stageMap = StageHelper.getStageMap();
        ArrayList<String> stageRemove = StageHelper.getRemoveList();
        if (stageMap.isEmpty() && stageRemove.isEmpty() && secondParentHash == null) {
            System.out.println("No changes added to the commit");
            return;
        }
        CommitHelper.commitSetUp();
        Commit headCommit = CommitHelper.getHeadCommit();
        Commit newCommit = headCommit.clone();
        newCommit.updateTimeStamp();
        newCommit.updateMessage(message);
        newCommit.updateParentHash(headCommit.getHashCode());
        newCommit.updateSecondParentHash(secondParentHash);
        CommitHelper.commitStageAndRemove(newCommit);
        //Make the CWD contain all the files in commit
    }

    public static void saveCWD(Commit commit) {
        HashMap<String, String> commitFiles = commit.getBlobMap();
        Iterator<String> iterateFiles = commitFiles.keySet().iterator();
        ArrayList<String> tempList = new ArrayList<>();
        for (int i = 0; i < commitFiles.size(); i++) {
            tempList.add(iterateFiles.next());
        }
        Utils.writeObject(CWD_FILE_LIST, tempList);
    }

    public static void log() {
        HashMap<String, File> commitMap = Utils.readObject(COMMIT_MAP, HashMap.class);
        Commit currentCommit = CommitHelper.getHeadCommit();
        for (int i = 0; i < commitMap.size(); i++) {
            System.out.println("===");
            System.out.println("commit " + currentCommit.getHashCode());
            if (currentCommit.getSecondParentHash() != null) {
                String firstParent = currentCommit.getParentHash();
                String secondParent = currentCommit.getSecondParentHash();
                System.out.println("Merge: " + firstParent.substring(0, 7) + " "
                        + secondParent.substring(0, 7));
            }
            System.out.println("Date: " + currentCommit.getTimeStamp());
            System.out.println(currentCommit.getMessage());
            System.out.println();
            if (currentCommit.getTimeStamp().equals(ZERO_TIME)) {
                return;
            }
            if (!(currentCommit.getParentHash() == null)) {
                currentCommit = CommitHelper.getCommit(currentCommit.getParentHash());
            }
        }
    }

    public static void checkoutCommitID(String commitID, String name) {
        if (commitID.length() < 40) {
            commitID = CheckoutHelper.checkAbbreviated(commitID);
        }
        if (commitID == null || !CommitHelper.commitExists(commitID)) {
            System.out.println("No commit with that id exists.");
            return;
        }
        Commit commit = CommitHelper.getCommit(commitID);
        CheckoutHelper.checkoutFile(name, commit);
    }

    public static void checkoutHead(String name) {
        Commit head = CommitHelper.getHeadCommit();
        CheckoutHelper.checkoutFile(name, head);
    }

    public static void checkoutBranch(String branchName) {
        ArrayList<String> branchMap = BranchHelper.getBranchesMap();
        if (!branchMap.contains(branchName)) {
            System.out.println("No such branch exists.");
            return;
        }
        String currentBranch = BranchHelper.getCurrentBranchName();
        if (branchName.equals(currentBranch)) {
            System.out.println("No need to checkout the current branch.");
            return;
        }
        String branchCommitID = BranchHelper.getBranchContents(branchName);
        if (CheckoutHelper.checkUntrackedFileS(branchCommitID)) {
            System.out.println("There is an untracked file in the way, delete it, "
                    + "or add and commit it first.");
            return;
        }
        CheckoutHelper.replaceCWDWithCommitFiles(branchCommitID);
        StageHelper.clearStaging();
        BranchHelper.setCurrentBranch(branchName);
    }

    public static void reset(String commitID) {
        if (!CommitHelper.commitExists(commitID)) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        if (CheckoutHelper.checkUntrackedFileS(commitID)) {
            System.out.println("There is an untracked file in the way, "
                    + "delete it, or add and commit it first.");
            return;
        }
        CheckoutHelper.replaceCWDWithCommitFiles(commitID);
        Commit commit = CommitHelper.getCommit(commitID);
        BranchHelper.moveCurrentBranchToCommit(commit);
        StageHelper.clearStaging();
    }


    public static void globalLog() {
        HashMap<String, File> commitMap = CommitHelper.getCommitMap();
        Iterator<String> commitIterate = commitMap.keySet().iterator();
        for (int i = 0; i < commitMap.size(); i++) {
            Commit currentCommit = CommitHelper.getCommit(commitIterate.next());
            System.out.println("===");
            System.out.println("commit " + currentCommit.getHashCode());
            System.out.println("Date: " + currentCommit.getTimeStamp());
            System.out.println(currentCommit.getMessage());
            System.out.println();
        }
    }

    public static void find(String message) {
        HashMap<String, File> commitMap = CommitHelper.getCommitMap();
        Iterator<String> commitIterator = commitMap.keySet().iterator();
        boolean foundACommit = false;
        for (int i = 0; i < commitMap.size(); i++) {
            Commit currentCommit = CommitHelper.getCommit(commitIterator.next());
            if (currentCommit.getMessage().equals(message)) {
                System.out.println(currentCommit.getHashCode());
                foundACommit = true;
            }
        }
        if (!foundACommit) {
            System.out.println("Found no commit with that message.");
        }
    }

    public static void makeBranch(String branchName) {
        BranchHelper.branchSetUp();
        if (BranchHelper.branchExists(branchName)) {
            System.out.println("A branch with that name already exists.");
            return;
        }
        BranchHelper.makeBranch(branchName);
    }

    public static void removeBranch(String branchName) {
        ArrayList<String> branchMap = BranchHelper.getBranchesMap();
        if (!branchMap.contains(branchName)) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        String headBranch = BranchHelper.getCurrentBranchName();
        if (headBranch.equals(branchName)) {
            System.out.println("Cannot remove the current branch.");
            return;
        }
        File branch = BranchHelper.getBranchFile(branchName);
        branchMap.remove(branchName);
        branch.delete();
        Utils.writeObject(BRANCHES_MAP, branchMap);
    }

    public static void merge(String branchName) {
        if (checkMergeBaseCases(branchName)) {
            return;
        }
        String givenCommitHash = Utils.readContentsAsString(BranchHelper.getBranchFile(branchName));
        Commit splitPoint = MergeHelper.getLatestAncestor(branchName);
        Commit givenCommit = CommitHelper.getCommit(givenCommitHash);
        Commit currentCommit = CommitHelper.getHeadCommit();
        //Make hashmap of all files in splitpoint, current, and branch
        ArrayList<String> fileList = MergeHelper.makeFileNameList(givenCommit, splitPoint);
        /* Iterate through the list, and perform the right action on each file based
           on the conditions given in the spec. 8 total conditions.*/
        HashMap<String, String> splitPointMap = splitPoint.getBlobMap();
        HashMap<String, String> givenCommitMap = givenCommit.getBlobMap();
        HashMap<String, String> currentCommitMap = currentCommit.getBlobMap();
        boolean encounteredMerge = false;
        for (int i = 0; i < fileList.size(); i++) {
            String fileName = fileList.get(i);
            //Split point didnt have that file
            if (!splitPointMap.containsKey(fileName)) {
                //Case 4: Present only in current
                if (currentCommitMap.containsKey(fileName)
                        && currentCommitMap.get(fileName) != null) {
                    if (!givenCommitMap.containsKey(fileName)
                            || givenCommitMap.get(fileName) == null) {
                        continue;
                    }
                }
                //Case 5: Present only in given.
                if (givenCommitMap.containsKey(fileName)
                        && givenCommitMap.get(fileName) != null) {
                    if (!currentCommitMap.containsKey(fileName)
                            || currentCommitMap.get(fileName) == null) {
                        CheckoutHelper.checkoutFile(fileName, givenCommit);
                        StageHelper.addToStaging(fileName);
                        continue;
                    }
                }
            }
            //Case 6: Present in split, unmodified in current, absent from given.
            if (!givenCommitMap.containsKey(fileName)
                    && !MergeHelper.filesAreDifferent(splitPoint, currentCommit, fileName)) {
                Repository.remove(fileName);
                CommitHelper.saveCommit(currentCommit);
                continue;
            }
            //Case 7: Present in split, unmodified in given, absent from current.
            if (!currentCommitMap.containsKey(fileName)
                    && !MergeHelper.filesAreDifferent(splitPoint, givenCommit, fileName)) {
                continue;
            }
            //Case 2: Modified in current, but not in given.
            if (MergeHelper.filesAreDifferent(splitPoint, currentCommit, fileName)
                    && !MergeHelper.filesAreDifferent(splitPoint, givenCommit, fileName)) {
                continue;
            }
            //Case 1: Modified in given, but not in current.
            if (!MergeHelper.filesAreDifferent(splitPoint, currentCommit, fileName)
                    && MergeHelper.filesAreDifferent(splitPoint, givenCommit, fileName)) {
                CheckoutHelper.checkoutFile(fileName, givenCommit);
                StageHelper.addToStaging(fileName);
                continue;
            }
            //Case 3: Modified the same way in both commits.
            if (!MergeHelper.filesAreDifferent(currentCommit, givenCommit, fileName)) {
                continue;
            } else {
                /* If all other cases fail, then its case 8 */
                encounteredMerge = true;
                caseEight(currentCommit, givenCommit, currentCommitMap, givenCommitMap, fileName);
            }
        }
        //Make commit and change the second parent.
        Repository.commit("Merged " + branchName + " into "
                + BranchHelper.getCurrentBranchName() + ".", givenCommitHash);
        if (encounteredMerge) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    private static void caseEight(Commit currentCommit, Commit givenCommit,
                                  HashMap<String, String> currentCommitMap,
                                  HashMap<String, String> givenCommitMap, String fileName) {
        if (!currentCommitMap.containsKey(fileName)
                || currentCommitMap.get(fileName) == null) {
            File givenFile = CommitHelper.getFile(givenCommitMap.get(fileName));
            String givenContents = Utils.readContentsAsString(givenFile);
            String newContents = "<<<<<<< HEAD\n"
                    + "=======\n" + givenContents + "\n";
            String currentFileHash = Utils.sha1(newContents);
            File currentFile = Utils.join(BLOBJECTS_FOLDER, currentFileHash);
            Utils.writeContents(currentFile, newContents);
            currentCommitMap.put(fileName, currentFileHash);
            CommitHelper.saveCommit(currentCommit);
            CheckoutHelper.checkoutFile(fileName, currentCommit);
        } else if (!givenCommitMap.containsKey(fileName)
                || givenCommitMap.get(fileName) == null) {
            File currentFile = CommitHelper.getFile(currentCommitMap.get(fileName));
            String currentContents = Utils.readContentsAsString(currentFile);
            String newContents = "<<<<<<< HEAD\n" + currentContents
                    + "=======\n" + ">>>>>>>\n";
            Utils.writeContents(currentFile, newContents);
            CheckoutHelper.checkoutFile(fileName, currentCommit);
        } else {
            File currentFile = CommitHelper.getFile(currentCommitMap.get(fileName));
            String currentContents = Utils.readContentsAsString(currentFile);
            File givenFile = CommitHelper.getFile(givenCommitMap.get(fileName));
            String givenContents = Utils.readContentsAsString(givenFile);
            String newContents = "<<<<<<< HEAD\n" + currentContents
                    + "=======\n" + givenContents + ">>>>>>>\n";
            Utils.writeContents(currentFile, newContents);
            CheckoutHelper.checkoutFile(fileName, currentCommit);
        }
    }

    private static boolean checkMergeBaseCases(String branchName) {
        if (!StageHelper.stageAddIsEmpty() || !StageHelper.stageRemoveIsEmpty()) {
            System.out.println("You have uncomitted changes.");
            System.exit(0);
        }
        if (!BranchHelper.branchExists(branchName)) {
            System.out.println("A branch with that name does not exist.");
            return true;
        }
        if (BranchHelper.getCurrentBranchName().equals(branchName)) {
            System.out.println("Cannot merge a branch with itself.");
            return true;
        }
        String givenCommitHash = Utils.readContentsAsString(BranchHelper.getBranchFile(branchName));
        if (CheckoutHelper.checkUntrackedFileS(givenCommitHash)) {
            System.out.println("There is an untracked file in the way; "
                    + "delete it, or add and commit it first.");
            System.exit(0);
        }
        Commit splitPoint = MergeHelper.getLatestAncestor(branchName);
        Commit givenCommit = CommitHelper.getCommit(givenCommitHash);
        Commit currentCommit = CommitHelper.getHeadCommit();
        if (splitPoint.getHashCode().equals(givenCommit.getHashCode())) {
            System.out.println("Given branch is an ancestor of the current branch.");
            return true;
        }
        if (splitPoint.getHashCode().equals(currentCommit.getHashCode())) {
            System.out.println("Current branch fast-forwarded.");
            Repository.checkoutBranch(branchName);
            return true;
        }
        return false;
    }

    public static void status() {
        InitHelper.initSetup();
        BranchHelper.branchSetUp();
        CommitHelper.commitSetUp();
        StageHelper.stageSetUp();
        System.out.println("=== Branches ===");
        ArrayList<String> branchesMap = BranchHelper.getBranchesMap();
        Collections.sort(branchesMap);
        Iterator<String> iterateBranches = branchesMap.iterator();
        for (int i = 0; i < branchesMap.size(); i++) {
            String branchName = iterateBranches.next();
            if (BranchHelper.getCurrentBranchName().equals(branchName)) {
                System.out.println("*" + branchName);
            } else {
                System.out.println(branchName);
            }
        }
        System.out.println();
        System.out.println("=== Staged Files ===");

        HashMap<String, File> stagedMap = StageHelper.getStageMap();
        if (!stagedMap.isEmpty()) {
            Iterator<String> iterateStage = hashKeysToArray(stagedMap).iterator();
            for (int i = 0; i < stagedMap.size(); i++) {
                System.out.println(iterateStage.next());
            }
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        ArrayList<String> removeList = StageHelper.getRemoveList();
        if (!removeList.isEmpty()) {
            Collections.sort(removeList);
            Iterator<String> iterateRemoved = removeList.iterator();
            for (int i = 0; i < removeList.size(); i++) {
                System.out.println(iterateRemoved.next());
            }
        }

        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        //Extra credit for this
        System.out.println();
        System.out.println("=== Untracked Files ===");
        //Extra credit for this
        System.out.println();

    }

    private static ArrayList<String> hashKeysToArray(HashMap<String, File> hashMap) {
        ArrayList<String> arrayList = new ArrayList<>();
        Set<String> keySet = hashMap.keySet();
        arrayList.addAll(keySet);
        Collections.sort(arrayList);
        return arrayList;
    }

    public static void checkInitialized() {

        if (!GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
    }







}
