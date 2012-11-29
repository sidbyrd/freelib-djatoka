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

package gov.lanl.adore.djatoka.io.reader;

import gov.lanl.adore.djatoka.io.FormatIOException;
import gov.lanl.adore.djatoka.io.IReader;

import info.freelibrary.djatoka.io.PNMImage;

import java.awt.image.BufferedImage;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Returns BufferedImage give a PNM image InputStream or file path.
 * 
 * @author Ryan Chute
 * @author Kevin S. Clarke &lt;<a
 *         href="mailto:ksclarke@gmail.com">ksclarke@gmail.com</a>&gt;
 */
public class PNMReader implements IReader {

	private static Logger LOGGER = LoggerFactory.getLogger(PNMReader.class);

	/**
	 * Returns a BufferedImage instance for provided image file path.
	 * 
	 * @param aFileName absolute file path for image file
	 * @return a <code>BufferedImage</code> instance for source image file
	 * @throws FormatIOException
	 */
	public BufferedImage open(String aFileName) throws FormatIOException {
		try {
			return new PNMImage(aFileName).getBufferedImage();
		}
		catch (IOException details) {
			throw new FormatIOException(details);
		}
	}

	/**
	 * Returns a BufferedImage instance for provided InputStream
	 * 
	 * @param aInputStream an InputStream consisting of an image bitstream
	 * @return a <code>BufferedImage</code> instance for source image InputStream
	 * @throws FormatIOException
	 */
	public BufferedImage open(InputStream aInputStream) throws FormatIOException {
		try {
			return new PNMImage(aInputStream).getBufferedImage();
		}
		catch (IOException details) {
			throw new FormatIOException(details);
		}
	}

}
	