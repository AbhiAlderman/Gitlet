package gitlet;


import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Queue;
import java.util.LinkedList;
import java.util.HashMap;



public class MergeHelper {

    public static Commit getLatestAncestor(String branchName) {
        /*Make an ordered queue for the given commits parents.
          Make a hashMap for the current commits parents.
          Go through the queue until a parent matches in the queue and the map.
          Return that commit.
         */
        Commit givenCommit = CommitHelper.getCommit(BranchHelper.getBranchContents(branchName));
        ArrayList<String> givenBranchParents = givenBFS(givenCommit);
        ArrayList<String> currentBranchParents = new ArrayList<>();
        getAllParents(CommitHelper.getHeadCommit(), currentBranchParents);
        Iterator<String> iterateGiven = givenBranchParents.iterator();
        for (int i = 0; i < givenBranchParents.size(); i++) {
            String currentHash = iterateGiven.next();
            if (currentBranchParents.contains(currentHash)) {
                return CommitHelper.getCommit(currentHash);
            }
        }
        System.out.println("Somehow didn't find a split bruh");
        return null;
    }

    private static ArrayList<String> givenBFS(Commit givenCommit) {
        Queue<Commit> queue = new LinkedList<>();
        queue.add(givenCommit);
        ArrayList<String> givenCommitParents = new ArrayList<>();
        while (!queue.isEmpty()) {
            Commit currentCommit = queue.remove();
            if (currentCommit != null) {
                givenCommitParents.add(currentCommit.getHashCode());
                Commit firstParent = CommitHelper.getCommit(currentCommit.getParentHash());
                Commit secondParent = CommitHelper.getCommit(currentCommit.getSecondParentHash());
                if (firstParent != null) {
                    queue.add(firstParent);
                }
                if (secondParent != null) {
                    queue.add(secondParent);
                }
            }
        }
        return givenCommitParents;
    }
    private static void getAllParents(Commit commit, ArrayList<String> parentList) {
        if (commit == null) {
            return;
        }
        parentList.add(commit.getHashCode());
        getAllParents(CommitHelper.getCommit(commit.getParentHash()), parentList);
        getAllParents(CommitHelper.getCommit(commit.getSecondParentHash()), parentList);
    }


    //Makes an arraylist for the files contained in the given commit,
    // the given splitpoint, and the head
    public static ArrayList<String> makeFileNameList(Commit givenCommit, Commit splitPoint) {
        Commit currentCommit = CommitHelper.getHeadCommit();
        ArrayList<String> fileList = new ArrayList<>();
        HashMap<String, String> currentMap = currentCommit.getBlobMap();
        HashMap<String, String> givenMap = givenCommit.getBlobMap();
        HashMap<String, String> splitMap = splitPoint.getBlobMap();
        Iterator<String> currentIterator = currentMap.keySet().iterator();
        Iterator<String> givenIterator = givenMap.keySet().iterator();
        Iterator<String> splitIterator = splitMap.keySet().iterator();
        for (int i = 0; i < currentMap.size(); i++) {
            fileList.add(currentIterator.next());
        }
        for (int i = 0; i < givenMap.size(); i++) {
            String givenNext = givenIterator.next();
            if (!fileList.contains(givenNext)) {
                fileList.add(givenNext);
            }
        }
        for (int i = 0; i < splitMap.size(); i++) {
            String splitNext = splitIterator.next();
            if (!fileList.contains(splitNext)) {
                fileList.add(splitNext);
            }
        }
        return fileList;
    }

    public static boolean filesAreDifferent(Commit firstCommit, Commit secondCommit,
                                            String fileName) {
        HashMap<String, String> firstCommitMap = firstCommit.getBlobMap();
        HashMap<String, String> secondCommitMap = secondCommit.getBlobMap();
        //If one commit has the file but the other doesn't, return true
        if (!firstCommitMap.containsKey(fileName) && secondCommitMap.containsKey(fileName)) {
            return true;
        }
        if (firstCommitMap.containsKey(fileName) && !secondCommitMap.containsKey(fileName)) {
            return true;
        }
        if (!firstCommitMap.containsKey(fileName) && !secondCommitMap.containsKey(fileName)) {
            return false;
        }
        /*
        if (firstCommitMap.get(fileName) == null && secondCommitMap.get(fileName) != null) {
            return true;
        }
        if (firstCommitMap.get(fileName) != null && secondCommitMap.get(fileName) == null) {
            return true;
        }
        if (firstCommitMap.get(fileName) == null && secondCommitMap.get(fileName) == null) {
            return false;
        }
        */
        //If both commits have the file, compare the sha1 hash.

        File firstFile = CommitHelper.getFile(firstCommitMap.get(fileName));
        String firstHash = Utils.sha1(fileName, Utils.readContentsAsString(firstFile));
        File secondFile = CommitHelper.getFile(secondCommitMap.get(fileName));
        String secondHash = Utils.sha1(fileName, Utils.readContentsAsString(secondFile));
        return !firstHash.equals(secondHash);


    }


}
