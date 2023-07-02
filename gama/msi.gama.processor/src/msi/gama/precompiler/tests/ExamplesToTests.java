/*******************************************************************************************************
 *
 * ExamplesToTests.java, in msi.gama.processor, is part of the source code of the
 * GAMA modeling and simulation platform (v.1.9.2).
 *
 * (c) 2007-2023 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package msi.gama.precompiler.tests;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import msi.gama.precompiler.Constants;
import msi.gama.precompiler.GamaProcessor;
import msi.gama.precompiler.ProcessorContext;
import msi.gama.precompiler.doc.utils.XMLElements;

/**
 * The Class ExamplesToTests.
 */
public class ExamplesToTests implements XMLElements {

	/** The Constant ATT_NAME_FILE. */
	public static final String ATT_NAME_FILE = "fileName";
	
	/** The Constant factoryT. */
	static final TransformerFactory factoryT = TransformerFactory.newInstance();
	
	/** The Constant OPERATORS_XSL. */
	static final URL OPERATORS_XSL = GamaProcessor.class.getClassLoader()
			.getResource("msi/gama/precompiler/resources/testGaml-Operators-xml2test.xsl");
	
	/** The Constant OPERATORS_TRANSFORMER. */
	static final Transformer OPERATORS_TRANSFORMER;
	
	/** The Constant NAME_OPERATOR. */
	static final HashMap<String, String> NAME_OPERATOR = new HashMap<String, String>() {
		{
			put("*", "Multiply");
			put("-", "Minus");
			put("/", "Divide");
			put("+", "Plus");
			put("^", "Power");
			put("!=", "Different");
			put("<>", "Different2");
			put("<", "LT");
			put("<=", "LE");
			put(">", "GT");
			put(">=", "GE");
			put("=", "Equals");
			put(":", "ELSEoperator");
			put("!", "NOunary");
			put("?", "IFoperator");
			put("::", "DoublePoint");
			put("@", "Arobase");
			put(".", "PointAcces");
		}
	};

