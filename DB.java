import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public abstract class DB {
    // Absolute path to the folder containing the db
    public final String path;
    // Name of the db file, including the file extension
    public final String dbName;
    // File extension of the db file
    public final String fileExtension;
    
    public DB(String path, String dbName) {
        this.path = path;
        this.dbName = dbName;
        this.fileExtension = "." + dbName.split("\\.")[1];
    }
    
    public boolean checkCreate() {
        // First make sure dbName contains no periods other than the file extension so as to not break getdbNameNoExtension()
        if (this.dbName.split("\\.").length != 2) {
            System.out.println("ERROR: dbName may contain only one period '.' character");
            return false;
        }
        
        // Check if the db file exists and is readable + writable
        File dbfile = new File(this.getdbPath());
        if (!dbfile.exists()) {
            try {
                dbfile.createNewFile();
                dbfile.setReadable(true);
                dbfile.setWritable(true);
            } catch (Exception e) {
                System.out.println(e);
                return false;
            }
        }
        
        if (!(Files.isReadable(Paths.get(this.getdbPath())) && Files.isWritable(Paths.get(this.getdbPath()))))
            return false;
        
        else
            return true;
    }
    
    public String getdbPath() {
        return this.path + File.separator + this.dbName;
    }
    
    // Returns dbName minus the file extension
    // Make sure dbName contains no dots other than the file extension
    public String getdbNameNoExtension() {
        return this.dbName.split("\\.")[0];
    }
    
    /*
     * Function that returns file for the next save, and renames the old file
     */
    public File getNextFile() {
        File db = new File(this.getdbPath());
        File dbOld = new File(this.getdbNameNoExtension() + "OLD" + this.fileExtension);
        
        // if the dbOld file already exists, delete it
        if (dbOld.exists())
            dbOld.delete();
        
        // Try to rename the current db file to be the old db file
        if (db.renameTo(dbOld)) {
            // If successful, create a new db file and try to create it
            File nextdb = new File(this.getdbPath());
            try {
                if (nextdb.createNewFile()) {
                    nextdb.setReadable(true);
                    nextdb.setWritable(true);
                    return nextdb;
                }
                else
                    throw new IOException("ERROR: Could not create new db file");
            } catch (Exception e) {
                System.out.println(e);
                return null;
            }
        }
        // Otherwise, we couldn't rename the current db file so return null
        // Keep in mind the old db file will still be deleted regardless of if we successfully rename the current db file
        else
            return null;
    }
}
