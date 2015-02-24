/*
 * Copyright (c) 2008  Los Alamos National Security, LLC.
 *
 * Los Alamos National Laboratory
 * Research Library
 * Digital Library Research & Prototyping Team
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 * 
 */

package gov.lanl.adore.djatoka.openurl;

import gov.lanl.adore.djatoka.DjatokaEncodeParam;
import gov.lanl.adore.djatoka.DjatokaException;
import gov.lanl.adore.djatoka.ICompress;
import gov.lanl.adore.djatoka.IExtract;
import gov.lanl.adore.djatoka.io.FormatConstants;
import gov.lanl.adore.djatoka.kdu.KduCompressExe;
import gov.lanl.adore.djatoka.kdu.KduExtractExe;
import gov.lanl.adore.djatoka.util.IOUtils;
import gov.lanl.adore.djatoka.util.ImageProcessingUtils;
import gov.lanl.adore.djatoka.util.ImageRecord;
import info.freelibrary.util.PairtreeObject;
import info.freelibrary.util.PairtreeRoot;
import info.freelibrary.util.PairtreeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Utility class used to harvest URIs and compress files into JP2.
 * 
 * @author Ryan Chute
 * @author <a href="mailto:ksclarke@gmail.com">Kevin S. Clarke</a>
 */
public class DjatokaImageMigrator implements FormatConstants, IReferentMigrator {

    private static Logger LOGGER = LoggerFactory.getLogger(DjatokaImageMigrator.class);

    private final List<String> processing = java.util.Collections.synchronizedList(new LinkedList<String>());

    private HashMap<String, String> formatMap;

    private File myPtRootDir;

    /**
     * Constructor. Initialized formatMap with common extension suffixes
     */
    public DjatokaImageMigrator() {
        formatMap = new HashMap<String, String>();
        formatMap.put(FORMAT_ID_JPEG, FORMAT_MIMEYPE_JPEG);
        formatMap.put(FORMAT_ID_JP2, FORMAT_MIMEYPE_JP2);
        formatMap.put(FORMAT_ID_PNG, FORMAT_MIMEYPE_PNG);
        formatMap.put(FORMAT_ID_PNM, FORMAT_MIMEYPE_PNM);
        formatMap.put(FORMAT_ID_TIFF, FORMAT_MIMEYPE_TIFF);
        // Additional Extensions
        formatMap.put(FORMAT_ID_JPG, FORMAT_MIMEYPE_JPEG);
        formatMap.put(FORMAT_ID_TIF, FORMAT_MIMEYPE_TIFF);
        // Additional JPEG 2000 Extensions
        formatMap.put(FORMAT_ID_J2C, FORMAT_MIMEYPE_JP2);
        formatMap.put(FORMAT_ID_JPC, FORMAT_MIMEYPE_JP2);
        formatMap.put(FORMAT_ID_J2K, FORMAT_MIMEYPE_JP2);
        formatMap.put(FORMAT_ID_JPF, FORMAT_MIMEYPE_JPX);
        formatMap.put(FORMAT_ID_JPX, FORMAT_MIMEYPE_JPX);
        formatMap.put(FORMAT_ID_JPM, FORMAT_MIMEYPE_JPM);
    }

    /**
     * Sets the pairtree root for the migrator.
     * 
     * @param aPtRootDir The Pairtree root directory
     */
    @Override
    public void setPairtreeRoot(final File aPtRootDir) {
        myPtRootDir = aPtRootDir;
    }

    /**
     * Gets the pairtree root for the migrator.
     * 
     * @return The Pairtree root directory
     */
    @Override
    public File getPairtreeRoot() {
        return myPtRootDir;
    }

    /**
     * Returns true if the migrator has a Pairtree root directory; else, false.
     * 
     * @return True if the migrator has a Pairtree root directory; else, false
     */
    @Override
    public boolean hasPairtreeRoot() {
        return myPtRootDir != null;
    }

