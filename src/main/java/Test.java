import com.datastax.driver.core.RemoteEndpointAwareJdkSSLOptions;
import com.datastax.driver.core.SSLOptions;
import org.springframework.data.cassandra.config.CassandraClusterFactoryBean;

import javax.net.ssl.*;
import java.io.File;
import java.security.KeyStore;
import java.security.SecureRandom;

public class Test {
    private static final String KEYSTORE_PATH = "path to your ideintity.jks";
    private static final char[] KEYSTORE_PASSWORD = "keystore password from config.json".toCharArray();
    private static final String TRUSTSTORE_PATH = "path to your truststore.jks";
    private static final char[] TRUSTSTORE_PASSWORD ="truststore password from config.json".toCharArray();
    private static final String contactPoints = "host dns from cqlshrc";
    private static final int port = 30000; // "port from cqlshrc Note do not use the one from config.json";
    private static final boolean sslEnabled = true;
    private static final String username = "db username";
    private static final String password = "db password";

    @Override
    public CassandraClusterFactoryBean cluster(){

        CassandraClusterFactoryBean cluster = new CassandraClusterFactoryBean();

        cluster.setJmxReportingEnabled(false);
        cluster.setContactPoints(contactPoints);
        cluster.setPort(port);
        cluster.setSslEnabled(sslEnabled);
        cluster.setContactPoints(contactPoints);
        cluster.setUsername(username);
        cluster.setPassword(password);
        cluster.setSslOptions(generateSSLConf());

        return cluster;
    }
    public SSLOptions generateSSLConf() {
        try {
            KeyManagerFactory kmf;
            try {
                kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                kmf.init(KeyStore.getInstance(KEYSTORE_PATH), KEYSTORE_PASSWORD);
            } catch (Exception e) {
                throw new RuntimeException("Unable to init KeyManagerFactory. Please check password and location.", e);
            }

            KeyStore truststore;
            try {
                truststore = KeyStore.getInstance(new File(TRUSTSTORE_PATH),TRUSTSTORE_PASSWORD);
            } catch (Exception e) {
                throw new RuntimeException("Unable to load the truststore. Check path and password.", e);
            }
            TrustManagerFactory tmf;
            try {
                tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                tmf.init(truststore);
            } catch (Exception e) {
                throw new RuntimeException("Unable to init TrustManagerFactory.", e);
            }

            try {
                SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
                sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());
                return RemoteEndpointAwareJdkSSLOptions.builder()
                        .withSSLContext(sslContext)
                        .build();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } catch (Throwable th) {
            throw new RuntimeException("Failed to load truststore for casandra", th);
        }
    }
}
