package cz.wincor.pnc.main;

import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

/*
 * This class handles the version checking, the message to the user, and the user response to continue or just stop to download
 * a new version.
 */


public class VersionHandler {

    public static boolean continueWithCurrenVersion() {
        // First check if the version is correct or we need to tell the user to upgrade to a newer version.
        // If user just wants to continue, we do so
        
        if (appUpToDate()){
            //app is up to date
            return true;
        }
        else if (continueWithAppNoUpdated()) {
            //the applicaiton is not updated, but the user wants to continue anyway
            return true;     
        }
        //any other case, we stop
        return false;
    }
    
    
    private static boolean appUpToDate() {
        //This method checks the version in the maven.properties file with the version on the pom.xml file
        
        //get both versions, from maven.properties and from the pom.xml at the GitHub page.
        Optional<String> mavenVersion = getVersionFromMavenFile();
        Optional<String> pomVersion = getVersionFromPomFile();
        
        if (pomVersionNewer (mavenVersion, pomVersion)) {
            //POM version is newer, so we are NOT up to date, false
            return false;
        }
        
        //POM version is not newer (equals also works), so we are up to date, true
        return true;
    }

    private static boolean pomVersionNewer(Optional<String> mavenVersion, Optional<String> pomVersion) {
        // compares the maven with the pom version.
        //For now, we just compare a version with three different numbers, separated with dots, and with the most significant version
        //at the left -> numerical value
        
        //convert to actual strings for comparision. We get an empty string if the values were empty.
        String mavenVer = mavenVersion.orElse("");
        String pomVer = pomVersion.orElse("");
        
        //convert to integers and compare numerically
        mavenVer = mavenVer.replaceAll("\\.","");
        pomVer = pomVer.replaceAll("\\.","");
        int mavenVerInt = Integer.parseInt(mavenVer);
        int pomVerInt = Integer.parseInt(pomVer);
        
        //we return true ONLY if the POM has a newer version, a greater numerically value
        return (pomVerInt > mavenVerInt);
    }


    private static Optional<String> getVersionFromPomFile() {
        //this method gets the version of the application from the pom.xml file at GitHub.
        //I need the URL fo the file, then scrap it and get the version number.
        //tags are: <version>"version</version>
        
        Optional<String> value = Optional.of("2.1.0");
        return value;
    }


    private static Optional<String> getVersionFromMavenFile() {
        //this method extracts the version from the maven.properties file.
        //this code is a copy of the one at the AboutMeJFrame class.
        java.io.InputStream is = VersionHandler.class.getResourceAsStream("/maven.properties");
        java.util.Properties p = new Properties();
        try {
            p.load(is);
            //get the property and return it
            String versionMaven = p.getProperty("logWrapper.version");
            return  Optional.of(versionMaven);
        } catch (IOException e) {
            // WE could not get the property, so we have to show the user that the version could not be checked
            e.printStackTrace();            
        }
        return null;
    }


    private static boolean continueWithAppNoUpdated() {
        //this method shows a dialog warning the user that the application has a newer version and asks either to continue or stop
        //the current one.
        
        
        //display the dialog
        
        ///get dialog value
        
        return false;
    }




   
    
}
