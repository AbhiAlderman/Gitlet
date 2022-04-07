package gitlet;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class CheckoutHelper {
    private static final File CWD = Repository.CWD;

    private static final File CWD_FILE_LIST = Repository.CWD_FILE_LIST;



    public static void checkoutFile(String fileName, Commit commit) {
        HashMap<String, String> blobjectMap = commit.getBlobMap();
        if (!blobjectMap.containsKey(fileName) || blobjectMap.get(fileName) == null) {
            System.out.println("File does not exist in that commit.");
            return;
        }
        File cwdFile = Utils.join(CWD, fileName);
        File commitFile = CommitHelper.getFile(blobjectMap.get(fileName));
        Utils.writeContents(cwdFile, Utils.readContentsAsString(commitFile));
        ArrayList<String> cwdFileList = Utils.readObject(CWD_FILE_LIST, ArrayList.class);
        if (!cwdFileList.contains(fileName)) {
            cwdFileList.add(fileName);
            Utils.writeObject(CWD_FILE_LIST, cwdFileList);
        }
    }

    //Assumes that the failure cases for branch checkout didnt pass,
    public static void replaceCWDWithCommitFiles(String desiredCommitHash) {
        Commit desiredCommit = CommitHelper.getCommit(desiredCommitHash);
        HashMap<String, String> desiredCommitFiles = desiredCommit.getBlobMap();
        Iterator<String> iterateHashes = desiredCommitFiles.keySet().iterator();
        for (int i = 0; i < desiredCommitFiles.size(); i++) {
            String fileName = iterateHashes.next();
            CheckoutHelper.checkoutFile(fileName, desiredCommit);
        }
        ArrayList<String> cwdFiles = Utils.readObject(CWD_FILE_LIST, ArrayList.class);
        ArrayList<String> deleteList = new ArrayList<>();
        for (int i = 0; i < cwdFiles.size(); i++) {
            String fileName = cwdFiles.get(i);
            if (!desiredCommitFiles.containsKey(fileName)
                    || desiredCommitFiles.get(fileName) == null) {
                File cwdFile = Utils.join(CWD, fileName);
                cwdFile.delete();
                deleteList.add(fileName);
            }
        }
        //Remove the files we deleted from our cwd list.
        for (int i = 0; i < deleteList.size(); i++) {
            cwdFiles.remove(deleteList.get(i));
        }
        Utils.writeObject(CWD_FILE_LIST, cwdFiles);
    }

    //Checks if any files tracked in the given commit arent tracked by the head commit
    public static boolean checkUntrackedFileS(String commitID) {
        Commit currentCommit = CommitHelper.getHeadCommit();
        Commit givenCommit = CommitHelper.getCommit(commitID);
        HashMap<String, String> currentCommitMap = currentCommit.getBlobMap();
        HashMap<String, String> givenCommitMap = givenCommit.getBlobMap();
        if (givenCommitMap.isEmpty()) {
            return false;
        }
        Iterator<String> iterator = givenCommitMap.keySet().iterator();
        for (int i = 0; i < givenCommitMap.size(); i++) {
            String nextFile = iterator.next();
            if (!currentCommitMap.containsKey(nextFile)) {
                File cwdFile = Utils.join(CWD, nextFile);
                if (cwdFile.exists()) {
                    return true;
                }
            }
        }
        return false;
    }

    public static String checkAbbreviated(String shortenedHash) {
        HashMap<String, File> commitMap = CommitHelper.getCommitMap();
        Iterator<String> iterateHashes = commitMap.keySet().iterator();
        for (int i = 0; i < commitMap.size(); i++) {
            String nextHash = iterateHashes.next();
            if (nextHash.startsWith(shortenedHash)) {
                return nextHash;
            }
        }
        return null;
    }
}
