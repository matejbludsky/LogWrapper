package cz.wincor.pnc.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
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

        if (appUpToDate()) {
            // app is up to date
            return true;
        } else if (continueWithAppNoUpdated()) {
            // the applicaiton is not updated, but the user wants to continue anyway
            return true;
        }
        // any other case, we stop
        return false;
    }

    private static boolean appUpToDate() {
        // This method checks the version in the maven.properties file with the version on the pom.xml file

        // get both versions, from maven.properties and from the pom.xml at the GitHub page.
        Optional<String> mavenVersion = getVersionFromMavenFile();
        Optional<String> pomVersion = getVersionFromPomFile();

        if (pomVersionNewer(mavenVersion, pomVersion)) {
            // POM version is newer, so we are NOT up to date, false
            return false;
        }

        // POM version is not newer (equals also works), so we are up to date, true
        return true;
    }

    private static boolean pomVersionNewer(Optional<String> mavenVersion, Optional<String> pomVersion) {
        // compares the maven with the pom version.
        // For now, we just compare a version with three different numbers, separated with dots, and with the most
        // significant version
        // at the left -> numerical value

        // convert to actual strings for comparing. We get an empty string if the values were empty.
        String mavenVer = mavenVersion.orElse("");
        String pomVer = pomVersion.orElse("");

        // convert to integers and compare numerically
        mavenVer = mavenVer.replaceAll("\\.", "");
        pomVer = pomVer.replaceAll("\\.", "");
        int mavenVerInt = Integer.parseInt(mavenVer);
        int pomVerInt = Integer.parseInt(pomVer);

        // we return true ONLY if the POM has a newer version, a greater numerically value
        return (pomVerInt > mavenVerInt);
    }

    private static Optional<String> getVersionFromPomFile() {
        // This method gets the version of the application from the pom.xml file at GitHub.
        // I need the URL for the file, then use an HTTPUrlConnecto, check the header for
        // a non error response, get the page as a string with an
        // InputStream ()HttpURLConnection.getInputStream()) and put it in a char[] buffer.
        // Pass that to a String and look for the tags are: <version>"version</version>
        //Issues: -github uses redirects and HTTPURLConnection does not handle those so I'll have to do it manually
        //         -we have a proxy also. 
        
        
        
        
        //TO DO: get the link variable from the properties/conf file
        Optional<String> pomVersion = null;

        //this is the actual link.
        String link = "https://github.com/matejbludsky/LogWrapper/blob/master/pom.xml";
        URL pomURL;
        HttpURLConnection pomHttp;
        try {
            Properties systemProperties = System.getProperties();
            //systemProperties.setProperty("http.proxyHost","http://proxyconf.wincor-nixdorf.com/");
            //systemProperties.setProperty("http.proxyPort","81");
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxy-pdb.wincor-nixdorf.com", 81));
            pomURL = new URL(link);
            pomHttp = (HttpURLConnection) pomURL.openConnection(proxy);
            pomHttp.connect();

            int responseHeader = pomHttp.getResponseCode();

            
            // check that the answer is correct, we are getting the document
            if (responseHeader == 200) {
                // We know there should not be that many lines before we hit the version tag,
                // but I don't want to risk the tag being in several lines or any other ill format issue
                String response = getStringFromStream(pomHttp);
                String pomVer = getversion(response);

                pomVersion = Optional.of(pomVer);
            }
            return pomVersion;

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;

        }

    }

    private static String getStringFromStream(HttpURLConnection pomHttp) throws IOException {
        // this method gets the whole page that we got from the URL into a String.
        // TO DO: clear up this method
        if (pomHttp != null) {
            // Writer writer = new StringWriter();
            char[] buffer = new char[2048];
            // this is the actual whole page
            StringBuffer page = new StringBuffer();
            
            try (InputStream pomStream = pomHttp.getInputStream(); Reader reader = new BufferedReader(new InputStreamReader(pomStream, "UTF-8"));) {

                // read appending to the stringbuffer so that we can have an "arbitrary" long page.
                while ((reader.read(buffer)) != -1) {
                    // get the char [] appended to the string buffer which is "page"
                    page.append(buffer);
                }
                String resultPage = new String(page);
                
                // we are returning the WHOLE page we got from the URL
                return resultPage;

            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return null;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return null;
            }

        } else {
            return null;
        }
    }

    private static String getversion(String response) {
        //This method gets the version of the application by looking for the tags <version> on the file
              
        
        return null;
    }

    private static Optional<String> getVersionFromMavenFile() {
        // this method extracts the version from the maven.properties file.
        // this code is a copy of the one at the AboutMeJFrame class.
        java.io.InputStream is = VersionHandler.class.getResourceAsStream("/maven.properties");
        java.util.Properties p = new Properties();
        try {
            p.load(is);
            // get the property and return it
            String versionMaven = p.getProperty("logWrapper.version");
            return Optional.of(versionMaven);
        } catch (IOException e) {
            // WE could not get the property, so we have to show the user that the version could not be checked
            e.printStackTrace();
        }
        return null;
    }

    private static boolean continueWithAppNoUpdated() {
        // this method shows a dialog warning the user that the application has a newer version and asks either to
        // continue or stop
        // the current one.

        // display the dialog

        /// get dialog value

        return false;
    }

}
