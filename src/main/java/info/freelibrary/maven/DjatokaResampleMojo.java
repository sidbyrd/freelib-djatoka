
package info.freelibrary.maven;

import gov.lanl.adore.djatoka.DjatokaEncodeParam;
import gov.lanl.adore.djatoka.io.FormatIOException;
import gov.lanl.adore.djatoka.io.reader.DjatokaReader;
import gov.lanl.adore.djatoka.io.writer.TIFWriter;
import gov.lanl.adore.djatoka.kdu.KduCompressExe;
import info.freelibrary.djatoka.Constants;
import org.apache.commons.exec.*;
import org.apache.commons.exec.environment.EnvironmentUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.imgscalr.Scalr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Map;
import java.util.Properties;

/**
 * Resamples images for ingest in an attempt to reduce Moiré effects.
 * <p/>
 * 
 * @author <a href="mailto:ksclarke@gmail.com">Kevin S. Clarke</a>
 */
@Mojo(name = "resampled-ingest")
public class DjatokaResampleMojo extends DjatokaIngestMojo {

    private static final Logger LOGGER = LoggerFactory.getLogger(DjatokaResampleMojo.class);

    /**
     * Pixel offset for the resampling.
     */
    @Parameter(property = "pixels", defaultValue = "10")
    private int myPixelCount;

    @Override
    protected File convertToJp2(final File aSource, final DjatokaEncodeParam aParams) throws MojoExecutionException {
        try {
            final String fileName = aSource.getName().replace(".tiff", "").replace(".tif", "");
            final File tmpFile = File.createTempFile(fileName, ".tif");
            final File tmpJp2File = File.createTempFile("djatoka-", Constants.JP2_EXT);
            final Properties properties = myProject.getProperties();
            final String kakadu = properties.getProperty("LD_LIBRARY_PATH");
            final DjatokaEncodeParam params = getEncodingParams();
            final String target = tmpJp2File.getAbsolutePath();

            // The TIFF source image (before and after resampling)
            String source = aSource.getAbsolutePath();
            String command;

            source = resampleImage(new DjatokaReader().open(source), tmpFile);

            if (kakadu == null) {
                throw new MojoExecutionException(BUNDLE.get("INGEST_KAKADU_CFG"));
            }

            // Build the console command we use to do the conversion
            command = KduCompressExe.getKduCompressCommand(source, target, params);
            command = command.replaceFirst("^null/", kakadu + "/");

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(BUNDLE.get("INGEST_CONVERSION_COMMAND"), command);
            }

            final DefaultExecuteResultHandler handler = new DefaultExecuteResultHandler();
            final CommandLine cmdLine = CommandLine.parse(command);
            final DefaultExecutor executor = new DefaultExecutor();
            final ExecuteWatchdog watchdog = new ExecuteWatchdog(60000 * 10);
            final ByteArrayOutputStream error = new ByteArrayOutputStream();
            final ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
            final Map<String, String> environment = EnvironmentUtils.getProcEnvironment();

            if (System.getProperty("os.name").toLowerCase().indexOf("mac") >= 0) {
                environment.put("DYLD_LIBRARY_PATH", kakadu);
            } else {
                environment.put("LD_LIBRARY_PATH", kakadu);
            }

            executor.setStreamHandler(new PumpStreamHandler(stdOut, error));
            executor.setWatchdog(watchdog);
            executor.execute(cmdLine, environment, handler);

            try {
                handler.waitFor();
            } catch (final InterruptedException details) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error(BUNDLE.get("INGEST_INTERRUPTED", aSource, error.toString()));
                }
            }

            if (handler.getExitValue() != 0) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error(BUNDLE.get("INGEST_CONVERSION_FAILED", aSource, error.toString()));
                }

                return null;
            } else {
                return tmpJp2File;
            }
        } catch (final IOException details) {
            throw new MojoExecutionException(details.getMessage(), details);
        } catch (final FormatIOException details) {
            throw new MojoExecutionException(details.getMessage(), details);
        }
    }

    private String resampleImage(final BufferedImage aImage, final File aOutputFile) throws IOException {
        final int max = Math.max(aImage.getWidth(), aImage.getHeight());
        final BufferedImage image = Scalr.resize(aImage, Scalr.Method.ULTRA_QUALITY, max - myPixelCount);
        final FileOutputStream fOutStream = new FileOutputStream(aOutputFile);
        final BufferedOutputStream outStream = new BufferedOutputStream(fOutStream);

        try {
            new TIFWriter().write(image, outStream);
        } catch (final FormatIOException details) {
            throw new IOException(details);
        }

        outStream.close();
        aImage.flush();
        image.flush();

        return aOutputFile.getAbsolutePath();
    }
}
