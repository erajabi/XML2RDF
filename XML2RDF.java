/**
 * Created with IntelliJ IDEA.
 * User: rajabi
 * Date: 9/5/13
 * Time: 9:42 AM
 * To change this template use File | Settings | File Templates.
 */
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.util.UUID;

public class XML2RDF {
    public static final String NL = System.getProperty("line.separator") ;
    public static final String inputFileName  = "input/55.XML";
    public static final String inputFileName2  = "my.RDF";
    public static boolean hasData=false;
    public Model model = ModelFactory.createDefaultModel();


    // ***************** Set prefix  *******************************************************
    String lomPrefix="lom";
    String dcPrefix="dcterms";
    String lomVoc="lomVoc";
    String RDFPrefix="rdf";

    public void setRDFPrefix(){
        model.setNsPrefix(lomPrefix,"http://ltsc.ieee.org/rdf/lomv1p0/lom#");
        model.setNsPrefix(dcPrefix,"http://purl.org/dc/terms/");
        model.setNsPrefix(lomVoc,"http://ltsc.ieee.org/rdf/lomv1p0/vocabulary#");
        model.setNsPrefix(RDFPrefix,"http://www.w3.org/1999/02/22-rdf-syntax-ns#");
    }

    // ***************** General Category  *******************************************************
    public void parseGeneralCategory(NodeList GeneralList, String metadataURI){
        if (GeneralList != null && GeneralList.getLength() > 0 ) {

            Node node = GeneralList.item(0);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element generalE = (Element) node;

                // *****************************  LOM:identifier    ***********************************
                NodeList GeneralIdentifierList=generalE.getElementsByTagName("lom:identifier");
                Property generalIdentifierProperty=model.createProperty(model.getNsPrefixURI(lomPrefix)+"identifier");

                for ( int i = 0; i < GeneralIdentifierList.getLength(); i++) {
                    Node generalIdentifierNode = GeneralIdentifierList.item(i);
                    Element generalID = (Element) generalIdentifierNode;

                    // *****************************  catalog    ***********************************
                    System.out.println("Analyzing general.Catalog");

                    NodeList GCatalogNodeList = generalID.getElementsByTagName("lom:catalog");
                    Property generalIdentifier=model.createProperty(metadataURI+"General-Identifier-"+(i+1));
                    if(GCatalogNodeList.getLength()!=0 && GCatalogNodeList.item(0).hasChildNodes() ) {
                        String generalCatalogValue= GCatalogNodeList.item(0).getChildNodes().item(0).getNodeValue();
                        if(!generalCatalogValue.isEmpty()) {
                            hasData=true;
                            Property catalog=model.createProperty(model.getNsPrefixURI(lomPrefix)+"catalog");
                            generalIdentifier.addProperty(catalog, GCatalogNodeList.item(0).getChildNodes().item(0).getNodeValue());
                        }
                    }
                    // *****************************  Entry    ***********************************
                    System.out.println("Analyzing general.Entry");

                    NodeList GEntryNodeList = generalID.getElementsByTagName("lom:entry");
                    if(GEntryNodeList.getLength()!=0 && GEntryNodeList.item(0).hasChildNodes() ) {
                        String generalEntryValue= GEntryNodeList.item(0).getChildNodes().item(0).getNodeValue();
                        if(!generalEntryValue.isEmpty()) {
                            hasData=true;
                            generalIdentifier.addProperty(DCTerms.identifier, generalEntryValue);
                        }
                    }
                    // Adding blank node
                    if(hasData)
                        model.createResource(metadataURI)
                                .addProperty(generalIdentifierProperty, generalIdentifier);
                }
                // *****************************  Title   **********************************
                System.out.println("Analyzing general.Title");

                NodeList titleList=generalE.getElementsByTagName("lom:title");
                for ( int i = 0; i < titleList.getLength(); i++) {
                    Node titleNode = titleList.item(i);
                    Element e1 = (Element) titleNode;
                    // *****************************  string    ***********************************
                    NodeList nodeList = e1.getElementsByTagName("lom:string");
                    if(nodeList.getLength()!=0 && nodeList.item(0).hasChildNodes() ) {
                        for ( int j = 0; j < nodeList.getLength(); j++) {
                            String titleValue= nodeList.item(j).getChildNodes().item(0).getNodeValue();
                            if(!titleValue.isEmpty())
                                model.createResource(metadataURI)
                                        .addProperty(DCTerms.title, titleValue,nodeList.item(j).getAttributes().item(0).getNodeValue());
                        }
                    }
                }
                // *****************************  Language  **********************************
                System.out.println("Analyzing general.Language");

                NodeList GLang=generalE.getElementsByTagName("lom:language");
                if(GLang.getLength()!=0 && GLang.item(0).hasChildNodes() ) {
                    for (int  i = 0; i < GLang.getLength(); i++) {
                        String GeneralLanguage=GLang.item(i).getChildNodes().item(0).getNodeValue();
                        if(!GeneralLanguage.isEmpty())
                            model.createResource(metadataURI)
                                    .addProperty(DCTerms.language, GeneralLanguage);
                    }
                }
                // ***************************** General Description   **********************************
                System.out.println("Analyzing general.Description");

                NodeList descList=generalE.getElementsByTagName("lom:description");
                Property GeneralDescriptionPrefix=model.createProperty(model.getNsPrefixURI(lomPrefix)+"description");

                for (int i = 0; i < descList.getLength(); i++) {
                    Property descIdentifier=model.createProperty(metadataURI+"General-Description-"+(i+1));
                    Node descNode = descList.item(i);
                    Element e1 = (Element) descNode;
                    // *****************************  string    ***********************************
                    NodeList descNodeList = e1.getElementsByTagName("lom:string");
                    if(descNodeList.getLength()!=0 && descNodeList.item(0).hasChildNodes() ) {

                        for ( int j = 0; j < descNodeList.getLength(); j++) {
                            if(!descNodeList.item(j).getChildNodes().item(0).getNodeValue().isEmpty()) {
                                model.createResource(metadataURI)
                                        .addProperty(GeneralDescriptionPrefix, descIdentifier);
                                descIdentifier
                                        .addProperty(DCTerms.description, descNodeList.item(j).getChildNodes().item(0).getNodeValue(),descNodeList.item(j).getAttributes().item(0).getNodeValue());
                            }
                        }
                    }
                }
                System.out.println("Analyzing general.Keyword");

                // *****************************  Keyword    **********************************
                NodeList keywordList=generalE.getElementsByTagName("lom:keyword");
                Property keywordDescriptionPrefix=model.createProperty(model.getNsPrefixURI(lomPrefix)+"keyword");

                for (int  i = 0; i < keywordList.getLength(); i++) {
                    Property keywordIdentifier=model.createProperty(metadataURI+"General-keyword"+(i+1));

                    Node keywordNode = keywordList.item(i);
                    Element keywordE = (Element) keywordNode;
                    // *****************************  string    ***********************************
                    NodeList keywordNodeList = keywordE.getElementsByTagName("lom:string");
                    if(keywordNodeList.getLength()!=0 && keywordNodeList.item(0).hasChildNodes() ) {

                        for ( int j = 0; j < keywordNodeList.getLength(); j++) {
                            if(!keywordNodeList.item(j).getChildNodes().item(0).getNodeValue().isEmpty()) {
                                model.createResource(metadataURI)
                                        .addProperty(keywordDescriptionPrefix, keywordIdentifier);
                                keywordIdentifier
                                        .addProperty(RDF.value, keywordNodeList.item(j).getChildNodes().item(0).getNodeValue(),keywordNodeList.item(j).getAttributes().item(0).getNodeValue());
                            }
                        }
                    }
                }

                // *****************************  Coverage    **********************************
                System.out.println("Analyzing general.Coverage");

                NodeList coverageList=generalE.getElementsByTagName("lom:coverage");
                Property coveragePrefix=model.createProperty(model.getNsPrefixURI(lomPrefix)+"coverage");

                for (int i = 0; i < coverageList.getLength(); i++) {
                    Property coverageIdentifier=model.createProperty(metadataURI+"General-Coverage-"+(i+1));

                    Node coverageNode = coverageList.item(i);
                    Element coverageE = (Element) coverageNode;
                    // *****************************  string   ***********************************
                    NodeList coverageNodeList = coverageE.getElementsByTagName("lom:string");
                    if(coverageNodeList.getLength()!=0 && coverageNodeList.item(0).hasChildNodes() ) {

                        for ( int j = 0; j < coverageNodeList.getLength(); j++) {
                            if(!coverageNodeList.item(j).getChildNodes().item(0).getNodeValue().isEmpty()) {
                                model.createResource(metadataURI)
                                        .addProperty(coveragePrefix, coverageIdentifier);
                                coverageIdentifier
                                        .addProperty(DCTerms.coverage, coverageNodeList.item(j).getChildNodes().item(0).getNodeValue(),coverageNodeList.item(j).getAttributes().item(0).getNodeValue());
                            }
                        }
                    }
                }
                // *****************************  Structure  **********************************
                System.out.println("Analyzing general.Structure");

                NodeList GStruct=generalE.getElementsByTagName("lom:structure");
                Property StrcuturePrefix=model.createProperty(model.getNsPrefixURI(lomPrefix)+"structure");

                Node GIDNode1 = GStruct.item(0);
                Element generalID1 = (Element) GIDNode1;

                // *****************************  value    ***********************************
                if(GStruct.getLength()!=0 && GStruct.item(0).hasChildNodes() ){
                    NodeList GeneralStructList = generalID1.getElementsByTagName("lom:value");

                    String GeneralStruct=GeneralStructList.item(0).getChildNodes().item(0).getNodeValue();
                    if(!GeneralStruct.isEmpty())
                        model.createResource(metadataURI)
                                .addProperty(StrcuturePrefix, model.getNsPrefixURI(lomVoc)+"Structure-"+GeneralStruct);
                }

                // *****************************  Aggregation Level  **********************************
                System.out.println("Analyzing general.Aggregation Level");

                NodeList GAggregation=generalE.getElementsByTagName("lom:aggregationLevel");
                Property aggregationPrefix=model.createProperty(model.getNsPrefixURI(lomPrefix)+"aggregationLevel");

                Node GAggNode1 = GAggregation.item(0);
                Element generalAgg = (Element) GAggNode1;

                // *****************************  value    ***********************************
                if(GAggregation.getLength()!=0 && GAggregation.item(0).hasChildNodes() ){
                    NodeList GeneralAggList = generalAgg.getElementsByTagName("lom:value");
                    String GeneralAgg=GeneralAggList.item(0).getChildNodes().item(0).getNodeValue();
                    if(!GeneralAgg.isEmpty())
                        model.createResource(metadataURI)
                                .addProperty(aggregationPrefix, model.getNsPrefixURI(lomVoc)+"AggregationLevel-"+GeneralAgg);
                }
            }

        }
    }

    // ***************** Lifecycle Category  *******************************************************
    public void parseLifecycleCategory(NodeList lifeCycleList, String metadataURI){
        if (lifeCycleList != null && lifeCycleList.getLength() > 0) {
            for (int i = 0; i < lifeCycleList.getLength(); i++) {

                Node node = lifeCycleList.item(i);

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element lifeCycleE = (Element) node;

                    // *****************************  LifeCycle:version    ***********************************
                    System.out.println("Analyzing LifeCycle.Version");

                    NodeList lifeCycleVersion=lifeCycleE.getElementsByTagName("lom:version");
                    Property lifeCycleVersionProperty=model.createProperty(model.getNsPrefixURI(lomPrefix)+"version");
                    for ( i = 0; i < lifeCycleVersion.getLength(); i++) {
                        Node versionNode = lifeCycleVersion.item(i);
                        Element e1 = (Element) versionNode;
                        // *****************************  string    ***********************************
                        NodeList nodeList = e1.getElementsByTagName("lom:string");
                        if(nodeList.getLength()!=0 && nodeList.item(0).hasChildNodes() ){

                            for ( int j = 0; j < nodeList.getLength(); j++) {
                                String versionValue=nodeList.item(j).getChildNodes().item(0).getNodeValue();
                                if(!versionValue.isEmpty()) {
                                    model.createResource(metadataURI)
                                            .addProperty(lifeCycleVersionProperty, versionValue,nodeList.item(j).getAttributes().item(0).getNodeValue());
                                }
                            }
                        }
                    }

                    // *****************************  Life Cycle Status  **********************************
                    System.out.println("Analyzing LifeCycle.Status");

                    NodeList lifeCycleStatus=lifeCycleE.getElementsByTagName("lom:status");
                    Property lifeCycleStatusPrefix=model.createProperty(model.getNsPrefixURI(lomPrefix)+"status");

                    Node statusNode = lifeCycleStatus.item(0);
                    Element generalAgg = (Element) statusNode;

                    // *****************************  value    ***********************************
                    if(lifeCycleStatus.getLength()!=0 && lifeCycleStatus.item(0).hasChildNodes() ){

                        NodeList statusList = generalAgg.getElementsByTagName("lom:value");
                        String status=statusList.item(0).getChildNodes().item(0).getNodeValue();
                        if(!status.isEmpty())
                            model.createResource(metadataURI)
                                    .addProperty(lifeCycleStatusPrefix, model.getNsPrefixURI(lomVoc)+"Status-"+status);
                    }
                    // *****************************  Life Cycle Contribute  **********************************
                    System.out.println("Analyzing LifeCycle.Contribute");

                    NodeList GeneralIdentifierList=lifeCycleE.getElementsByTagName("lom:contribute");
                    Property contributePrefix=model.createProperty(model.getNsPrefixURI(lomPrefix)+"contribute");

                    for ( i = 0; i < GeneralIdentifierList.getLength(); i++) {
                        Node contributeNode = GeneralIdentifierList.item(i);
                        Element contributeE = (Element) contributeNode;
                        Property Identifier=model.createProperty(metadataURI+"LifeCycle-Contribute-"+(i+1));
                        // Adding blank Node
                        model.createResource(metadataURI)
                                .addProperty(contributePrefix, Identifier);
                        // *****************************  role -value   ***********************************
                        NodeList contributeNodeList = contributeE.getElementsByTagName("lom:value");

                        if(contributeNodeList.getLength()!=0 && contributeNodeList.item(0).hasChildNodes() ){
                            String roleValue=contributeNodeList.item(0).getChildNodes().item(0).getNodeValue();
                            if(!roleValue.isEmpty()) {
                                Property contributeRolePrefix=model.createProperty(model.getNsPrefixURI(lomPrefix)+"contributeRole");
                                Identifier.addProperty(contributeRolePrefix, model.getNsPrefixURI(lomVoc)+"Role-" +roleValue);
                            }
                        }
                        // *****************************  Entity -vcard   ***********************************
                        NodeList contributeEntityNodeList = contributeE.getElementsByTagName("lom:entity");
                        Property contributeEntityPrefix=model.createProperty(model.getNsPrefixURI(lomPrefix)+"contributeEntity");

                        for ( int k = 0; k < contributeEntityNodeList.getLength(); k++) {

                            Property EntityIdentifier=model.createProperty(metadataURI+"LifeCycle-Contribute-Entity-"+(i+1)+"-"+(k+1));

                            if(contributeEntityNodeList.getLength()!=0 && contributeEntityNodeList.item(0).hasChildNodes() ){

                                if(!contributeEntityNodeList.item(k).getChildNodes().item(0).getNodeValue().isEmpty()) {
                                    // Adding blank Node
                                    Identifier
                                            .addProperty(contributeEntityPrefix, EntityIdentifier);
                                    String EntityVcard=contributeEntityNodeList.item(k).getChildNodes().item(0).getNodeValue();
                                    EntityIdentifier.addProperty(RDF.value,EntityVcard);
                                    //VCardParser n=new VCardParser(EntityVcard);
                                    //System.out.println("VCARD="+n.getFamilyName());
                                }

                            }
                        }
                        // *****************************  Entity -Date   ***********************************
                        NodeList contributionDateNodeList = contributeE.getElementsByTagName("lom:dateTime");
                        Property contributeDatePrefix=model.createProperty(model.getNsPrefixURI(lomPrefix)+"contributeEntityDate");
                        if(contributionDateNodeList.getLength()!=0 && contributionDateNodeList.item(0).hasChildNodes() ){
                            String contributionDate=contributionDateNodeList.item(0).getChildNodes().item(0).getNodeValue();
                            if(!contributionDate.isEmpty())
                                Identifier.addProperty(contributeDatePrefix,contributionDate);
                        }
                    }

                } // LIFE CYCLE
            }
        }
    }

    // ***************** metaMetadata Category  *******************************************************
    public void parsemetaMetadataCategory(NodeList metaMetadataList, String metadataURI)  {
        if (metaMetadataList != null && metaMetadataList.getLength() > 0) {
            Node node = metaMetadataList.item(0);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) node;

                // *****************************  metaMetadata:Identifier    ***********************************
                System.out.println("Analyzing metaMetadata.Identifier");

                NodeList metaMetadataIdentifierList=e.getElementsByTagName("lom:identifier");
                Property metaMetadataIdentifierPrefix=model.createProperty(model.getNsPrefixURI(lomPrefix)+"metaMetadataIdentifier");

                for (int i = 0; i < metaMetadataIdentifierList.getLength(); i++) {
                    Node mNode = metaMetadataIdentifierList.item(i);
                    Element metaMetadataE = (Element) mNode;

                    hasData=false;
                    // *****************************  metaMetadata catalog  ************************************
                    System.out.println("Analyzing metaMetadata.Identifier.Catalog");

                    NodeList metaMetadataENodeList = metaMetadataE.getElementsByTagName("lom:catalog");

                    Property metaMetadataIdentifier=model.createProperty(metadataURI+"metaMetadata-Identifier-"+(i+1));
                    if(metaMetadataENodeList.getLength()!=0 && metaMetadataENodeList.item(0).hasChildNodes() ){
                        String  metaMetadataCatalog=metaMetadataENodeList.item(0).getChildNodes().item(0).getNodeValue();
                        if(!metaMetadataCatalog.isEmpty()) {
                            hasData=true;
                            Property catalog=model.createProperty(model.getNsPrefixURI(lomPrefix)+"metaMetadataCatalog");
                            metaMetadataIdentifier.addProperty(catalog, metaMetadataCatalog);
                        }
                    }
                    // ***************************** metaMetadata Entry   **************************************
                    System.out.println("Analyzing metaMetadata.Identifier.Entry");

                    NodeList MEntryNodeList = metaMetadataE.getElementsByTagName("lom:entry");

                    if(MEntryNodeList.getLength()!=0 && MEntryNodeList.item(0).hasChildNodes() ){
                        String  metaMetadataEntry=MEntryNodeList.item(0).getChildNodes().item(0).getNodeValue();
                        if(!metaMetadataEntry.isEmpty()) {
                            hasData=true;
                            Property entry=model.createProperty(model.getNsPrefixURI(lomPrefix)+"metaMetadataEntry");
                            metaMetadataIdentifier.addProperty(entry, metaMetadataEntry);
                        }
                    }
                    if(hasData)
                        model.createResource(metadataURI)
                                .addProperty(metaMetadataIdentifierPrefix, metaMetadataIdentifier);
                }

                // *****************************  metaMetadata Contribute  **********************************
                System.out.println("Analyzing metaMetadata.Contribute");

                NodeList metaMetadataContributeList=e.getElementsByTagName("lom:contribute");
                Property metaMetadataContributeProperty=model.createProperty(model.getNsPrefixURI(lomPrefix)+"metaMetadataContribute");

                for ( int i = 0; i < metaMetadataContributeList.getLength(); i++) {
                    Node contributeNode = metaMetadataContributeList.item(i);
                    Element contributeE = (Element) contributeNode;

                    // *****************************  role -value   ***********************************
                    NodeList contributeNodeList = contributeE.getElementsByTagName("lom:value");
                    Property metaMetadataBlankNode=model.createProperty(metadataURI+"metaMetadata-Contribute"+(i+1));
                    // Adding blank Node
                    model.createResource(metadataURI)
                            .addProperty(metaMetadataContributeProperty, metaMetadataBlankNode);
                    if(contributeNodeList.getLength()!=0 && contributeNodeList.item(0).hasChildNodes() ){
                        String  contributeRoleValue=contributeNodeList.item(0).getChildNodes().item(0).getNodeValue();
                        if(!contributeRoleValue.isEmpty()) {
                            Property contributeRolePrefix=model.createProperty(model.getNsPrefixURI(lomPrefix)+"metaMetadataContributeRole");
                            metaMetadataBlankNode.addProperty(contributeRolePrefix, model.getNsPrefixURI(lomVoc)+"metaMetadataRole-" +contributeRoleValue);
                        }
                    }
                    // *****************************  Entity - vcard   ***********************************
                    NodeList contributeEntityNodeList = contributeE.getElementsByTagName("lom:entity");
                    Property contributeEntityProperty=model.createProperty(model.getNsPrefixURI(lomPrefix)+"metaMetadataContributeEntity");

                    for ( int k = 0; k < contributeEntityNodeList.getLength(); k++) {

                        Property metaMetadataEntityBlankNode=model.createProperty(metadataURI+"metaMetadata-Contribute-Entity"+(i+1)+"-"+(k+1));
                        // Adding blank Node
                        metaMetadataBlankNode
                                .addProperty(contributeEntityProperty, metaMetadataEntityBlankNode);
                        if(contributeEntityNodeList.getLength()!=0 && contributeEntityNodeList.item(0).hasChildNodes() ){
                            String contributeEntityValue=contributeEntityNodeList.item(k).getChildNodes().item(0).getNodeValue();
                            if(!contributeEntityValue.isEmpty()) {
                                //Property contributeRolePrefix=model.createProperty(model.getNsPrefixURI(lomPrefix)+"contributeEntity");
                                // String EntityVcard=contributeEntityNodeList.item(k).getChildNodes().item(0).getNodeValue();
                                metaMetadataEntityBlankNode.addProperty(RDF.value,contributeEntityValue);
                            }
                        }
                    }
                    // *****************************  Contribution - Date   ***********************************
                    NodeList metaMetadataContributionDateNodeList = contributeE.getElementsByTagName("lom:dateTime");
                    Property contributeDatePrefix=model.createProperty(model.getNsPrefixURI(lomPrefix)+"metaMetadataContributeDate");
                    if(metaMetadataContributionDateNodeList.getLength()!=0 && metaMetadataContributionDateNodeList.item(0).hasChildNodes() ){

                        if(!metaMetadataContributionDateNodeList.item(0).getChildNodes().item(0).getNodeValue().isEmpty()) {
                            String contributionDate=metaMetadataContributionDateNodeList.item(0).getChildNodes().item(0).getNodeValue();
                            metaMetadataBlankNode.addProperty(contributeDatePrefix,contributionDate);
                        }
                    }
                }
                // *****************************  metaMetadata Schema    ***********************************
                System.out.println("Analyzing metaMetadata.Schema");

                NodeList metaMetadataSchemaList=e.getElementsByTagName("lom:metadataSchema");
                Property metaMetadataSchemaProperty=model.createProperty(model.getNsPrefixURI(lomPrefix)+"metaMetadataSchema");
                for ( int i = 0; i < metaMetadataSchemaList.getLength(); i++) {
                    if(metaMetadataSchemaList.item(0).hasChildNodes() ){
                        String schemaValue=metaMetadataSchemaList.item(i).getChildNodes().item(0).getNodeValue();
                        if(!schemaValue.isEmpty())
                            model.createResource(metadataURI)
                                    .addProperty(metaMetadataSchemaProperty, schemaValue);
                    }
                }
                // *****************************  metaMetadata Language    ***********************************
                System.out.println("Analyzing metaMetadata.Language");

                NodeList metaMetadataLanguage=e.getElementsByTagName("lom:language");
                Property metaMetadataLanguageProperty=model.createProperty(model.getNsPrefixURI(lomPrefix)+"metaMetadataLanguage");
                String languageValue=metaMetadataLanguage.item(0).getChildNodes().item(0).getNodeValue();
                if(metaMetadataLanguage.getLength()!=0 && metaMetadataLanguage.item(0).hasChildNodes() )
                    if(!languageValue.isEmpty() )
                        model.createResource(metadataURI)
                                .addProperty(metaMetadataLanguageProperty, languageValue);
            }
        }// metaMetadata
    }

    // ***************** Technical Category  *******************************************************
    public void parseTechnicalCategory(NodeList TechnicalList, String metadataURI){
    if (TechnicalList != null && TechnicalList.getLength() > 0) {
        Node node = TechnicalList.item(0);

        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element e = (Element) node;

            // *****************************  Technical Format    **********************************
            System.out.println("Analyzing Technical.Format");

            NodeList technicalFormatList=e.getElementsByTagName("lom:format");
            //Property metaMetadataSchemaProperty=model.createProperty(model.getNsPrefixURI(lomPrefix)+"metaMetadataSchema");
            for ( int i = 0; i < technicalFormatList.getLength(); i++) {
                if(technicalFormatList.item(0).hasChildNodes() ) {

                    String formatValue=technicalFormatList.item(i).getChildNodes().item(0).getNodeValue();
                    if(!formatValue.isEmpty())
                        model.createResource(metadataURI)
                                .addProperty(DCTerms.format, formatValue);
                }
            }
            // *****************************  Technical Size    ***********************************
            System.out.println("Analyzing Technical.Size");

            NodeList technicalSize=e.getElementsByTagName("lom:size");
            Property technicalSizeProperty=model.createProperty(model.getNsPrefixURI(lomPrefix)+"technicalSize");
            if(technicalSize.getLength()!=0 && technicalSize.item(0).hasChildNodes() ) {

                String sizeValue=technicalSize.item(0).getChildNodes().item(0).getNodeValue();
                if(!sizeValue.isEmpty())
                    model.createResource(metadataURI)
                            .addProperty(technicalSizeProperty, sizeValue);
            }

            // *****************************  Technical Location    **********************************
            System.out.println("Analyzing Technical.Location");

            NodeList technicalLocationList=e.getElementsByTagName("lom:location");
            Property locationProperty=model.createProperty(model.getNsPrefixURI(lomPrefix)+"techincalLocation");
            for ( int i = 0; i < technicalLocationList.getLength(); i++) {
                if(technicalLocationList.getLength()!=0 && technicalLocationList.item(0).hasChildNodes() ) {

                    String locationValue=technicalLocationList.item(i).getChildNodes().item(0).getNodeValue();
                    if(!locationValue.isEmpty())
                        model.createResource(metadataURI)
                                .addProperty(locationProperty, locationValue);
                }
            }

            // *****************************  Technical Installation Remark    ***********************************
            System.out.println("Analyzing Technical.InstallationRemarks");

            NodeList installationList=e.getElementsByTagName("lom:installationRemarks");
            Property installationProperty=model.createProperty(model.getNsPrefixURI(lomPrefix)+"installationRemarks");
            Node installationNode = installationList.item(0);
            Element e1 = (Element) installationNode;

            if(installationList.getLength()!=0 && installationList.item(0).hasChildNodes() ){
                // *****************************  string    ***********************************
                NodeList nodeList = e1.getElementsByTagName("lom:string");
                for ( int j = 0; j < nodeList.getLength(); j++) {
                    String installValue=nodeList.item(j).getChildNodes().item(0).getNodeValue();
                    if(!installValue.isEmpty())
                        model.createResource(metadataURI)
                                .addProperty(installationProperty, installValue,nodeList.item(j).getAttributes().item(0).getNodeValue());
                }
            }
            // *****************************  Technical otherPlatformRequirements    ***********************************
            System.out.println("Analyzing Technical.otherPlatformRequirements");

            NodeList otherPlatformRequirementsList=e.getElementsByTagName("lom:otherPlatformRequirements");
            Property otherPlatformRequirementsProperty=model.createProperty(model.getNsPrefixURI(lomPrefix)+"otherPlatformRequirements");
            Node otherPlatformRequirementsNode = otherPlatformRequirementsList.item(0);
            Element e2 = (Element) otherPlatformRequirementsNode;
            if(otherPlatformRequirementsList.getLength()!=0 && otherPlatformRequirementsList.item(0).hasChildNodes() ){

                // *****************************  string    ***********************************
                NodeList otherPlatformNodeList = e2.getElementsByTagName("lom:string");

                for ( int j = 0; j < otherPlatformNodeList.getLength(); j++) {
                    String otherPlatformRequirementsValue=otherPlatformNodeList.item(j).getChildNodes().item(0).getNodeValue();
                    if(!otherPlatformRequirementsValue.isEmpty())
                        model.createResource(metadataURI)
                                .addProperty(otherPlatformRequirementsProperty, otherPlatformRequirementsValue,otherPlatformNodeList.item(j).getAttributes().item(0).getNodeValue());
                }
            }
            // *****************************  Technical duration    ***********************************
            System.out.println("Analyzing Technical.Duration");

            NodeList technicalDurationList=e.getElementsByTagName("lom:duration");
            Property technicalDurationProperty=model.createProperty(model.getNsPrefixURI(lomPrefix)+"technicalDuration");
            Node technicalDurationNode = technicalDurationList.item(0);
            Element technicalDurationE = (Element)technicalDurationNode;
            if(technicalDurationList.getLength()!=0 && technicalDurationList.item(0).hasChildNodes() ){

                NodeList technicalDurationNodeList = technicalDurationE.getElementsByTagName("lom:duration");
                String technicalDurationValue=technicalDurationNodeList.item(0).getChildNodes().item(0).getNodeValue();
                if(!technicalDurationValue.isEmpty())
                    model.createResource(metadataURI)
                            .addProperty(technicalDurationProperty, technicalDurationValue);
            }
            // *****************************  Requirement  **********************************
            System.out.println("Analyzing Technical.Requirement");

            NodeList RequirementList=e.getElementsByTagName("lom:requirement");
            Property RequirementProperty=model.createProperty(model.getNsPrefixURI(lomPrefix)+"requirement");

            for ( int i = 0; i < RequirementList.getLength(); i++) {
                Node RequirementNode = RequirementList.item(i);
                Element RequirementE = (Element) RequirementNode;

                // *****************************  OrComposite   ***********************************
                NodeList OrCompositeNodeList = RequirementE.getElementsByTagName("lom:orComposite");
                Property OrCompositeProperty=model.createProperty(model.getNsPrefixURI(lomPrefix)+"orComposite");

                for ( int k = 0; k < OrCompositeNodeList.getLength(); k++) {
                    // Adding blank Node
                    Property RequirementBlankNode=model.createProperty(metadataURI+"Technical-Requirement-"+(i+1));
                    Property OrCompositeBlankNode=model.createProperty(metadataURI+"Technical-OrComposite-"+(i+1)+"-"+(k+1));

                    model.createResource(metadataURI)
                            .addProperty(RequirementProperty,RequirementBlankNode);
                    RequirementBlankNode
                            .addProperty(OrCompositeProperty, OrCompositeBlankNode);
                    Node orCompositeNode = RequirementList.item(i);
                    Element orCompositeE = (Element) orCompositeNode;

                    // *****************************  OrComposite - Name   ***********************************
                    NodeList orCompositeNameList = orCompositeE.getElementsByTagName("lom:name");
                    Property orCompositeNameProperty=model.createProperty(model.getNsPrefixURI(lomPrefix)+"orCompositeName");
                    Node orCompositeNameNode = orCompositeNameList.item(k);
                    Element orCompositeNameE = (Element) orCompositeNameNode;
                    if(orCompositeNameList.getLength()!=0 && orCompositeNameList.item(0).hasChildNodes() ){
                        NodeList orCompositeNameValueList = orCompositeNameE.getElementsByTagName("lom:value");
                        String orCompositeNameValue=orCompositeNameValueList.item(0).getChildNodes().item(0).getNodeValue();
                        if(!orCompositeNameValue.isEmpty())
                            OrCompositeBlankNode.addProperty(orCompositeNameProperty,
                                    model.getNsPrefixURI(lomVoc)+"OperatingSystemTechnology-" +orCompositeNameValue.replace(" ","-"));
                    }
                    // *****************************  OrComposite - Type   ***********************************
                    NodeList orCompositeTypeList = orCompositeE.getElementsByTagName("lom:type");
                    Property orCompositeTypeProperty=model.createProperty(model.getNsPrefixURI(lomPrefix)+"orCompositeType");
                    Node orCompositeTypeNode = orCompositeTypeList.item(k);
                    Element orCompositeTypeE = (Element) orCompositeTypeNode;
                    NodeList orCompositeTypeValueList = orCompositeTypeE.getElementsByTagName("lom:value");

                    if(orCompositeTypeValueList.getLength()!=0 && orCompositeTypeValueList.item(0).hasChildNodes() ){

                        String orCompositeTypeValue=orCompositeTypeValueList.item(0).getChildNodes().item(0).getNodeValue();
                        if(!orCompositeTypeValue.isEmpty())
                            OrCompositeBlankNode.addProperty(orCompositeTypeProperty,
                                    model.getNsPrefixURI(lomVoc)+"RequirementType-" +orCompositeTypeValue.replace(" ","-"));
                    }

                    // *****************************  OrComposite - minimumVersion   ***********************************
                    NodeList orCompositeMinimumVersionList = orCompositeE.getElementsByTagName("lom:minimumVersion");
                    Property orCompositeMinimumVersionProperty=model.createProperty(model.getNsPrefixURI(lomPrefix)+"orCompositeMinimumVersion");
                    if(orCompositeMinimumVersionList.getLength()!=0 && orCompositeMinimumVersionList.item(0).hasChildNodes() ){

                        String orCompositeMinimumVersionValue=orCompositeMinimumVersionList.item(k).getChildNodes().item(0).getNodeValue();
                        if(!orCompositeMinimumVersionValue.isEmpty())
                            OrCompositeBlankNode.addProperty(orCompositeMinimumVersionProperty,orCompositeMinimumVersionValue);
                    }
                    // *****************************  OrComposite - maximumVersion   ***********************************
                    NodeList orCompositeMaximumVersionList = orCompositeE.getElementsByTagName("lom:maximumVersion");
                    Property orCompositeMaximumVersionProperty=model.createProperty(model.getNsPrefixURI(lomPrefix)+"orCompositeMaximumVersion");
                    if(orCompositeMaximumVersionList.getLength()!=0 && orCompositeMaximumVersionList.item(0).hasChildNodes() ){

                        String orCompositeMaximumVersionValue=orCompositeMaximumVersionList.item(k).getChildNodes().item(0).getNodeValue();
                        if(!orCompositeMaximumVersionValue.isEmpty())
                            OrCompositeBlankNode.addProperty(orCompositeMaximumVersionProperty,orCompositeMaximumVersionValue);
                    }

                }
            }
        }
    }// Technical
    }

    // ***************** Educational Category  *******************************************************
    public void parseEducationalCategory (NodeList educationalList, String metadataURI){

        if (educationalList != null && educationalList.getLength() > 0) {

            for (int educationalIndex = 0; educationalIndex < educationalList.getLength();educationalIndex++) {

            Node node = educationalList.item(educationalIndex);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element educationalE = (Element) node;

                Property educationalProperty=model.createProperty(model.getNsPrefixURI(lomPrefix)+"educational");
                Property educationalBlankNode=model.createProperty(metadataURI+"educational-"+(educationalIndex+1));
                // Adding blank Node
                model.createResource(metadataURI)
                        .addProperty(educationalProperty, educationalBlankNode);

                // *****************************  Educational - interactivityType   ***********************************
                System.out.println("Analyzing Educational.InteractivityType");

                NodeList interactivityTypeList = educationalE.getElementsByTagName("lom:interactivityType");
                Property interactivityTypeProperty=model.createProperty(model.getNsPrefixURI(lomPrefix)+"interactivityType");
                Node interactivityTypeNode = interactivityTypeList.item(0);
                Element interactivityTypeE = (Element) interactivityTypeNode;
                if(interactivityTypeList.getLength()!=0 && interactivityTypeList.item(0).hasChildNodes() ){
                    NodeList interactivityTypeValueList = interactivityTypeE.getElementsByTagName("lom:value");
                    String interactivityTypeValue=interactivityTypeValueList.item(0).getChildNodes().item(0).getNodeValue();
                    if(!interactivityTypeValue.isEmpty())
                        educationalBlankNode.addProperty(interactivityTypeProperty,
                                model.getNsPrefixURI(lomVoc)+"InteractivityType-" +interactivityTypeValue.replace(" ",""));
                }
                // *****************************  Educational - learningResourceType   ***********************************
                System.out.println("Analyzing Educational.LearningResourceType");

                NodeList learningResourceTypeList = educationalE.getElementsByTagName("lom:learningResourceType");
                //Property learningResourceTypeProperty=model.createProperty(model.getNsPrefixURI(lomPrefix)+"learningResourceType");
                for(int k=0;k<learningResourceTypeList.getLength();k++) {

                    Node learningResourceTypeNode = learningResourceTypeList.item(k);
                    Element learningResourceTypeE = (Element) learningResourceTypeNode;
                    NodeList learningResourceTypeValueList = learningResourceTypeE.getElementsByTagName("lom:value");

                    if(learningResourceTypeValueList.getLength()!=0 && learningResourceTypeValueList.item(0).hasChildNodes() ){

                        String learningResourceTypeValue=learningResourceTypeValueList.item(0).getChildNodes().item(0).getNodeValue();
                        if(!learningResourceTypeValue.isEmpty())
                            educationalBlankNode.addProperty(DCTerms.type,
                                    model.getNsPrefixURI(lomVoc)+"LearningResourceType-" +learningResourceTypeValue.replace(" ",""));
                    }
                }

                // *****************************  Educational - interactivityLevel   ***********************************
                System.out.println("Analyzing Educational.InteractivityLevel");

                NodeList interactivityLevelList = educationalE.getElementsByTagName("lom:interactivityLevel");
                Property interactivityLevelProperty=model.createProperty(model.getNsPrefixURI(lomPrefix)+"interactivityLevel");
                Node interactivityLevelNode = interactivityLevelList.item(0);
                Element interactivityLevelE = (Element) interactivityLevelNode;
                NodeList interactivityLevelValueList = interactivityLevelE.getElementsByTagName("lom:value");
                if(interactivityLevelValueList.getLength()!=0 && interactivityLevelValueList.item(0).hasChildNodes() ){

                    String interactivityLevelValue=interactivityLevelValueList.item(0).getChildNodes().item(0).getNodeValue();
                    if(!interactivityLevelValue.isEmpty())
                        educationalBlankNode.addProperty(interactivityLevelProperty,
                                model.getNsPrefixURI(lomVoc)+"InteractivityLevel-" +interactivityLevelValue.replace(" ",""));
                }
                // *****************************  Educational - SemanticDensity   ***********************************
                System.out.println("Analyzing Educational.SemanticDensity");

                NodeList semanticDensityList = educationalE.getElementsByTagName("lom:semanticDensity");
                Property semanticDensityProperty=model.createProperty(model.getNsPrefixURI(lomPrefix)+"semanticDensity");
                Node semanticDensityNode = semanticDensityList.item(0);
                Element semanticDensityE = (Element) semanticDensityNode;
                NodeList semanticDensityValueList = semanticDensityE.getElementsByTagName("lom:value");

                if(semanticDensityValueList.getLength()!=0 && semanticDensityValueList.item(0).hasChildNodes() ){

                    String semanticDensityValue=semanticDensityValueList.item(0).getChildNodes().item(0).getNodeValue();
                    if(!semanticDensityValue.isEmpty())
                        educationalBlankNode.addProperty(semanticDensityProperty,
                                model.getNsPrefixURI(lomVoc)+"semanticDensity-" +semanticDensityValue.replace(" ",""));
                }
                // *****************************  Educational - intendedEndUserRole   ***********************************
                System.out.println("Analyzing Educational.IntendedEndUserRole");

                NodeList intendedEndUserRoleList = educationalE.getElementsByTagName("lom:intendedEndUserRole");
                Property intendedEndUserRoleProperty=model.createProperty(model.getNsPrefixURI(lomPrefix)+"intendedEndUserRole");
                for(int k=0;k<intendedEndUserRoleList.getLength();k++) {

                    Node intendedEndUserRoleNode = intendedEndUserRoleList.item(k);
                    Element intendedEndUserRoleE = (Element) intendedEndUserRoleNode;
                    NodeList intendedEndUserRoleValueList = intendedEndUserRoleE.getElementsByTagName("lom:value");

                    if(intendedEndUserRoleValueList.getLength()!=0 && intendedEndUserRoleValueList.item(0).hasChildNodes() ){

                        String intendedEndUserRoleValue=intendedEndUserRoleValueList.item(0).getChildNodes().item(0).getNodeValue();
                        if(!intendedEndUserRoleValue.isEmpty())
                            educationalBlankNode.addProperty(intendedEndUserRoleProperty,
                                    model.getNsPrefixURI(lomVoc)+"IntendedEndUserRole-" +intendedEndUserRoleValue.replace(" ",""));
                    }
                }

                // *****************************  Educational - context   ***********************************
                System.out.println("Analyzing Educational.Context");

                NodeList contextList = educationalE.getElementsByTagName("lom:context");
                Property contextProperty=model.createProperty(model.getNsPrefixURI(lomPrefix)+"context");
                for(int k=0;k<contextList.getLength();k++) {

                    Node contextNode = contextList.item(k);
                    Element contextE = (Element) contextNode;
                    NodeList contextValueList = contextE.getElementsByTagName("lom:value");
                    if(contextValueList.getLength()!=0 && contextValueList.item(0).hasChildNodes() ){

                        String contextValue=contextValueList.item(0).getChildNodes().item(0).getNodeValue();
                        if(!contextValue.isEmpty())
                            educationalBlankNode.addProperty(contextProperty,
                                    model.getNsPrefixURI(lomVoc)+"Context-" +contextValue.replace(" ",""));
                    }
                }
                // *****************************  Educational - difficulty   ***********************************
                System.out.println("Analyzing Educational.Difficulty");

                NodeList difficultyList = educationalE.getElementsByTagName("lom:difficulty");
                Property difficultyProperty=model.createProperty(model.getNsPrefixURI(lomPrefix)+"difficulty");
                Node difficultyNode = difficultyList.item(0);
                Element difficultyE = (Element) difficultyNode;
                NodeList difficultyValueList = difficultyE.getElementsByTagName("lom:value");

                if(difficultyValueList.getLength()!=0 && difficultyValueList.item(0).hasChildNodes() ){

                    String difficultyValue=difficultyValueList.item(0).getChildNodes().item(0).getNodeValue();
                    if(!difficultyValue.isEmpty())
                        educationalBlankNode.addProperty(difficultyProperty,
                                model.getNsPrefixURI(lomVoc)+"Difficulty-" +difficultyValue.replace(" ",""));
                }

                // *****************************  Typical Learning Time    ***********************************
                System.out.println("Analyzing Educational.Typical Learning Time");

                NodeList typicalLearningTimeList=educationalE.getElementsByTagName("lom:typicalLearningTime");
                Property typicalLearningTimeProperty=model.createProperty(model.getNsPrefixURI(lomPrefix)+"typicalLearningTime");
                Node typicalLearningTimeNode = typicalLearningTimeList.item(0);
                Element typicalLearningTimeE = (Element)typicalLearningTimeNode;
                if(typicalLearningTimeList.getLength()!=0 && typicalLearningTimeList.item(0).hasChildNodes() ){
                    NodeList typicalLearningTimeNodeList = typicalLearningTimeE.getElementsByTagName("lom:duration");
                    String typicalLearningTimeValue=typicalLearningTimeNodeList.item(0).getChildNodes().item(0).getNodeValue();
                    if(!typicalLearningTimeValue.isEmpty())
                        educationalBlankNode
                                .addProperty(typicalLearningTimeProperty, typicalLearningTimeValue);
                }

                // *****************************  educational Language  **********************************
                System.out.println("Analyzing Educational.Language");

                NodeList educationalLanguage=educationalE.getElementsByTagName("lom:language");
                Property educationalLanguageProperty=model.createProperty(model.getNsPrefixURI(lomPrefix)+"educationalLanguage");

                for ( int i = 0; i < educationalLanguage.getLength(); i++) {

                    if(educationalLanguage.getLength()!=0 && educationalLanguage.item(0).hasChildNodes() ){

                        String ELanguage=educationalLanguage.item(i).getChildNodes().item(0).getNodeValue();
                        if(!ELanguage.isEmpty())
                            educationalBlankNode
                                    .addProperty(educationalLanguageProperty, ELanguage);
                    }
                }
                // *****************************  Description    **********************************
                System.out.println("Analyzing Educational.Description");

                NodeList educationalDescriptionList=educationalE.getElementsByTagName("lom:description");
                Property educationalDescriptionProperty=model.createProperty(model.getNsPrefixURI(lomPrefix)+"educationalDescription");
                for ( int i = 0; i < educationalDescriptionList.getLength(); i++) {
                    Node descNode = educationalDescriptionList.item(i);
                    Element e1 = (Element) descNode;
                    // *****************************  string    ***********************************
                    NodeList descNodeList = e1.getElementsByTagName("lom:string");

                    if(descNodeList.getLength()!=0 && descNodeList.item(0).hasChildNodes() ){

                        for ( int j = 0; j < descNodeList.getLength(); j++) {
                            String descriptionValue=descNodeList.item(j).getChildNodes().item(0).getNodeValue();
                            if(!descriptionValue.isEmpty()) {
                                Property educationalDescriptionIdentifier=model.createProperty(metadataURI+"Educational-Description"+(i+1));
                                educationalBlankNode
                                        .addProperty(educationalDescriptionProperty, educationalDescriptionIdentifier);
                                educationalDescriptionIdentifier
                                        .addProperty(DCTerms.description, descriptionValue, descNodeList.item(j).getAttributes().item(0).getNodeValue());
                            }
                        }
                    }
                }

                // *****************************  educational -Typical age range    **********************************
                System.out.println("Analyzing Educational.Typical age range");

                NodeList educationalTypicalAgeRangeList=educationalE.getElementsByTagName("lom:typicalAgeRange");
                Property educationalTypicalAgeRangeProperty=model.createProperty(model.getNsPrefixURI(lomPrefix)+"educationalTypicalAgeRange");
                for ( int i = 0; i < educationalTypicalAgeRangeList.getLength(); i++) {
                    Node descNode = educationalTypicalAgeRangeList.item(i);
                    Element e1 = (Element) descNode;
                    // *****************************  string    ***********************************
                    NodeList descNodeList = e1.getElementsByTagName("lom:string");
                    if(descNodeList.getLength()!=0 && descNodeList.item(0).hasChildNodes() ){

                        for ( int j = 0; j < descNodeList.getLength(); j++) {
                            String typicalAgeRangeValue=descNodeList.item(j).getChildNodes().item(0).getNodeValue();
                            if(!typicalAgeRangeValue.isEmpty()) {
                                Property educationalTypicalAgeRangeIdentifier=model.createProperty(metadataURI+"Educational-typicalAgeRange"+(i+1));
                                educationalBlankNode
                                        .addProperty(educationalTypicalAgeRangeProperty, educationalTypicalAgeRangeIdentifier);
                                educationalTypicalAgeRangeIdentifier
                                        .addProperty(RDF.value, typicalAgeRangeValue, descNodeList.item(j).getAttributes().item(0).getNodeValue());
                            }
                        }
                    }
                }
            }
        }

    }// Educational
}

    // ***************** Right Category  *******************************************************
    public void parseRightCategory (NodeList rightList, String metadataURI) {
    if (rightList != null && rightList.getLength() > 0) {
        Node node = rightList.item(0);

        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element rightE = (Element) node;

            // *****************************  right - cost    **********************************
            System.out.println("Analyzing Right.Cost");

            NodeList costtList=rightE.getElementsByTagName("lom:cost");
            Property costProperty=model.createProperty(model.getNsPrefixURI(lomPrefix)+"cost");
            Node nodeCost = costtList.item(0);
            Element costE = (Element) nodeCost;
            NodeList costValueList = costE.getElementsByTagName("lom:value");

            if(costValueList.getLength()!=0 && costValueList.item(0).hasChildNodes() ){

                String costValue=costValueList.item(0).getChildNodes().item(0).getNodeValue();
                if(!costValue.isEmpty())
                    model.createResource(metadataURI)
                            .addProperty(costProperty,model.getNsPrefixURI(lomVoc)+"Cost-"+costValue);
            }
            // *****************************  right - copyright    ***********************************
            System.out.println("Analyzing Right.Copyright");

            NodeList copyrightAndOtherRestrictionsList=rightE.getElementsByTagName("lom:copyrightAndOtherRestrictions");
            Property copyrightAndOtherRestrictionsProperty=model.createProperty(model.getNsPrefixURI(lomPrefix)+"copyrightAndOtherRestrictions");
            Node nodeCopyright = copyrightAndOtherRestrictionsList.item(0);
            Element copyrightAndOtherRestrictionsE = (Element) nodeCopyright;
            NodeList copyrightValueList = copyrightAndOtherRestrictionsE.getElementsByTagName("lom:value");

            if(copyrightValueList.getLength()!=0 && copyrightValueList.item(0).hasChildNodes() ){

                String copyrightValue=copyrightValueList.item(0).getChildNodes().item(0).getNodeValue();
                if(!copyrightValue.isEmpty())
                    model.createResource(metadataURI)
                            .addProperty(copyrightAndOtherRestrictionsProperty,
                                    model.getNsPrefixURI(lomVoc)+"CopyrightAndOtherRestrictions-"+copyrightValue);
            }
            // *****************************  right - description    ***********************************
            System.out.println("Analyzing Right.Description");

            NodeList rightDescription=rightE.getElementsByTagName("lom:description");
            Property rightDescriptionProperty=model.createProperty(model.getNsPrefixURI(lomPrefix)+"rightDescription");
            for ( int i = 0; i < rightDescription.getLength(); i++) {
                Node rightDescriptionNode = rightDescription.item(i);
                Element e1 = (Element) rightDescriptionNode;
                // *****************************  string    ***********************************
                NodeList nodeList = e1.getElementsByTagName("lom:string");

                if(nodeList.getLength()!=0 && nodeList.item(0).hasChildNodes() ){

                    for ( int j = 0; j < nodeList.getLength(); j++) {
                        String rightDescriptionValue=nodeList.item(j).getChildNodes().item(0).getNodeValue();
                        if(!rightDescriptionValue.isEmpty()) {
                            model.createResource(metadataURI)
                                    .addProperty(rightDescriptionProperty, rightDescriptionValue,nodeList.item(j).getAttributes().item(0).getNodeValue());
                        }
                    }
                }
            }



        }
    }// Rights
     }


    // ***************** Relation Category  *******************************************************
    public void parseRelationCategory (NodeList relationList, String metadataURI){
        if (relationList != null && relationList.getLength() > 0) {
            for (int relationIndex = 0; relationIndex < relationList.getLength();relationIndex++) {

                Node node = relationList.item(relationIndex);

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element relationE = (Element) node;

                    Property relationProperty=model.createProperty(model.getNsPrefixURI(lomPrefix)+"relation");
                    Property relationBlankNode=model.createProperty(metadataURI+"relation-"+(relationIndex+1));
                    // Adding blank Node
                    model.createResource(metadataURI)
                            .addProperty(relationProperty, relationBlankNode);

                    // *****************************  relation- kind   ***********************************
                    System.out.println("Analyzing Relation.Kind");

                    NodeList kindList = relationE.getElementsByTagName("lom:kind");
                    Property kindProperty=model.createProperty(model.getNsPrefixURI(lomPrefix)+"kind");
                    Node kindNode = kindList.item(0);
                    Element kindE = (Element) kindNode;
                    NodeList kindValueList = kindE.getElementsByTagName("lom:value");
                    if(kindValueList.getLength()!=0 && kindValueList.item(0).hasChildNodes() ) {
                        String kindValue=kindValueList.item(0).getChildNodes().item(0).getNodeValue();
                        if(!kindValue.isEmpty())
                            relationBlankNode.addProperty(kindProperty,
                                    model.getNsPrefixURI(lomVoc)+"Kind-" +kindValue.replace(" ",""));
                    }

                    // *****************************  relation- Resource   ***********************************
                    System.out.println("Analyzing Relation.Resource");

                    NodeList resourceList = relationE.getElementsByTagName("lom:resource");
                    Node resourceNode = resourceList.item(0);
                    Element resourceE = (Element) resourceNode;
                    // *****************************  resource identifier    ***********************************
                    NodeList resourceIdentifierList=resourceE.getElementsByTagName("lom:identifier");
                    Property resourceIdentifierProperty=model.createProperty(model.getNsPrefixURI(lomPrefix)+"resourceIdentifier");

                    //System.out.println("leng="+resourceIdentifierList.getLength());
                    for ( int i = 0; i < resourceIdentifierList.getLength(); i++) {
                        Node resourceIdentifierNode = resourceIdentifierList.item(i);
                        Element resourceIdentifierE = (Element) resourceIdentifierNode;

                        // *****************************  catalog    ***********************************
                        System.out.println("Analyzing ResourceIdentifier.Catalog");

                        NodeList resourceCatalogNodeList = resourceIdentifierE.getElementsByTagName("lom:catalog");
                        Property resourceIdentifierBlankNode=model.createProperty(metadataURI+"resource-Identifier-"+(relationIndex+1)+(i+1));
                        if(resourceCatalogNodeList.getLength()!=0 && resourceCatalogNodeList.item(0).hasChildNodes() ) {

                            String resourceCatalogValue=resourceCatalogNodeList.item(0).getChildNodes().item(0).getNodeValue();
                            if(!resourceCatalogValue.isEmpty()) {
                                relationBlankNode
                                        .addProperty(resourceIdentifierProperty, resourceIdentifierBlankNode);
                                Property catalogResource=model.createProperty(model.getNsPrefixURI(lomPrefix)+"resourceCatalog");
                                resourceIdentifierBlankNode
                                        .addProperty(catalogResource, resourceCatalogValue);
                            }
                        }

                        // *****************************  Entry    ***********************************
                        System.out.println("Analyzing ResourceIdentifier.Entry");

                        NodeList resourceEntryNodeList = resourceIdentifierE.getElementsByTagName("lom:entry");
                        Property entryResourceProperty=model.createProperty(model.getNsPrefixURI(lomPrefix)+"resourceEntry");
                        if(resourceEntryNodeList.getLength()!=0 && resourceEntryNodeList.item(0).hasChildNodes() ) {

                            String resourceEntryValue=resourceEntryNodeList.item(0).getChildNodes().item(0).getNodeValue();
                            if(!resourceEntryValue.isEmpty()) {
                                resourceIdentifierBlankNode
                                        .addProperty(entryResourceProperty, resourceEntryValue);
                            }
                        }
                    }

                    // *****************************  Description    **********************************
                    System.out.println("Analyzing Resource.Description");

                    NodeList resourceDescriptionList=resourceE.getElementsByTagName("lom:description");
                    Property resourceDescriptionProperty=model.createProperty(model.getNsPrefixURI(lomPrefix)+"resourceDescription");
                    for ( int i = 0; i < resourceDescriptionList.getLength(); i++) {
                        Node descNode = resourceDescriptionList.item(i);
                        Element e1 = (Element) descNode;
                        // *****************************  string    ***********************************
                        NodeList descNodeList = e1.getElementsByTagName("lom:string");
                        if(descNodeList.getLength()!=0 && descNodeList.item(0).hasChildNodes() ) {

                            for ( int j = 0; j < descNodeList.getLength(); j++) {
                                String descriptionValue=descNodeList.item(j).getChildNodes().item(0).getNodeValue();
                                if(!descriptionValue.isEmpty()) {
                                    Property resourceDescriptionIdentifier=model.createProperty(metadataURI+"resource-Description"+(i+1));
                                    relationBlankNode
                                            .addProperty(resourceDescriptionProperty, resourceDescriptionIdentifier);
                                    resourceDescriptionIdentifier
                                            .addProperty(DCTerms.description, descriptionValue, descNodeList.item(j).getAttributes().item(0).getNodeValue());
                                }
                            }
                        }
                    }
                }
            }   // Relation
        }
    }

    // ***************** Annotation Category  *******************************************************
    public void parseAnnotationCategory (NodeList annotationList, String metadataURI) {
        if (annotationList != null && annotationList.getLength() > 0) {
            for (int annotationIndex = 0; annotationIndex < annotationList.getLength();annotationIndex++) {

                Node node = annotationList.item(annotationIndex);

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element annotationE = (Element) node;

                    Property annotationProperty=model.createProperty(model.getNsPrefixURI(lomPrefix)+"annotation");
                    Property annotationBlankNode=model.createProperty(metadataURI+"annotation-"+(annotationIndex+1));
                    // Adding blank Node
                    model.createResource(metadataURI)
                            .addProperty(annotationProperty, annotationBlankNode);

                    // *****************************  annotation entity   ***********************************
                    System.out.println("Analyzing Annotation.Entity");

                    NodeList annotationEntityList = annotationE.getElementsByTagName("lom:entity");
                    Property annotationEntityProperty=model.createProperty(model.getNsPrefixURI(lomPrefix)+"annotationEntity");
                    if(annotationEntityList.getLength()!=0 && annotationEntityList.item(0).hasChildNodes() ) {

                        String annotationValue=annotationEntityList.item(0).getChildNodes().item(0).getNodeValue();
                        if(!annotationValue.isEmpty())
                            annotationBlankNode.addProperty(annotationEntityProperty,annotationValue);
                    }
                    // *****************************  annotation dateTime   ***********************************
                    System.out.println("Analyzing Annotation.dateTime");

                    NodeList annotationDateTimeList = annotationE.getElementsByTagName("lom:dateTime");
                    Property annotationDateTimeProperty=model.createProperty(model.getNsPrefixURI(lomPrefix)+"annotationDateTime");
                    if(annotationDateTimeList.getLength()!=0 && annotationDateTimeList.item(0).hasChildNodes() ) {

                        String DateTimeValue=annotationDateTimeList.item(0).getChildNodes().item(0).getNodeValue();
                        if(!DateTimeValue.isEmpty())
                            annotationBlankNode.addProperty(annotationDateTimeProperty,DateTimeValue);
                    }
                    // *****************************  Description    **********************************
                    System.out.println("Analyzing Annotation.Description");

                    NodeList annotationDescriptionList=annotationE.getElementsByTagName("lom:description");
                    Property annotationDescriptionProperty=model.createProperty(model.getNsPrefixURI(lomPrefix)+"annotationDescription");

                    Node descNode = annotationDescriptionList.item(0);
                    Element e1 = (Element) descNode;
                    // *****************************  string  ***********************************
                    NodeList descNodeList = e1.getElementsByTagName("lom:string");

                    if(descNodeList.getLength()==0){
                        descNode = annotationDescriptionList.item(1);
                        e1 = (Element) descNode;
                        descNodeList = e1.getElementsByTagName("lom:string");
                    }
                    for ( int j = 0; j < descNodeList.getLength(); j++) {
                        String descriptionValue=descNodeList.item(j).getChildNodes().item(0).getNodeValue();
                        if(!descriptionValue.isEmpty()) {
                            annotationBlankNode
                                    .addProperty(annotationDescriptionProperty,
                                            descriptionValue, descNodeList.item(j).getAttributes().item(0).getNodeValue());
                        }
                    }
                }
            }   // Annotation
        }

    }


    // ***************** Classification Category  *******************************************************
    public void parseClassificationCategory (NodeList classificationList, String metadataURI){

        if (classificationList != null && classificationList.getLength() > 0) {

            for (int classificationIndex = 0; classificationIndex < classificationList.getLength();classificationIndex++) {

                Node node = classificationList.item(classificationIndex);

                //if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element classificationE = (Element) node;

                Property classificationProperty=model.createProperty(model.getNsPrefixURI(lomPrefix)+"classification");
                Property classificationBlankNode=model.createProperty(metadataURI+"classification-"+(classificationIndex+1));
                // Adding blank Node
                model.createResource(metadataURI)
                        .addProperty(classificationProperty, classificationBlankNode);

                // *****************************  classification - purpose ***********************************
                System.out.println("Analyzing Classification.Purpose");

                NodeList classificationPurposeList = classificationE.getElementsByTagName("lom:purpose");
                Property classificationPurposeProperty=model.createProperty(model.getNsPrefixURI(lomPrefix)+"purpose");
                Node purposeNode = classificationPurposeList.item(0);
                Element purposeE = (Element) purposeNode;
                NodeList purposeValueList = purposeE.getElementsByTagName("lom:value");

                if(purposeValueList.getLength()!=0 && purposeValueList.item(0).hasChildNodes() ) {

                    String purposeValue=purposeValueList.item(0).getChildNodes().item(0).getNodeValue();
                    if(!purposeValue.isEmpty())
                        classificationBlankNode.addProperty(classificationPurposeProperty
                                ,model.getNsPrefixURI(lomVoc)+"Purpose-" +purposeValue.replace(" ",""));
                }
                // *****************************  Description    **********************************
                System.out.println("Analyzing Classification.Description");

                NodeList classificationDescriptionList=classificationE.getElementsByTagName("lom:description");
                Property classificationDescriptionProperty=model.createProperty(model.getNsPrefixURI(lomPrefix)+"classificationDescription");

                Node descNode = classificationDescriptionList.item(0);
                Element e1 = (Element) descNode;
                // *****************************  string  ***********************************
                if(classificationDescriptionList.getLength()!=0)   {
                    NodeList descNodeList = e1.getElementsByTagName("lom:string");
                    if(descNodeList.getLength()!=0 && descNodeList.item(0).hasChildNodes() ) {

                        for ( int j = 0; j < descNodeList.getLength(); j++) {
                            String descriptionValue=descNodeList.item(j).getChildNodes().item(0).getNodeValue();
                            if(!descriptionValue.isEmpty()) {
                                classificationBlankNode
                                        .addProperty(classificationDescriptionProperty,
                                                descriptionValue, descNodeList.item(j).getAttributes().item(0).getNodeValue());
                            }
                        }
                    }
                }
                // *****************************  Keyword    **********************************
                System.out.println("Analyzing Classification.Keyword");

                NodeList keywordList=classificationE.getElementsByTagName("lom:keyword");
                Property keywordDescriptionPrefix=model.createProperty(model.getNsPrefixURI(lomPrefix)+"classificationKeyword");

                for ( int i = 0; i < keywordList.getLength(); i++) {
                    Property keywordIdentifier=model.createProperty(metadataURI+"Classification-keyword-"+(i+1));

                    Node keywordNode = keywordList.item(i);
                    Element keywrodE = (Element) keywordNode;
                    // *****************************  string    ***********************************
                    NodeList keywordNodeList = keywrodE.getElementsByTagName("lom:string");
                    if(keywordNodeList.getLength()!=0 && keywordNodeList.item(0).hasChildNodes() ) {

                        for ( int j = 0; j < keywordNodeList.getLength(); j++) {
                            String keywordValue=keywordNodeList.item(j).getChildNodes().item(0).getNodeValue();
                            if(!keywordValue.isEmpty()) {
                                classificationBlankNode
                                        .addProperty(keywordDescriptionPrefix, keywordIdentifier);
                                keywordIdentifier
                                        .addProperty(RDF.value, keywordValue,keywordNodeList.item(j).getAttributes().item(0).getNodeValue());
                            }
                        }
                    }

                }
                // *****************************  taxonpath    **********************************
                System.out.println("Analyzing Classification.TaxonPath");

                NodeList taxonPathList=classificationE.getElementsByTagName("lom:taxonPath");
                Property taxonPathProperty=model.createProperty(model.getNsPrefixURI(lomPrefix)+"taxonPath");
                for ( int taxonPathIndex = 0; taxonPathIndex < taxonPathList.getLength(); taxonPathIndex++) {
                    Node taxonPathNode = taxonPathList.item(taxonPathIndex);
                    Element taxonPathE = (Element) taxonPathNode;

                    //Adding blank node
                    Property taxonPathBlankNode=model.createProperty(metadataURI+"taxonPath-Identifier-"+(classificationIndex+1)+"-"+(taxonPathIndex+1));
                    classificationBlankNode
                            .addProperty(taxonPathProperty,taxonPathBlankNode);


                    // *****************************  string    ***********************************
                    NodeList sourceList = taxonPathE.getElementsByTagName("lom:source");
                    Property sourceProperty=model.createProperty(model.getNsPrefixURI(lomPrefix)+"taxonPathSource");
//                            if(classificationDescriptionList.getLength()!=0)   {

                    Node sourceNode = sourceList.item(0);
                    Element sourceE = (Element) sourceNode;
                    NodeList sourceStringList = sourceE.getElementsByTagName("lom:string");

                    if(sourceStringList.getLength()!=0 && sourceStringList.item(0).hasChildNodes() ) {

                        for ( int j = 0; j < sourceStringList.getLength(); j++) {
                            String sourceStringListValue=sourceStringList.item(j).getChildNodes().item(0).getNodeValue();
                            if(!sourceStringListValue.isEmpty()) {
                                taxonPathBlankNode
                                        .addProperty(sourceProperty, sourceStringListValue, sourceStringList.item(j).getAttributes().item(0).getNodeValue());
                            }
                        }
                    }

                    // *****************************  Taxon    ***********************************
                    System.out.println("Analyzing Classification.Taxon");

                    NodeList taxonIdentifierList=taxonPathE.getElementsByTagName("lom:taxon");
                    Property taxonIdentifierProperty=model.createProperty(model.getNsPrefixURI(lomPrefix)+"taxon");

                    //System.out.println("leng="+resourceIdentifierList.getLength());
                    for ( int taxonIndex = 0; taxonIndex < taxonIdentifierList.getLength(); taxonIndex++) {

                        Node taxonNode = taxonIdentifierList.item(taxonIndex);
                        Element taxonE = (Element) taxonNode;

                        Property taxonBlankNode=model.createProperty(metadataURI+"taxon-"+(classificationIndex+1)+"-"+(taxonPathIndex+1)+"-"+(taxonIndex+1));
                        taxonPathBlankNode
                                .addProperty(taxonIdentifierProperty, taxonBlankNode);
                        // *****************************  catalog    ***********************************
                        NodeList taxonIdList = taxonE.getElementsByTagName("lom:id");

                        if(taxonIdList.item(0).hasChildNodes()){
                            String taxonIdValue=taxonIdList.item(0).getChildNodes().item(0).getNodeValue();
                            if(!taxonIdValue.isEmpty()) {
                                Property taxonIdProperty=model.createProperty(model.getNsPrefixURI(lomPrefix)+"taxonID");
                                taxonBlankNode
                                        .addProperty(taxonIdProperty, taxonIdValue);
                            }
                        }

                        // *****************************  Entry    ***********************************
                        NodeList taxonEntryNodeList = taxonE.getElementsByTagName("lom:entry");
                        Property taxonEntryProperty=model.createProperty(model.getNsPrefixURI(lomPrefix)+"taxonEntry");

                        for ( int i = 0; i < taxonEntryNodeList.getLength(); i++) {
                            Node taxonEntryNode = taxonEntryNodeList.item(i);
                            Element taxonEntryE = (Element) taxonEntryNode;
                            // *****************************  string    ***********************************
                            NodeList nodeList = taxonEntryE.getElementsByTagName("lom:string");
                            if(nodeList.getLength()!=0 && nodeList.item(0).hasChildNodes() ) {

                                for ( int j = 0; j < nodeList.getLength(); j++) {
                                    String taxonEntryValue=nodeList.item(0).getChildNodes().item(0).getNodeValue();
                                    if(!taxonEntryValue.isEmpty()) {
                                        taxonBlankNode
                                                .addProperty(taxonEntryProperty, taxonEntryValue,
                                                        nodeList.item(j).getAttributes().item(0).getNodeValue());
                                    }
                                }
                            }
                        }
                    }

                }
                // }
            }
        }
    }



    // ***************** Main Parser  *******************************************************

    public void parseLOM(File folderPath, FileOutputStream fout) {

        String fileName;

        // setting prefix
         setRDFPrefix();

        File[] listOfFiles;
        int fileNumber=0;

        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".xml") || name.endsWith(".XML");
            }
        };

        try{
            listOfFiles = folderPath.listFiles(filter);
            fileNumber=listOfFiles.length;
            if(fileNumber==0){
                System.out.println( "No XML files found in the selected folder");
                System.exit(1);
            }
            System.out.println("processing "+fileNumber+" files ...");
            for (int fileIndex = 0; fileIndex < fileNumber; fileIndex++) {
                System.out.println("processing "+(fileIndex+1)+"th file ...");
                fileName=listOfFiles[fileIndex].getPath();
                File file = new File(fileName);
                if (file.exists()) {

                    // *************** identifier of XML file *********************************************
                    String metadataURI= UUID.randomUUID().toString()+"/";

                    model.createResource(metadataURI)
                            .addProperty(RDF.type, metadataURI+"resource");
                    try{
                        // *************** processing XML file *********************************************
                        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                        DocumentBuilder db = dbf.newDocumentBuilder();
                        Document doc = db.parse(file);
                        Element docEle = doc.getDocumentElement();

                        // Print root element of the document
                        // System.out.println("Root element of the document: " + docEle.getNodeName());
                        System.out.println("Starting...");

                        // ================================================ General ========================================================

                        NodeList GeneralList = docEle.getElementsByTagName("lom:general");
                        parseGeneralCategory(GeneralList, metadataURI);

                        // ================================================ LIFE CYCLE ========================================================
                        NodeList lifeCycleList = docEle.getElementsByTagName("lom:lifeCycle");
                        parseLifecycleCategory(lifeCycleList,metadataURI);

                        // ================================================ META-METADATA =====================================================
                        NodeList metaMetadataList = docEle.getElementsByTagName("lom:metaMetadata");
                        parsemetaMetadataCategory(metaMetadataList,metadataURI);

                        // ================================================ Technical =====================================================
                        NodeList TechnicalList = docEle.getElementsByTagName("lom:technical");
                        parseTechnicalCategory(TechnicalList,metadataURI);

                        // ================================================ Educational ========================================================

                        NodeList educationalList = docEle.getElementsByTagName("lom:educational");
                        parseEducationalCategory(educationalList,metadataURI);

                        // ================================================ Rights =====================================================
                        NodeList rightList = docEle.getElementsByTagName("lom:rights");
                        parseRightCategory(rightList,metadataURI);


                        // ================================================ Relation =====================================================
                        NodeList relationList = docEle.getElementsByTagName("lom:relation");
                        parseRelationCategory(relationList,metadataURI);

                        // ================================================ Annotation =====================================================
                        NodeList annotationList = docEle.getElementsByTagName("lom:annotation");
                        parseAnnotationCategory(annotationList,metadataURI);

                        // ================================================ Classification =====================================================
                        NodeList classificationList = docEle.getElementsByTagName("lom:classification");
                        parseClassificationCategory(classificationList,metadataURI);

                        //============================================================================================================
                        System.out.println("is run successfully!");
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                }
            }
        }catch (Exception fileE) {
                System.out.println(fileE);
        }
        model.write(fout,"TURTLE");

    }
    public static void main(String[] args) {


        XML2RDF parser = new XML2RDF();
        try{
            // Open the file that is the first
            // command line parameter
            FileInputStream fstream = new FileInputStream(inputFileName);
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            FileOutputStream fout=new FileOutputStream(
                    "lom.ttl");
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            InputStream inRDF = FileManager.get().open(inputFileName2);
            // some definitions
            long startTime = System.currentTimeMillis();
            File xmlFolder = new File("C:\\Users\\Enayat\\IdeaProjects\\test\\input");

            parser.parseLOM(xmlFolder,fout);
            long endTime   = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            System.out.println("Run time="+totalTime);
            // create the resource

        } catch (FileNotFoundException e) {
            System.err.println("FileStreamsReadnWrite: " + e);
        } catch (IOException e) {
            System.err.println("FileStreamsReadnWrite: " + e);
        }
    }
}
