import com.microsoft.aad.msal4j.*;
import com.azure.core.util.* ;
import java.util.Collections;
import java.util.*;
import java.util.Set;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.CompletableFuture;
import java.lang.ClassCastException ;
import com.microsoft.aad.msal4j.MsalServiceException ;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.DefaultAzureCredentialBuilder;
import oracle.jdbc.AccessToken;
import oracle.jdbc.OracleConnectionBuilder;
//import oracle.jdbc.datasource.OracleDataSource;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.pool.OracleDataSource;
import java.sql.DatabaseMetaData;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
//import com.azure.identity.DefaultAzureCredentialBuilder;
//import com.azure.identity.TokenCredential;
// import com.azure.identity.AuthenticationResult;
//import com.microsoft.identity.client ;
public class GetTokens4ADB {
static IAuthenticationResult authResult ;
static  AccessToken jdbcToken ;

    
    public static void writeToDirectory(String directoryPath, String fileName, String content) {
        try {
            File directory = new File(directoryPath);

            // Create the directory if it doesn't exist
            if (!directory.exists()) {
                directory.mkdirs(); // mkdirs() will create parent directories if needed
            }

            File file = new File(directory, fileName);

            // If the file exists, overwrite it; otherwise, create a new file
            FileOutputStream outputStream = new FileOutputStream(file, false); // Pass 'true' to append instead of overwrite
            outputStream.write(content.getBytes());
            outputStream.close();

            System.out.println("Content has been written to the file.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void connectToOracleWithOAuth() {
        String DB_URL = "jdbc:oracle:thin:@(description=(retry_count=2)(retry_delay=3)(address=(protocol=tcps)(port=1521)(host=adb.us-phoenix-1.oraclecloud.com))(connect_data=(service_name=bk8uwrvkgqzvi2h_oauth_low.adb.oraclecloud.com))(security=(TOKEN_AUTH=OAUTH)(TOKEN_LOCATION=C:/Users/opc/jdbc-test-project/tocken)))";
        Properties properties = new Properties();
        // Connection property to enable IAM token authentication.
        properties.put(OracleConnection.CONNECTION_PROPERTY_TOKEN_AUTHENTICATION, "OAUTH");
        
        try {
            OracleDataSource ods = new OracleDataSource();

            ods.setURL(DB_URL);
            ods.setConnectionProperties(properties);

            try (OracleConnection connection = (OracleConnection) ods.getConnection()) {
                // Get the JDBC driver name and version
                DatabaseMetaData dbmd = connection.getMetaData();
                System.out.println("Driver Name: " + dbmd.getDriverName());
                System.out.println("Driver Version: " + dbmd.getDriverVersion());
                // Print some connection properties
                System.out.println("Default Row Prefetch Value is: " + connection.getDefaultRowPrefetch());
                System.out.println("Database GLOBAL OAuth Username is: " + connection.getUserName());
                System.out.println();
                // Perform a database operation
                // create the statement object
                Statement stmt = connection.createStatement();

                // execute query
                ResultSet rs = stmt.executeQuery("SELECT 1, 'aaa', 'passing token' FROM dual");
                while (rs.next())
                    System.out.println(rs.getInt(1) + "  " + rs.getString(2) + "  " + rs.getString(3));

            } catch (SQLException e) {
                System.out.println("SQL Exception: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("Error creating data source or setting properties: " + e.getMessage());
        }
    }


    public static void main(String[] args) {
        String clientId = "f080bd12-c7d2-4acb-b8ab-5c8fa2cf5c40";
        String tenantId = "5b743bc7-c1e2-4d46-b4b5-a32eddac0286";
        String clientSecret = "fJ-8Q~hQmDSfhCXsHYqX0WR~CdLoeL5pWiCKXcKe";
       // String[] scopes = {"https://oracledevelopment.onmicrosoft.com/aa04583a-fa70-4bbd-8f62-ec4ec80213df/.default"};
        String scopes = "https://oracledevelopment.onmicrosoft.com/aa04583a-fa70-4bbd-8f62-ec4ec80213df/.default";
        String authority = "https://login.microsoftonline.com/" + tenantId;
         ConfidentialClientApplication app;
        try {
            app = ConfidentialClientApplication.builder(clientId, ClientCredentialFactory.createFromSecret(clientSecret))
                    .authority(authority)
                    .build();
        } catch (Exception  e) {
            System.out.println("Invalid authority URL");
            return;
        }
        Set<String> scopeSet = Set.of(scopes);
        //ClientCredentialParameters clientCredentialParam = ClientCredentialParameters.builder(scopeSet).build();
        ClientCredentialParameters clientCredentialParam = ClientCredentialParameters.builder(Collections.singleton(scopes)).build();
        CompletableFuture<IAuthenticationResult> future = app.acquireToken(clientCredentialParam);
        //IAuthenticationResult result ;
        System.out.println("Access token:\n");
        try {
             IAuthenticationResult result = future.get();
             result =  future.get();
            System.out.println("Access token:\n");
            System.out.println(result.accessToken());
        String directoryPath = "C:\\Users\\opc\\jdbc-test-project\\";
        String fileName = "tocken";
        String content = result.accessToken() ;

    // Wite the token into   TOKEN_LOCATION in the jdbc URL  
    writeToDirectory(directoryPath, fileName, content); 

    // Connect to ADB using the TOKEN_LOCATION in the jdbc url.
    connectToOracleWithOAuth() ;

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
