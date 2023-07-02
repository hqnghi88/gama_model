/*******************************************************************************************************
 *
 * IGamaFileMetaData.java, in msi.gama.core, is part of the source code of the
 * GAMA modeling and simulation platform (v.1.9.2).
 *
 * (c) 2007-2023 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package msi.gama.util.file;

/**
 * Class IGamaFileInfo. Provides meta-information on files (like crs and envelope for shapefiles, number of rows/columns
 * for csv files, etc. Used in the UI for decorating files in the navigator. Will be used also to accelerate the loading
 * of files in GAMA, when it can be retrieved ( i.e. when we are in a workspace context).
 * 
 * @author drogoul
 * @since 11 févr. 2015
 * 
 */
public interface IGamaFileMetaData {

	/**
	 * A delimiter character for separating attributes in the property string
	 */
	final static public String DELIMITER = "_!_";
	
	/** The Constant SUB_DELIMITER. */
	final static public String SUB_DELIMITER = "@%@";
	
	/** The Constant FAILED. */
	public final static String FAILED = "failed";
	
	/** The Constant SUFFIX_DEL. */
	public final static String SUFFIX_DEL = " | ";

	/**
	 * Gets the modification stamp.
	 *
	 * @return the modification stamp
	 */
	long getModificationStamp();

	/**
	 * Indicates a failure in the computation of metadata
	 * 
	 * @return
	 */
	boolean hasFailed();

	/**
	 * Never returns null
	 * 
	 * @return the suffix to use for decorating files in the navigator
	 */
	String getSuffix();

	/**
	 * Append suffix.
	 *
	 * @param sb the sb
	 */
	void appendSuffix(StringBuilder sb);

	/**
	 * Returns a thumbnail (imageDescriptor or anything else) or null if no image are provided
	 * 
	 * @return an image (ImageDescriptor, BufferedImage, ...) or null
	 */
	public Object getThumbnail();

	/**
	 * Returns a string that can be stored in the metadata part of the workspace. The implementing classes should also
	 * allow instantiating with this string as an input
	 * 
	 * @return a string describing completely the attributes of this metadata or null
	 */
	public String toPropertyString();

	/**
	 * Returns a string that can be displayed in hover info
	 * 
	 * @return
	 */
	public String getDocumentation();

	/**
	 * @param modificationStamp
	 */
	void setModificationStamp(long modificationStamp);

}