    /**
     * Returns a delete on exit File object for a provide URI
     * 
     * @param aReferent the identifier for the remote file
     * @param aURI the URI of an image to be downloaded and compressed as JP2
     * @return File object of JP2 compressed image
     * @throws DjatokaException
     */
    @Override
    public File convert(final String aReferent, final URI aURI) throws DjatokaException {
        File file = null;

        // Add the request to the list of in-process requests
        processing.add(aReferent);

        try {
            // If the referent is not the URL, we've been able to parse an ID
            // out from the URL; if we did that, we assume it's already a JP2
            boolean isJp2 = !aReferent.equals(aURI.toString());

            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Processing remote {}: {}", isJp2 ? "JP2 file" : "URI", aURI);
            }

            // Obtain remote resource
            final URL url = aURI.toURL();
            InputStream source = IOUtils.getInputStream(url);

            // If we know it's JP2 at this point, it's because it's been passed
            // in as one of our parsable URLs.
            if (isJp2 && myPtRootDir != null) {
                final PairtreeRoot pairtree = new PairtreeRoot(myPtRootDir);
                final PairtreeObject dir = pairtree.getObject(aReferent);
                final String filename = PairtreeUtils.encodeID(aReferent);
                FileOutputStream destination;
                boolean result;

                file = new File(dir, filename);
                destination = new FileOutputStream(file);

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Remote stream is{}accessible: {}", source.available() > 0 ? " " : " not ", url);
                }

                // FIXME: For some reason, it's taking two requests sometimes
                if (source.available() == 0) {
                    source = IOUtils.getInputStream(url);

                    if (LOGGER.isDebugEnabled() && source.available() > 0) {
                        LOGGER.debug("Hey, I checked again and found it!");
                    }
                }

                result = IOUtils.copyStream(source, destination);

                if (LOGGER.isDebugEnabled() && source.available() > 0 && result) {
                    LOGGER.debug("Stored retrieved JP2 into Pairtree FS: {}", file.getAbsolutePath());
                }

                source.close();
                destination.close();

                // Clean up the file stub of unsuccessful copies
                if (file.length() == 0) {
                    if (!file.delete() && LOGGER.isWarnEnabled()) {
                        LOGGER.warn("File not deleted: {}", file);
                    }
                }
            } else {
                final int extIndex = url.toString().lastIndexOf(".") + 1;
                String ext = url.toString().substring(extIndex).toLowerCase();
                final int hash = aURI.hashCode();

                if (ext.equals(FORMAT_ID_TIF) || ext.equals(FORMAT_ID_TIFF)) {
                    ext = "." + FORMAT_ID_TIF;
                    file = File.createTempFile("convert" + hash, ext);
                } else if (formatMap.containsKey(ext) &&
                        (formatMap.get(ext).equals(FORMAT_MIMEYPE_JP2) || formatMap.get(ext).equals(
                                FORMAT_MIMEYPE_JPX))) {
                    file = File.createTempFile("cache" + hash, "." + ext);
                    isJp2 = true;
                } else {
                    if (source.markSupported()) {
                        source.mark(15);
                    }

                    if (ImageProcessingUtils.checkIfJp2(source)) {
                        ext = "." + FORMAT_ID_JP2;
                        file = File.createTempFile("cache" + hash, ext);
                    }

                    if (source.markSupported()) {
                        source.reset();
                    } else { // close and reopen the stream
                        source.close();
                        source = IOUtils.getInputStream(url);
                    }
                }

                if (file == null) {
                    file = File.createTempFile("convert" + hash, ".img");
                }

                file.deleteOnExit();

                final FileOutputStream destination = new FileOutputStream(file);
                IOUtils.copyStream(source, destination);

                // Process Image
                if (!isJp2) {
                    file = processImage(file, aURI);
                }

                // Clean-up
                source.close();
                destination.close();
            }

            return file;
        } catch (final Exception details) {
            throw new DjatokaException(details.getMessage(), details);
        } finally {
            if (processing.contains(aReferent)) {
                processing.remove(aReferent);
            }
        }
    }

    /**
     * Returns a delete on exit File object for a provide URI
     * 
     * @param img File object on local image to be compressed
     * @param uri the URI of an image to be compressed as JP2
     * @return File object of JP2 compressed image
     * @throws DjatokaException
     */
    @Override
    public File processImage(File img, final URI uri) throws DjatokaException {
        final String imgPath = img.getAbsolutePath();
        final String fmt = formatMap.get(imgPath.substring(imgPath.lastIndexOf('.') + 1).toLowerCase());
        try {
            if (fmt == null || !ImageProcessingUtils.isJp2Type(fmt)) {
                final ICompress jp2 = new KduCompressExe();
                final File jp2Local = File.createTempFile("cache" + uri.hashCode() + "-", ".jp2");
                if (!jp2Local.delete() && LOGGER.isWarnEnabled()) {
                    LOGGER.warn("File not deleted: {}", jp2Local);
                }
                jp2.compressImage(img.getAbsolutePath(), jp2Local.getAbsolutePath(), new DjatokaEncodeParam());
                if (!img.delete() && LOGGER.isWarnEnabled()) {
                    LOGGER.warn("File not deleted: {}", img);
                }
                img = jp2Local;
            } else {
                try {
                    final IExtract ex = new KduExtractExe();
                    ex.getMetadata(new ImageRecord(uri.toString(), img.getAbsolutePath()));
                } catch (final DjatokaException e) {
                    throw new DjatokaException("Unknown JP2/JPX file format");
                }
            }
        } catch (final Exception e) {
            throw new DjatokaException(e.getMessage(), e);
        }
        return img;
    }

    /**
     * Return a unmodifiable list of images currently being processed. Images are removed once complete.
     * 
     * @return list of images being processed
     */
    @Override
    public List<String> getProcessingList() {
        return processing;
    }

    /**
     * Returns map of format extension (e.g. jpg) to mime-type mappings (e.g. image/jpeg)
     * 
     * @return format extension to mime-type mappings
     */
    @Override
    public HashMap<String, String> getFormatMap() {
        return formatMap;
    }

    /**
     * Sets map of format extension (e.g. jpg) to mime-type mappings (e.g. image/jpeg)
     * 
     * @param formatMap extension to mime-type mappings
     */
    @Override
    public void setFormatMap(final HashMap<String, String> formatMap) {
        this.formatMap = formatMap;
    }

}
