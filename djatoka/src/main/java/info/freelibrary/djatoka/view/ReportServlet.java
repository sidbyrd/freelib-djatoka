package info.freelibrary.djatoka.view;

import gov.lanl.adore.djatoka.util.IOUtils;

import info.freelibrary.djatoka.Constants;
import info.freelibrary.util.FileUtils;
import info.freelibrary.util.RegexFileFilter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nu.xom.ValidityException;
import nu.xom.XPathContext;

import org.im4java.core.CommandException;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
import org.im4java.core.IdentifyCmd;
import org.im4java.core.ImageCommand;
import org.im4java.process.OutputConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReportServlet extends HttpServlet implements Constants {

    /**
     * Generated <code>serialVersionUID</code>.
     */
    private static final long serialVersionUID = 1123517462247013351L;

    private static final String JETTY_XPATH = "//maven:connector[@implementation='org.eclipse.jetty.server.nio.SelectChannelConnector']";
    private static final String MAVEN_NS = "http://maven.apache.org/POM/4.0.0";
    private static final XPathContext XPATH_CONTEXT = new XPathContext("maven",
	    MAVEN_NS);

    private static final Logger LOGGER = LoggerFactory
	    .getLogger(ReportServlet.class);

    private Properties myProps;

    private String mySourceURL;

    @Override
    protected void doGet(HttpServletRequest aRequest,
	    HttpServletResponse aResponse) throws ServletException, IOException {
	File tifDir = new File(myProps.getProperty(TIFF_DATA_DIR));
	File jp2Dir = new File(myProps.getProperty(JP2_DATA_DIR));
	String[] dirPaths = new String[] { jp2Dir.getAbsolutePath(),
		tifDir.getAbsolutePath() };
	PrintWriter toBrowser = aResponse.getWriter();
	Builder bob = new Builder();

	try {
	    // Assuming we're running from Jetty/Maven plugin
	    Document pom = bob.build(new File("pom.xml"));
	    Nodes nodes = pom.query(JETTY_XPATH, XPATH_CONTEXT);
	    String port;

	    if (nodes.size() == 1) {
		Element cElem = (Element) nodes.get(0);
		Element pElem = cElem.getFirstChildElement("port", MAVEN_NS);

		Integer.parseInt(port = pElem.getValue()); // check for int
		mySourceURL = "http://localhost:" + port + "/view/";
	    }
	    else {
		mySourceURL = "http://localhost:8080/view/"; // default
	    }

	    toBrowser.print("<html>");
	    toBrowser.print(getHead().toXML());
	    toBrowser.print("<body>");

	    print("", dirPaths, toBrowser);

	    toBrowser.print("</body></html>");
	    toBrowser.close();
	}
	catch (ValidityException details) {
	    throw new ServletException(details);
	}
	catch (ParsingException details) {
	    throw new ServletException(details);
	}
	catch (NumberFormatException details) {
	    throw new ServletException("POM file's port not a valid number");
	}
    }

    @Override
    public void init() throws ServletException {
	String dir = getServletContext().getRealPath("/WEB-INF/classes") + "/";
	String propertiesFile = dir + PROPERTIES_FILE;

	try {
	    myProps = IOUtils.loadConfigByPath(propertiesFile);

	    if (LOGGER.isDebugEnabled()) {
		LOGGER.debug("Loaded properties file: {}", propertiesFile);
	    }
	}
	catch (Exception details) {
	    throw new ServletException(details);
	}
    }

    @Override
    public void log(String aMessage, Throwable aThrowable) {
	super.log(aMessage, aThrowable);
    }

    @Override
    public void log(String aMessage) {
	super.log(aMessage);
    }

    private void print(String aPath, String[] aPathsArray, PrintWriter aOut)
	    throws MalformedURLException, ValidityException, ParsingException,
	    IOException {
	URL url = new URL(mySourceURL + aPath);
	HttpURLConnection conx = (HttpURLConnection) url.openConnection();
	Document doc = new Builder().build(conx.getInputStream());
	Element pathElem = (Element) doc.query("//path").get(0);
	Elements pathParts = pathElem.getChildElements("part");
	String suppliedPath = aPath.equals("") ? "/" : aPath;
	StringBuilder path = new StringBuilder("/");
	Nodes dirElems = doc.query("//dir");
	Nodes fileElems = doc.query("//file");

	aOut.print("<div>" + makeLink(suppliedPath) + "</div>");
	aOut.print("<div>");

	for (int index = 0; index < fileElems.size(); index++) {
	    Element fileElem = (Element) fileElems.get(index);
	    Attribute fileName = fileElem.getAttribute("name");
	    String value = fileName.getValue();

	    value += getStatus(new File(aPathsArray[0] + aPath, value));
	    aOut.print(getDiv(value).toXML());
	}

	System.out.println(doc.toXML());

	for (int index = 0; index < pathParts.size(); index++) {
	    path.append(pathParts.get(index).getValue()).append('/');
	}

	if (dirElems.size() > 0) {
	    for (int index = 0; index < dirElems.size(); index++) {
		Element dirElem = (Element) dirElems.get(index);
		Attribute dirName = dirElem.getAttribute("name");
		String newPath = path.toString() + dirName.getValue() + "/";

		print(newPath, aPathsArray, aOut);
	    }
	}

	aOut.print("</div>");
	aOut.flush();
    }

    private Element getDiv(String aText) {
	return getDiv(aText, null);
    }

    private Element getDiv(String aText, String aClass) {
	Element div = new Element("div");
	div.appendChild(aText);

	if (aClass != null && !aClass.equals("")) {
	    Attribute clattr = new Attribute("class", aClass);
	    div.addAttribute(clattr);
	}

	return div;
    }

    private Element getHead() {
	Element head = new Element("head");
	Element title = new Element("title");
	Element style = new Element("style");
	
	title.appendChild("djatoka ingest report");
	
	style.addAttribute(new Attribute("type", "text/css"));
	style.appendChild("a:link{color:blue;text-decoration:none;}");
	style.appendChild("a:visited{color:blue;text-decoration:none;}");
	style.appendChild("a:active{color:blue;text-decoration:none;}");
	style.appendChild("a:hover{color:blue;text-decoration:none;}");
	style.appendChild(".fail{color:red;}");
	style.appendChild(".success{color:green;}");
	style.appendChild("div{padding-left:10px;}");
	
	head.appendChild(title);
	head.appendChild(style);
	
	return head;
    }
    
    private String getStatus(File aFile) {
	final StringBuilder result = new StringBuilder(" ");
	String jp2Path = aFile.getAbsolutePath();
	String tifPath = getTiffFileName(jp2Path);
	System.out.println(jp2Path + " " + aFile.exists());
	System.out.println(tifPath);

	
	
	try {
	    IdentifyCmd identify = new IdentifyCmd();
	    IMOperation op = new IMOperation();

	    identify.setOutputConsumer(new OutputConsumer() {

		public void consumeOutput(InputStream aStream)
			throws IOException {
		    InputStreamReader isReader = new InputStreamReader(aStream);
		    BufferedReader reader = new BufferedReader(isReader);
		    String line;

		    while ((line = reader.readLine()) != null) {
			result.append(line);
		    }
		}

	    });

	    op.addImage(jp2Path);
	    identify.run(op);

	    System.out.println(result.toString());
	    result.delete(0, result.length());
	}
	catch (CommandException details) {
	    System.err.println("Details abbreviated ImageMagick not installed");
	}
	catch (IM4JavaException details) {
	    System.out.println(details);
	}
	catch (InterruptedException details) {
	    System.out.println(details);
	}
	catch (IOException details) {
	    System.out.println(details);
	}

	return result.toString();
    }

    private String makeLink(String aPath) {
	String url = "/view" + aPath;
	String onText = "window.open('" + url + "'); return false";
	Attribute onclick = new Attribute("onclick", onText);
	Attribute onKeyPress = new Attribute("onkeypress", onText);
	Element a = new Element("a");

	a.addAttribute(new Attribute("href", url));
	a.appendChild(aPath);
	a.addAttribute(onclick);
	a.addAttribute(onKeyPress);

	return a.toXML();
    }

    private String getTiffFileName(String aJp2FileName) {
	if (aJp2FileName.endsWith(JP2_EXT)) {
	    int end = aJp2FileName.lastIndexOf(JP2_EXT) + 1;
	    return aJp2FileName.substring(0, end) + TIF_EXTS[0];
	}

	return aJp2FileName;
    }
}