	static {
		Transformer transformer = null;
		try (final InputStream xsl = OPERATORS_XSL.openStream();) {
			final StreamSource stylesource = new StreamSource(xsl);
			transformer = factoryT.newTransformer(stylesource);
		} catch (final TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (final IOException e2) {
			e2.printStackTrace();
		}
		if (transformer != null) {
			transformer.setOutputProperty(OutputKeys.METHOD, "text");
		}
		OPERATORS_TRANSFORMER = transformer;
	}

	/**
	 * Creates the tests.
	 *
	 * @param context the context
	 * @param doc the doc
	 */
	public static void createTests(final ProcessorContext context, final Document doc) {
		if (doc == null || !doc.hasChildNodes()) { return; }
		final Document document = cleanDocumentTest(doc);
		createOperatorsTests(context, document, OPERATORS_TRANSFORMER);
	}

	/**
	 * List.
	 *
	 * @param nl the nl
	 * @return the list
	 */
	private static List<org.w3c.dom.Element> list(final NodeList nl) {
		if (nl.getLength() == 0) { return Collections.EMPTY_LIST; }
		final List<org.w3c.dom.Element> result = new ArrayList<>();
		for (int i = 0; i < nl.getLength(); i++) {
			result.add((org.w3c.dom.Element) nl.item(i));
		}
		return result;
	}

	/** The Constant documents. */
	final static Map<String, Document> documents = new HashMap<>();

	/**
	 * Creates the operators tests.
	 *
	 * @param context the context
	 * @param document the document
	 * @param xsl the xsl
	 */
	private static void createOperatorsTests(final ProcessorContext context, final Document document,
			final Transformer xsl) {
		final List<org.w3c.dom.Element> categories =
				list(((org.w3c.dom.Element) document.getElementsByTagName(OPERATORS_CATEGORIES).item(0))
						.getElementsByTagName(CATEGORY));
		final List<org.w3c.dom.Element> operators = list(document.getElementsByTagName(OPERATOR));

		for (final org.w3c.dom.Element categoryElement : categories) {
			final String category = categoryElement.getAttribute(ATT_CAT_ID);
			final String nameFileSpecies = categoryElement.getAttribute("id");
			final Document tempDocument = context.getBuilder().newDocument();
			String fileName = nameFileSpecies + ".experiment";
			context.emitWarning(fileName);
			documents.put(fileName, tempDocument);
			final org.w3c.dom.Element root = tempDocument.createElement(DOC);
			root.setAttribute(ATT_NAME_FILE, nameFileSpecies);
			final org.w3c.dom.Element rootOperators = tempDocument.createElement(OPERATORS);

			for (final org.w3c.dom.Element operatorElement : operators) {
				if (operatorElement.hasAttribute("HAS_TESTS")) {
					// OPTION 1 - PRODUCE ALL TESTS, EVEN IF THERE ARE DUPLICATES IN SOME CATEGORIES
					// list(operatorElement.getElementsByTagName(CATEGORY)).stream()
					// .filter(o -> o.getAttribute("id").equals(category))
					// .map(o -> tempDocument.importNode(operatorElement.cloneNode(true), true))
					// .forEach(o -> rootOperators.appendChild(o));
					// OPTION 2 - PRODUCE EACH TEST ONLY ONCE IN ITS FIRST CATEGORY
					list(operatorElement.getElementsByTagName(CATEGORY)).stream()
							.filter(o -> o.getAttribute("id").equals(category))
							.map(o -> tempDocument.importNode(operatorElement.cloneNode(true), true)).forEach(each -> {
								rootOperators.appendChild(each);
								operatorElement.removeAttribute("HAS_TESTS");
							});
				}
			}
			if (rootOperators.hasChildNodes()) {
				root.appendChild(rootOperators);
				tempDocument.appendChild(root);
			} else {
				context.emitWarning("No tests found. Removing :" + fileName);
				documents.remove(fileName);
			}
		}
		transformDocuments(context, xsl);

	}

	/**
	 * Transform documents.
	 *
	 * @param context the context
	 * @param transformer the transformer
	 */
	public static void transformDocuments(final ProcessorContext context, final Transformer transformer) {

		documents.forEach((targetFile, doc) -> {
			try (final Writer writer = context.createTestWriter(targetFile);) {
				// If no writer can be created, just abort
				if (writer == null) { return; }
				final Source source = new DOMSource(doc);
				final Result result = new StreamResult(writer);
				try {
					transformer.transform(source, result);
				} catch (final TransformerException e) {
					e.printStackTrace();
					context.emitError("Impossible to transform XML: ", e);
				}
			} catch (final IOException e1) {
				context.emitError("Impossible to open file for writing: ", e1);
			}
		});
		documents.clear();

	}

	// Cleaning means:
	// - Category: remove space and minus characters in the category name to be able to use it in the model
	// - Operators: replace special characters like +, -, *, /
	/**
	 * Clean document test.
	 *
	 * @param doc the doc
	 * @return the document
	 */
	// - Operators and statements: addition of an index to have different variables
	public static Document cleanDocumentTest(final Document doc) {

		final NodeList nLCategories = doc.getElementsByTagName(CATEGORY);
		final NodeList nLOperators = doc.getElementsByTagName(OPERATOR);
		final NodeList nLStatements = doc.getElementsByTagName(STATEMENT);

		for (int i = 0; i < nLCategories.getLength(); i++) {
			final org.w3c.dom.Element eltCategory = (org.w3c.dom.Element) nLCategories.item(i);
			eltCategory.setAttribute(ATT_CAT_ID,
					Constants.capitalizeAllWords(eltCategory.getAttribute(ATT_CAT_ID).replaceAll("-", " ")));
		}

		for (int j = 0; j < nLOperators.getLength(); j++) {
			final org.w3c.dom.Element eltOperator = (org.w3c.dom.Element) nLOperators.item(j);
			String att = eltOperator.getAttribute(ATT_OP_ID);
			eltOperator.setAttribute(ATT_OP_ID, NAME_OPERATOR.getOrDefault(att, att));
			att = eltOperator.getAttribute(ATT_OP_NAME);
			eltOperator.setAttribute(ATT_OP_NAME, NAME_OPERATOR.getOrDefault(att, att));
			att = eltOperator.getAttribute(ATT_OP_ALT_NAME);
			eltOperator.setAttribute(ATT_OP_ALT_NAME, NAME_OPERATOR.getOrDefault(att, att));

			final NodeList nLExamples = eltOperator.getElementsByTagName(EXAMPLE);
			for (int k = 0; k < nLExamples.getLength(); k++) {
				final org.w3c.dom.Element eltExample = (org.w3c.dom.Element) nLExamples.item(k);
				eltExample.setAttribute(ATT_EXAMPLE_INDEX, "" + k);
			}
		}

		for (int j = 0; j < nLStatements.getLength(); j++) {
			final org.w3c.dom.Element eltStatement = (org.w3c.dom.Element) nLStatements.item(j);

			final NodeList nLExamples = eltStatement.getElementsByTagName(EXAMPLE);
			for (int k = 0; k < nLExamples.getLength(); k++) {
				final org.w3c.dom.Element eltExample = (org.w3c.dom.Element) nLExamples.item(k);
				eltExample.setAttribute(ATT_EXAMPLE_INDEX, "" + k);
			}
		}

		return doc;
	}

}
