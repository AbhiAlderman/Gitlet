package gitlet;



import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/** Represents a gitlet commit object.
 *
 *  does at a high level.
 *
 *  @author Robert Abhi Alderman
 */
public class Commit implements Serializable {
    /*
     *
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */

    private final HashMap<String, String> fileMap;
    private String message;
    private String timestamp;
    private String parentHash;
    private String secondParentHash;



    public Commit(String message, String parentHashCode, String timestamp,
                  HashMap<String, String> hashMap, String secondParentHash) {
        this.message = message;
        this.parentHash = parentHashCode;
        this.timestamp = timestamp;
        this.fileMap = hashMap;
        this.secondParentHash = null;

    }

    public String getSecondParentHash() {
        return this.secondParentHash;
    }
    public String getMessage() {
        return this.message;
    }

    public String getTimeStamp() {
        return this.timestamp;
    }

    public String getParentHash() {
        return this.parentHash;
    }

    public HashMap<String, String> getBlobMap() {
        return this.fileMap;
    }

    public void updateMessage(String newMessage) {
        this.message = newMessage;
    }

    public void updateTimeStamp() {
        SimpleDateFormat parseFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy ZZZZZ");
        Date date = new Date();
        this.timestamp = parseFormat.format(date);
    }

    public void updateParentHash(String parentHashCode) {
        this.parentHash = parentHashCode;
    }

    public void updateSecondParentHash(String secondParent) {
        this.secondParentHash = secondParent;
    }
    public Commit clone() {
        return new Commit(this.message, this.parentHash, this.timestamp,
                this.fileMap, this.secondParentHash);
    }

    public String getHashCode() {
        return Utils.sha1(Utils.serialize(this));
    }


}
