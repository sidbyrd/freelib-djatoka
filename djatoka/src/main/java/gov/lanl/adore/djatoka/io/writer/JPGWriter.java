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

package gov.lanl.adore.djatoka.io.writer;

import gov.lanl.adore.djatoka.io.FormatIOException;
import gov.lanl.adore.djatoka.io.IWriter;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Properties;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JPG File Writer. Uses ImageIO to write BufferedImage as JPG
 * 
 * @author Ryan Chute
 * @author Kevin S. Clarke &lt;<a
 *         href="mailto:ksclarke@gmail.com">ksclarke@gmail.com</a>&gt;
 * 
 */
public class JPGWriter implements IWriter {

	private static Logger LOGGER = LoggerFactory.getLogger(JPGWriter.class);

	public static final int DEFAULT_QUALITY_LEVEL = 90;

	private int q = DEFAULT_QUALITY_LEVEL;

	/**
	 * Write a BufferedImage instance using implementation to the provided
	 * OutputStream.
	 * 
	 * @param bi a BufferedImage instance to be serialized
	 * @param os OutputStream to output the image to
	 * @throws FormatIOException
	 */
	public void write(BufferedImage aImage, OutputStream aOutStream)
			throws FormatIOException {
		Iterator<ImageWriter> iterator = ImageIO
				.getImageWritersByFormatName("jpeg");

		try {
			ImageWriter jpgWriter = iterator.next();
			ImageWriteParam iwp = jpgWriter.getDefaultWriteParam();

			iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			iwp.setCompressionQuality((float) (q / 100.0));
			
			jpgWriter.setOutput(ImageIO.createImageOutputStream(aOutStream));
			jpgWriter.write(null, new IIOImage(aImage, null, null), iwp);
			jpgWriter.dispose();
		}
		catch (IOException details) {
			throw new FormatIOException(details);
		}
	}

	/**
	 * Set the Writer Implementations Serialization properties. Only
	 * JPGWriter.quality_level is supported in this implementation.
	 * 
	 * @param props writer serialization properties
	 */
	public void setWriterProperties(Properties aProps) {
		if (aProps.containsKey("JPGWriter.quality_level")) {
			q = Integer
					.parseInt((String) aProps.get("JPGWriter.quality_level"));
		}
	}
}
