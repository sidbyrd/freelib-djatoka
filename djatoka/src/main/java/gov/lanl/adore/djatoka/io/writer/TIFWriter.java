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

import ij.ImagePlus;
import ij.io.FileInfo;
import ij.io.TiffEncoder;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TIF File Writer.
 * 
 * @author Ryan Chute
 * @author Kevin S. Clarke <a
 *         href="mailto:ksclarke@gmail.com">ksclarke@gmail.com</a>
 * 
 */
public class TIFWriter implements IWriter {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(TIFWriter.class);

	/**
	 * Write a BufferedImage instance to the provided OutputStream.
	 * 
	 * @param aImage A BufferedImage instance to be serialized
	 * @param aOutputStream OutputStream to output the image to
	 * @throws FormatIOException
	 */
	public void write(BufferedImage aImage, OutputStream aOutStream)
			throws FormatIOException {
		ImagePlus imagePlus = new ImagePlus("tempTif", aImage);
		TiffEncoder encoder = new TiffEncoder(imagePlus.getFileInfo());
		BufferedOutputStream bufStream = new BufferedOutputStream(aOutStream);
		DataOutputStream out = new DataOutputStream(bufStream);

		try {
			encoder.write(out);
		}
		catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			throw new FormatIOException(e.getMessage(), e);
		}
	}

	/**
	 * NOT SUPPORTED. TODO: Add support for key TIFF properties
	 */
	public void setWriterProperties(Properties props) {
	}
}
