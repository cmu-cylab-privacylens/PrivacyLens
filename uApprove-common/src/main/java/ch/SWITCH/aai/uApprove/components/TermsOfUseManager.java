/*
 * Created on Apr 21, 2005
 * 
 * Copyright (c) 2005-2006 SWITCH - The Swiss Education & Research Network 
 *
 * 
 * @author Patrik Schnellmann <schnellmann@switch.ch>
 * binding to xml document with the following structure:
 * <TermsOfUse>
 *   <version>...</version>
 *   <termsText>...</termsText>
 * </TermsOfUse>
 * both of these can either contain a PCDATA or a CDATA section
 * 
 */
package ch.SWITCH.aai.uApprove.components;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * @author schnell, reusser
 * 
 * Data container for Terms of Use which are read from an XML file
 * 
 */
public class TermsOfUseManager {
  private static TermsOfUseManager termsManager;
  private static String termsVersion;
  private static String termsText;

  private static Logger LOG = LoggerFactory.getLogger(TermsOfUseManager.class);



  /**
   * 
   * @param filename
   * @return TermsOfUseManager object
   * @throws IOException 
   * @throws SAXException 
   * @throws ParserConfigurationException 
   * @throws FileNotFoundException 
   */
  public static synchronized void initalize(String filename)
      throws UApproveException, FileNotFoundException, ParserConfigurationException, SAXException, IOException {
    if (termsManager == null)
      termsManager = new TermsOfUseManager(filename);
  }


  /**
   * 
   * Read information from XML file and store data in this class
   * @throws ParserConfigurationException 
   * @throws IOException 
   * @throws SAXException 
   * @throws FileNotFoundException 
   * @throws UApproveException 
   */
  private TermsOfUseManager(String filename) throws ParserConfigurationException, FileNotFoundException, SAXException, IOException, UApproveException {
    LOG.debug("TermsOfUseManager initialization from file="+filename);
    DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    Document doc = docBuilder.parse(new FileInputStream(new File(filename)));
    readFromXmlDocument(doc);
  }

  /**
   * 
   * @return String
   * @throws UApproveException 
   */
  public static String getVersion() throws UApproveException {
    if (termsVersion == null)
       throw new UApproveException(TermsOfUseManager.class+" is not initialisized");
    if (termsVersion.trim().equals(""))
      LOG.warn(TermsOfUseManager.class +" Terms version is not set");
    return termsVersion;
  }

  /**
   * 
   * @return String
   * @throws UApproveException 
   */
  public static String getTermsText() throws UApproveException {
    if (termsText == null)
      throw new UApproveException(TermsOfUseManager.class+" is not initialisized");
   if (termsText.trim().equals(""))
     LOG.warn(TermsOfUseManager.class +" Terms text is not set");
    return termsText;
  }

  /**
   * Read the user settings LogInfo XML document
   * 
   * @param doc
   * @throws UApproveException
   */
  private void readFromXmlDocument(Document doc) throws UApproveException {
    Node n, t;
    NodeList nodeList, children = null;

    nodeList = doc.getChildNodes();
    if (nodeList != null) {
      if (nodeList.item(0).getNodeName().equals("TermsOfUse")) {
        nodeList = nodeList.item(0).getChildNodes();
        if (nodeList != null) {
          for (int i = 0, maxi = nodeList.getLength(); i < maxi; i++) {
            n = nodeList.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
              children = n.getChildNodes();
              if (children != null) {
                for (int j = 0, maxj = children.getLength(); j < maxj; j++) {
                  t = children.item(j);
                  if (t.getNodeType() == Node.TEXT_NODE
                      || t.getNodeType() == Node.CDATA_SECTION_NODE) {
                    if ("version".equals(n.getNodeName())) {
                      termsVersion = t.getNodeValue();
                    }
                    if ("text".equals(n.getNodeName())) {
                      termsText = t.getNodeValue();
                      break;
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    
  }

}