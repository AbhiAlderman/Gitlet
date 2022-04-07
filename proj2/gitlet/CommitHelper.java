package gitlet;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class CommitHelper {
    private static final File COMMITS_FOLDER = Repository.COMMITS_FOLDER;
    private static final File BLOBJECTS_FOLDER = Repository.BLOBJECTS_FOLDER;

    private static final File BLOBJECTS_MAP = Repository.BLOBJECTS_MAP;
    private static final File COMMIT_MAP = Repository.COMMIT_MAP;
    private static final File STAGING_MAP = Repository.STAGING_MAP;
    private static final File STAGING_REMOVE = Repository.STAGING_REMOVE;

    //NOTE: Create a staging_area for files that are about to be removed, then when committing
    //remove those files. will be used in rm command.
    public static void commitSetUp() {
        if (!BLOBJECTS_FOLDER.exists()) {
            BLOBJECTS_FOLDER.mkdir();
        }
        if (!BLOBJECTS_MAP.exists()) {
            HashMap<String, File> blobMap = new HashMap<>();
            Utils.writeObject(BLOBJECTS_MAP, blobMap);
        }
    }

    public static void saveCommit(Commit commit) {
        String commitHash = Utils.sha1(Utils.serialize(commit));
        File commitFile = Utils.join(COMMITS_FOLDER, commitHash);
        Utils.writeObject(commitFile, commit);
        HashMap<String, File> commitMap = CommitHelper.getCommitMap();
        commitMap.put(commitHash, commitFile);
        Utils.writeObject(COMMIT_MAP, commitMap);
    }

    public static Commit getCommit(String commitHash) {
        if (commitHash == null) {
            return null;
        }
        if (!CommitHelper.commitExists(commitHash)) {
            System.out.println("Commit doesnt exist");
            return null;
        }
        HashMap<String, File> commitMap = CommitHelper.getCommitMap();
        File commitFile = commitMap.get(commitHash);
        return Utils.readObject(commitFile, Commit.class);
    }

    public static Commit getHeadCommit() {
        File currentBranch = BranchHelper.getCurrentBranchFile();
        String currentCommitHash = Utils.readContentsAsString(currentBranch);
        return getCommit(currentCommitHash);
    }

    public static HashMap<String, File> getCommitMap() {
        return Utils.readObject(COMMIT_MAP, HashMap.class);
    }

    public static boolean commitExists(String commitID) {
        HashMap<String, File> commitMap = CommitHelper.getCommitMap();
        return commitMap.containsKey(commitID);
    }

    public static void commitStageAndRemove(Commit commit) {
        HashMap<String, File> stageMap = StageHelper.getStageMap();
        HashMap<String, String> commitHashes = commit.getBlobMap();
        if (!stageMap.isEmpty()) {
            Iterator<String> keySet = stageMap.keySet().iterator();
            for (int i = 0; i < stageMap.size(); i++) {
                String fileName = keySet.next();
                File stagedFile = stageMap.get(fileName);
                String fileHash = Utils.sha1(fileName, Utils.readContents(stagedFile));
                File committedFile = Utils.join(BLOBJECTS_FOLDER, fileHash);
                Utils.writeContents(committedFile, Utils.readContents(stagedFile));
                if (commitHashes.containsKey(fileName)) {
                    commitHashes.replace(fileName, fileHash);
                } else {
                    commitHashes.put(fileName, fileHash);
                }
                //Add to Blobjects Map? May not be necessary
                HashMap<String, File> blobjectsMap = getBlobjectsMap();
                blobjectsMap.put(fileHash, committedFile);
                stagedFile.delete();
            }
            stageMap.clear();
            Utils.writeObject(STAGING_MAP, stageMap);
        }
        ArrayList<String> stageRemove = StageHelper.getRemoveList();
        if (!stageRemove.isEmpty()) {
            Iterator<String> removeIterate = stageRemove.iterator();
            for (int i = 0; i < stageRemove.size(); i++) {
                commitHashes.remove(removeIterate.next());
            }
            stageRemove.clear();
            Utils.writeObject(STAGING_REMOVE, stageRemove);
        }
        CommitHelper.saveCommit(commit);
        BranchHelper.moveCurrentBranchToCommit(commit);
        Repository.saveCWD(commit);
    }

    public static HashMap<String, File> getBlobjectsMap() {
        return Utils.readObject(BLOBJECTS_MAP, HashMap.class);
    }

    public static File getFile(String fileHash) {
        return Utils.join(BLOBJECTS_FOLDER, fileHash);
    }







}
