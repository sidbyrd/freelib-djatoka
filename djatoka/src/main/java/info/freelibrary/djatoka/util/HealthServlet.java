package info.freelibrary.djatoka.util;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Serializer;

public class HealthServlet extends HttpServlet {

    private static final long serialVersionUID = -1456866062313365312L;

    private static final Logger LOGGER = LoggerFactory
	    .getLogger(HealthServlet.class);

    @Override
    protected void doGet(HttpServletRequest aRequest,
	    HttpServletResponse aResponse) throws ServletException, IOException {
	boolean detailedOutput = aRequest.getParameter("detailed") != null;
	ServletOutputStream out = aResponse.getOutputStream();
	Element root = new Element("response");
	Document response = new Document(root);
	Element health = new Element("health");
	Serializer serializer = new Serializer(out);
	Runtime runtime = Runtime.getRuntime();
	long freeMemory = runtime.freeMemory();
	long totalMemory = runtime.totalMemory();
	long usedMemory = totalMemory - freeMemory;
	double percentage = (double) usedMemory / totalMemory;
	String memUsage = String.format("%.2g", percentage);
	int memory;

	serializer.setIndent(2);
	root.appendChild(health);

	memory = (int) (Double.parseDouble(memUsage) * 100);

	if (LOGGER.isDebugEnabled()) {
	    LOGGER.debug("Memory usage at {}%", memory);
	}
	
	// These numbers are just a guess... need some real world tests
	// also, add other things like deadlocked threads, etc?
	if (memory < 85) {
	    health.appendChild("ok");
	}
	else {
	    if (LOGGER.isWarnEnabled()) {
		LOGGER.warn("Memory usage at {}%", memory);
	    }

	    if (memory > 95) {
		health.appendChild("dying");
	    }
	    else {
		health.appendChild("sick");
	    }
	}

	if (detailedOutput) {
	    root.appendChild(getMemoryStats(memory, freeMemory, totalMemory));
	    root.appendChild(getProcessorStats());
	    root.appendChild(getThreadStats());
	}

	serializer.write(response);
	out.close();
    }

    private Element getThreadStats() {
	Element threads = new Element("threads");
	ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();
	int threadCount = mxBean.getThreadCount();
	long[] deadlocked = mxBean.findDeadlockedThreads();
	long startedCount = mxBean.getTotalStartedThreadCount();
	int deadCount = deadlocked != null ? deadlocked.length : 0;
	int peakCount = mxBean.getPeakThreadCount();
	long totalCount = mxBean.getTotalStartedThreadCount();
	Element threadCountElem = new Element("threadCount");
	Element deadlockedCountElem = new Element("deadlockedCount");
	Element peakCountElem = new Element("peakCount");
	Element totalCountElem = new Element("totalCount");
	Element startedCountElem = new Element("totalStartedCount");
	threadCountElem.appendChild(String.valueOf(threadCount));
	deadlockedCountElem.appendChild(String.valueOf(deadCount));
	peakCountElem.appendChild(String.valueOf(peakCount));
	totalCountElem.appendChild(String.valueOf(totalCount));
	startedCountElem.appendChild(String.valueOf(startedCount));

	threads.appendChild(threadCountElem);
	threads.appendChild(deadlockedCountElem);
	threads.appendChild(peakCountElem);
	threads.appendChild(totalCountElem);
	threads.appendChild(startedCountElem);

	return threads;
    }

    private Element getProcessorStats() {
	Runtime runtime = Runtime.getRuntime();
	String processors = Integer.toString(runtime.availableProcessors());
	Element processorsElem = new Element("processors");

	processorsElem.appendChild(processors);
	return processorsElem;
    }

    private Element getMemoryStats(int aMemPct, long aFreeMem, long aTotalMem) {
	long maxMemory = Runtime.getRuntime().totalMemory();
	Element freeMemElem = new Element("freeMem");
	Element totalMemElem = new Element("totalMem");
	Element maxMemElem = new Element("maxMem");
	Element memoryElem = new Element("memory");
	Attribute memPctAtt = new Attribute("pctUsed", String.valueOf(aMemPct));
	int mb = 1024 * 1024;
	String freeMB = Long.toString(aFreeMem / mb);
	String totalMB = Long.toString(aTotalMem / mb);
	String maxMB = Long.toString(maxMemory / mb);

	freeMemElem.appendChild(Long.toString(aFreeMem));
	freeMemElem.addAttribute(new Attribute("mb", freeMB));
	totalMemElem.appendChild(Long.toString(aTotalMem));
	totalMemElem.addAttribute(new Attribute("mb", totalMB));
	maxMemElem.appendChild(Long.toString(maxMemory));
	maxMemElem.addAttribute(new Attribute("mb", maxMB));

	memoryElem.appendChild(freeMemElem);
	memoryElem.appendChild(totalMemElem);
	memoryElem.appendChild(maxMemElem);
	memoryElem.addAttribute(memPctAtt);

	return memoryElem;
    }
}
